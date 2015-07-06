package fy.socket.JavaWebsocket.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;




















import javax.net.ssl.SSLException;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.util.logger.LoggerUtil;

import fy.socket.JavaWebsocket.exception.*;
import fy.socket.JavaWebsocket.interf.*;
import fy.socket.JavaWebsocket.util.*;
/**
 * @author Bryan-zhou
 * @date 2015年5月15日下午3:03:38
 **/
public class WebsocketClientImpl  implements Runnable,WebsocketClientInterf{

	
	private Thread  workThread;
	
	private  String URL;//System.getProperty("url", "ws://127.0.0.1:8877");
	  
	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());
	/**
	 * 握手状态
	 */
	private boolean hsStatus  = false;
	/**
	 * 用户验证状态
	 */
	private boolean vfStatus = false;
	/**
	 * 回调接口
	 */
	private WebsocketCoreInterf wCoreInterf;
	/**
	 * 待发送消息队列
	 */
	private  SendMsgQueue sendMsgQueue  ;


	private int USERID;
	
	public WebsocketClientImpl(URI serverUri, Draft draft,WebsocketCoreInterf wCoreInterf) {
//		super(serverUri, draft);
		this.wCoreInterf = wCoreInterf;
		this.URL = System.getProperty( "URL","ws://"+serverUri.getHost()+":"+ getPort(serverUri)) ;
		//this.URL = serverUri;
	}

	public WebsocketClientImpl(URI serverURI,WebsocketCoreInterf wCoreInterf) {
//		super(serverURI);
		this.wCoreInterf = wCoreInterf;
	}

	public WebsocketClientImpl(URI url, WebsocketCoreInterf wCoreInterf, int uSERID2) {
//		super(url,uSERID2);
		this.wCoreInterf = wCoreInterf;
		this.URL = System.getProperty( "URL","ws://"+url.getHost()+":"+ getPort(url)) ;
		this.USERID = uSERID2;

	}
	
	private EventLoopGroup group;

	private Channel ch;
	
	@Override
	public void run() {

		Thread.currentThread().setName("workReceiveThread" +USERID);
		try {
			
			URI uri = new URI(URL);

			String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
			final String host = uri.getHost() == null ? "127.0.0.1" : uri
					.getHost();
			final int port;
			if (uri.getPort() == -1) {
				if ("http".equalsIgnoreCase(scheme)) {
					port = 80;
				} else if ("https".equalsIgnoreCase(scheme)) {
					port = 443;
				} else {
					port = -1;
				}
			} else {
				port = uri.getPort();
			}

			if (!"ws".equalsIgnoreCase(scheme)
					&& !"wss".equalsIgnoreCase(scheme)) {
				System.err.println("Only WS(S) is supported.");
				return;
			}

			final boolean ssl = "wss".equalsIgnoreCase(scheme);
			final SslContext sslCtx;
			if (ssl) {
				sslCtx = SslContext
						.newClientContext(InsecureTrustManagerFactory.INSTANCE);
			} else {
				sslCtx = null;
			}

		
			 group = new NioEventLoopGroup();

			// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08
			// or V00.
			// If you change it to V00, ping is not supported and remember to
			// change
			// HttpResponseDecoder to WebSocketHttpResponseDecoder in the
			// pipeline.
			final WebSocketClientHandler handler = new WebSocketClientHandler(wCoreInterf,
					WebSocketClientHandshakerFactory.newHandshaker(uri,
							WebSocketVersion.V13, null, false,
							new DefaultHttpHeaders()));

			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) {
							ChannelPipeline p = ch.pipeline();
							if (sslCtx != null) {
								p.addLast(sslCtx.newHandler(ch.alloc(), host,
										port));
							}
							p.addLast(new HttpClientCodec(),
									new HttpObjectAggregator(8192), handler);
						}
					});

			ch = b.connect(uri.getHost(), port).sync().channel();
			handler.handshakeFuture().sync();

		} catch (URISyntaxException e) {
			logger.log(Level.SEVERE, "异常" + e.toString());
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "异常" + e.toString());
		} catch (SSLException e) {
			logger.log(Level.SEVERE, "异常" + e.toString());
		} finally {

		}

	}

	@Override
	public void connection(int heartbeat) throws IllegalWebsocketException {

		workThread = new Thread(this);
		workThread.start();
	}

	@Override
	public void connection() throws IllegalWebsocketException {
		connection(0);
	}

	@Override
	public void verify(String userKey, String virifyCode, String url)
			throws IOException, ConnectWebsocketException,
			HandshakeWebsocketException, InterruptedException {
		String msg = userKey + ":" + virifyCode + ":" + url;

		WebSocketFrame frame = new TextWebSocketFrame(msg);
		ch.writeAndFlush(frame);

	}

	@Override
	public void sendMsgBinary(ByteBuffer msg, long timeout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMsgBinary(List<ByteBuffer> msg, long timeout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMsgText(String msg, long timeout)
			throws IllegalWebsocketException, InterruptedException {
        if (msg == null) {
        	return;
        } else if ("bye".equals(msg.toLowerCase())) {
            ch.writeAndFlush(new CloseWebSocketFrame());
            ch.closeFuture().sync();
            
            return;
        } else if ("ping".equals(msg.toLowerCase())) {
            WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
            ch.writeAndFlush(frame);
        } else {
        	
    		WebSocketFrame frame = new TextWebSocketFrame(msg);
    		ch.writeAndFlush(frame);
        }
	}

	@Override
	public void close(long timeout) {

		 group.shutdownGracefully();
	}


	private int getPort(URI uri) {
		int port = uri.getPort();
		if( port == -1 ) {
			String scheme = uri.getScheme();
			if( scheme.equals( "wss" ) ) {
				return WebSocket.DEFAULT_WSS_PORT;
			} else if( scheme.equals( "ws" ) ) {
				return WebSocket.DEFAULT_PORT;
			} else {
				throw new RuntimeException( "unkonow scheme" + scheme );
			}
		}
		return port;
	}
	
}
