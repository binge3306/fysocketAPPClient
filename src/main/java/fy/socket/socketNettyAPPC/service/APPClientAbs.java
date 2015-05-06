package fy.socket.socketNettyAPPC.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import fy.socket.SocketAPPClient.core.*;
import fy.socket.SocketAPPClient.core.handshake.*;
import fy.socket.SocketAPPClient.exception.*;
import fy.socket.SocketAPPClient.interf.*;
import fy.socket.SocketAPPClient.util.Base64;
import fy.socket.SocketAPPClient.util.ByteBufferSwap;
import fy.socket.SocketAPPClient.util.logger.LoggerUtil;

public abstract class  APPClientAbs implements Runnable,WebsocketCoreInterf,FeedbackInterf,WebsocketClientInterf{

	private Logger logger = LoggerUtil.getLogger(this.getClass().getName()); 
	private WebsocketClientImpl coreClient;

	/**
	 * The URI this channel is supposed to connect to.
	 */
	protected URI uri = null;
	private int connectTimeout = 0;
	private Socket socket = null;

	private InputStream istream;

	private OutputStream ostream;
	
	private Proxy proxy = Proxy.NO_PROXY;
	
	private String Sec_WebSocket_Key;
	private Map<String,String> headers;
	
    /** No options specified. Value is zero. */
    private  final  int NO_OPTIONS = 0;
	
	// 保存用户key ，方便验证用户
	private String userKey;
	private Thread writeIOThread;
	private Thread readIOThread;
	
	private boolean handshakeStatus  = false;
	private boolean virifyStatus = false;
	
	
	private Object lala = new Object();
	
	private final Random reuseableRandom = new Random();
	
	public APPClientAbs(URI url){
		this.uri = url;
		this.coreClient = new WebsocketClientImpl(this);
		
	}
	
	public APPClientAbs(String host ,int port){
		
		this.coreClient = new WebsocketClientImpl(this);
		
	}

	@Override
	public  void virify(String userKey, String virifyCode,String url) throws IOException,
			ConnectWebsocketException, HandshakeWebsocketException {
		String tag = ":app";
		url = "app"+url;
		logger.log(Level.INFO, "发送用户验证消息");
		// wurunzhou eidt for (握手没有完成之前，如果碰到发送用户验证消息就抛出异常是反人类的) at 20150410 begin
		int htime = 0;
		while(!handshakeStatus){
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(htime>5) break;
		}
		if(handshakeStatus){
			coreClient.sendMsgQueue.setPendingStatus(true);
			byte[] virify = (userKey+":"+virifyCode+":"+url+tag).getBytes();
			ByteBuffer virifyMsg = ByteBuffer.allocate(virify.length);
			virifyMsg.put(virify);
			virifyMsg.flip();
			coreClient.sendMsgQueue.insert(virifyMsg);
			//coreClient.sendMsgQueue.setPendingStatus(false);
		}else {
			// 如果五秒之后 ，还是没有握手成功，那就等着验证用户抛出异常吧
			logger.log(Level.INFO,"五秒之后 ，还是没有握手成功，那就让验证用户抛出异常吧");
			throw new  HandshakeWebsocketException();
		}
		// wurunzhou eidt for (握手没有完成之前，如果碰到发送用户验证消息就抛出异常是反人类的) at 20150410 end

	}


	@Override
	public  void connection() {
		if( writeIOThread != null )
			throw new IllegalStateException( "WebSocketClient objects are not reuseable" );
		writeIOThread = new Thread( this );
		// 启动client线程
		writeIOThread.start();
		logger.log(Level.INFO,"connection");
		
	}

	private int getPort() {
		int port = uri.getPort();
//		if( port == -1 ) {
//			String scheme = uri.getScheme();
//			if( scheme.equals( "wss" ) ) {
//				return WebSocket.DEFAULT_WSS_PORT;
//			} else if( scheme.equals( "ws" ) ) {
//				return WebSocket.DEFAULT_PORT;
//			} else {
//				throw new RuntimeException( "unkonow scheme" + scheme );
//			}
//		}
		return port;
	}
	
	@Override
	public   void sendMsgBinary(ByteBuffer msg, long timeout) {
		
	}

	@Override
	public   void sendMsgBinary(List<ByteBuffer> msg, long timeout) {
		
	}

	@Override
	public   void sendMsgText(ByteBuffer msg, long timeout)
			throws IllegalWebsocketException {
		
		if(virifyStatus){
			if(coreClient.sendMsgQueue.hasPendingStatus()){
				// 将消息添加到待发送队列，有专门的写线程处理最终的发送任务
				coreClient.sendMsgQueue.insert(msg);
				
			}else{
				// 如果待发送队列不接收新消息，抛出非法异常
				throw new IllegalWebsocketException();
			}
		}

	}

	@Override
	public void close(long timeout) {
		
	}


	@Override
	public void run() {
		
		try {
			if( socket == null ) {
				socket = new Socket( proxy );
			} else if( socket.isClosed() ) {
				throw new IOException();
			}
			if( !socket.isBound() )
				socket.connect( new InetSocketAddress( uri.getHost(), getPort() ), connectTimeout );
			istream = socket.getInputStream();
			ostream = socket.getOutputStream();

			logger.log(Level.INFO,"创建socket");
			sendHandshake();
			logger.log(Level.INFO,"发送握手");
		} catch ( /*IOException | SecurityException | UnresolvedAddressException | InvalidHandshakeException | ClosedByInterruptException | SocketTimeoutException */Exception e ) {
			onWebsocketError(  e,"创建socket连接异常" );
			return;
		}

	
		logger.log(Level.INFO," 启动读写线程");
		writeIOThread = new Thread( new WriteIOMsgThread() );
		writeIOThread.start();
		
		readIOThread = new Thread(new ReadIOMsgThread());
		readIOThread.start();
		
	}

	/**
	 * 该握手是websocket协议规定的一部分
	 */
	private void sendHandshake(){
		/**
		 * 这里要写个回调
		 * 握手成功之后，启动读写线程
		 */
		logger.log(Level.INFO,"发送握手信息");
		String path;
		String part1 = uri.getPath();
		String part2 = uri.getQuery();
		if( part1 == null || part1.length() == 0 )
			path = "/";
		else
			path = part1;
		if( part2 != null )
			path += "?" + part2;
		int port = getPort();
		String host = uri.getHost() + ":" + getPort();

		HandshakeImpl1Client handshake = new HandshakeImpl1Client();
		handshake.setResourceDescriptor( path );
		handshake.put( "Host", host );
		if( headers != null ) {
			for( Map.Entry<String,String> kv : headers.entrySet() ) {
				handshake.put( kv.getKey(), kv.getValue() );
			}
		}
		
		
		// 发送握手信息之前 先要开启发送队列
		coreClient.sendMsgQueue.setPendingStatus(true);
		
		for( ByteBuffer b : prodHandshakeClient(handshake) ) {
			coreClient.sendMsgQueue.insert(b);
		}
		logger.log(Level.INFO,"握手信息插入待发送队列");
		//coreClient.sendMsgQueue.setPendingStatus(false);
	}
	
	
	
	@Override
	public void onHandshake(String msg){
		
		//String la = "";
		if(machKEYandACCEPT(msg,generateFinalKey(Sec_WebSocket_Key))){
			// 握手成功
			coreClient.setHsStatus(true);
			handshakeStatus = true;
//			notifyAll();
			logger.log(Level.INFO,"握手成功");
		}else {
			 onError(new IllegalWebsocketException(), "握手失败");
		}
		
	}

	private String generateFinalKey( String in ) {
		String seckey = in.trim();
		String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		MessageDigest sh1;
		try {
			sh1 = MessageDigest.getInstance( "SHA1" );
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException( e );
		}
		return Base64.encodeBytes( sh1.digest( acc.getBytes() ) );
	}
	
	@Override
	public void onVirify(ByteBuffer msg,boolean pass) throws VerifyWebsocketException {
		
		if(machUser(ByteBufferSwap.byteBufferToString(msg))){
			// 用户验证成功
			coreClient.setVfStatus(true);
			virifyStatus = true;
//			notifyAll();logger.log(Level.INFO,
			logger.log(Level.INFO,"用户验证成功");
		}else {
			throw new VerifyWebsocketException();
//			onError(new IllegalWebsocketException(), "用户验证失败");
		}
	}

	/**
	 * 产生客户端握手信息
	 * <br>
	 * 其格式：
	 * 	GET / HTTP/1.1
		Connection: Upgrade
		Host: localhost:8887
		Sec-WebSocket-Key: EiJU4iwKojWZNPx2AkD+3g==
		Sec-WebSocket-Version: 13
		Upgrade: websocket
	 * @return
	 */
	private List<ByteBuffer> prodHandshakeClient(ClientHandshakeBuilder handshake){

		handshake.put( "Upgrade", "websocket" );
		handshake.put( "Connection", "Upgrade" ); // to respond to a Connection keep alives
		handshake.put( "Sec-WebSocket-Version", "8" );

		byte[] random = new byte[ 16 ];
		reuseableRandom.nextBytes( random );
		Sec_WebSocket_Key = Base64.encodeBytes( random );
		handshake.put( "Sec-WebSocket-Key", Sec_WebSocket_Key );
		
		handshake.put( "Sec-WebSocket-Version", "13" );// overwriting the previous
		
		return createHandshake(handshake);
	}
	
	
	private List<ByteBuffer> createHandshake(Handshakedata handshakedata){
		
		StringBuilder bui = new StringBuilder( 100 );
		if( handshakedata instanceof ClientHandshake ) {
			bui.append( "GET " );
			bui.append( ( (ClientHandshake) handshakedata ).getResourceDescriptor() );
			bui.append( " HTTP/1.1" );
		} else {
			throw new RuntimeException( "unknow role" );
		}
		bui.append( "\r\n" );
		Iterator<String> it = handshakedata.iterateHttpFields();
		while ( it.hasNext() ) {
			String fieldname = it.next();
			String fieldvalue = handshakedata.getFieldValue( fieldname );
			bui.append( fieldname );
			bui.append( ": " );
			bui.append( fieldvalue );
			bui.append( "\r\n" );
		}
		bui.append( "\r\n" );
		byte[] httpheader = asciiBytes( bui.toString() );

		byte[] content =  null;
		ByteBuffer bytebuffer = ByteBuffer.allocate( ( content == null ? 0 : content.length ) + httpheader.length );
		bytebuffer.put( httpheader );
		if( content != null )
			bytebuffer.put( content );
		bytebuffer.flip();
		return Collections.singletonList( bytebuffer );
	}
	/*
	* @return ASCII encoding in bytes
	*/
	public  byte[] asciiBytes( String s ) {
		try {
			return s.getBytes( "ASCII" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}
	
	
	/**
	 * 判断是否握手成功
	 * @param access
	 * @return
	 */
	private boolean machKEYandACCEPT(String access,String key){
		
		if(access != null &&access != ""){
			return key.equals( access ) ;
		}else 
			return false;
	}
	
	private boolean machUser(String vfpass){
		
		if("ok".equals(vfpass)){
			return true;
		}
		return false;
	}
	
	
	@Override
	public void onWebsocketMessageB(ByteBuffer msg) {
		onMessageB(msg);
		
	}

	@Override
	public void onWebsocketMessageT(ByteBuffer msg) {
		onMessageT(msg);
		
	}

	@Override
	public void onWebsocketError(Exception e, String info) {

		coreClient.sendMsgQueue.setPendingStatus(false);
		onError(e,info);
	}

	@Override
	public void onWebsocketClose(Exception e, String info) {
		coreClient.sendMsgQueue.setPendingStatus(false);
		onClose(e,info);
	}
	
	
	/**
	 * 接收二进制消息
	 * @param msg
	 */
	public abstract void onMessageB(ByteBuffer msg);
	
	/**
	 * 接收文本消息
	 * @param msg 文件消息内容
	 */
	public abstract void onMessageT(ByteBuffer msg);
	
	/**
	 * 读取和写入异常		
	 * @param e 异常
	 * @param info 异常的补充消息
	 */
	public abstract void onError(Exception e,String info);
	
	/**
	 * 异常关闭
	 * @param e 异常
	 * @param info 异常的补充消息
	 */
	public abstract void onClose(Exception e,String info);
	
	/**
	 * 处理消息线程
	 * <br>
	 * 该线程负责从通道中读取消息
	 * 并将该消息放到待处理队列中，
	 * 如果待处理队列满，则线程阻塞
	 * @author wurunzhou
	 *
	 */
	class ReadIOMsgThread implements Runnable{

		
		@Override
		public void run() {
			logger.log(Level.INFO,"读IO线程启动");
			Thread.currentThread().setName( "WebsocketReadThread" );
			// 不断的读IO
			byte[] rawbuffer = new byte[ 16834 ];
			int readBytes;
			try {
			while ( ( readBytes = istream.read( rawbuffer ) ) != -1 ) {
				if(!handshakeStatus){
					// 还没有处理握手，不要对握手进行解密
					logger.log(Level.INFO,"收到信息用于websocket握手");
					coreClient.processMsgT( ByteBuffer.wrap( rawbuffer, 0, readBytes ) ,handshakeStatus);
				}else{
					// 用户验证和收消息不要 掩码处理
					logger.log(Level.INFO,"收到信息用于用户验证和收消息");
					coreClient.processMsgT( ByteBuffer.wrap( rawbuffer, 0, readBytes ),handshakeStatus );
				}
				
			}
			}catch(IOException e) {
				logger.log(Level.WARNING,"客户端读取异常");
				onWebsocketError(e, "客户端读取异常");
			}
		}
		
	}
	
	/**
	 * 
	 * 发送线程
	 * <br> 
	 * 从待发送消息队列中取出一个消息发送出去，
	 * 如果待发送消息队列为空，则该线程阻塞。
	 * @author wurunzhou
	 *
	 */
	class WriteIOMsgThread implements Runnable,CodeWebsocketFBInter{

		
		@Override
		public void run() {
			Thread.currentThread().setName( "WebsocketWriteThread" );
			while ( !Thread.interrupted() ) {
				logger.log(Level.INFO,"写IO线程启动");
				ByteBuffer buffer = coreClient.sendMsgQueue.get();
				if(!handshakeStatus){
					encodeFine(buffer);
				}else{
					new DeEncodeUtil(this).encodeWebsocket(buffer);
				}
				

			}
			
		}
		
		
		@Override
		public void decodeFine(ByteBuffer websocketMsg)   {
			
			
		}


		@Override
		public void encodeFine(ByteBuffer websocketMsg)  {
			try {
				logger.log(Level.INFO,"写消息到IO通道"+websocketMsg.capacity());
				ostream.write( websocketMsg.array(), 0, websocketMsg.limit() );
				ostream.flush();
			} catch (IOException e) {
				logger.log(Level.WARNING,"客户端发送消息异常");
				onWebsocketError(e, "客户端发送消息异常");
			}
		}
		
	}


}
