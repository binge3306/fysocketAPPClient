package fy.socket.JavaWebsocket.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.java_websocket.exceptions.WebsocketPongResponseException;

import fy.socket.JavaWebsocket.abs.APPClientAbs;
import fy.socket.JavaWebsocket.exception.ConnectWebsocketException;
import fy.socket.JavaWebsocket.exception.HandshakeWebsocketException;
import fy.socket.JavaWebsocket.exception.IllegalWebsocketException;
import fy.socket.JavaWebsocket.util.ByteBufferSwap;
import fy.socket.JavaWebsocket.util.logger.LoggerUtil;

public class APPClient extends APPClientAbs{

	
	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());
	private String loginUser;
	private String loginVerify;
	private String HomeURL;
	
	public APPClient(String host,int port) throws URISyntaxException {
		super(new URI("ws://"+host+":"+port));
	}

	@Override
	public void onMessageB(ByteBuffer msg) {
		logger.log(Level.INFO, "收到一条二进制消息");
	}

	@Override
	public void onMessageT(String msg) {
		logger.log(Level.INFO, "收到一条文本消息"+msg);
	}

	@Override
	public void onError(Exception e, String info) {
		logger.log(Level.INFO, "异常:"+info+" ."+e);
	}

	@Override
	public void onClose(Exception e, String info) {
		if(e instanceof WebsocketPongResponseException){
			try {
				reConnect();
			} catch (IllegalWebsocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ConnectWebsocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (HandshakeWebsocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		logger.log(Level.INFO, "通道关闭 ="+info+"  ."+e);
	}
	
	
	@Override
	public void connection(int heartbeat) throws IllegalWebsocketException {
		// TODO Auto-generated method stub
		super.connection(heartbeat);
	}

	@Override
	public void connection() throws IllegalWebsocketException {
		// TODO Auto-generated method stub
		super.connection();
	}

	@Override
	public void verify(String userKey, String virifyCode, String url)
			throws IOException, ConnectWebsocketException,
			HandshakeWebsocketException {
		this.HomeURL = url;
		this.loginUser = userKey;
		this.loginVerify = virifyCode;
		super.verify(userKey, virifyCode, url);
	}

	/**
	 * 添加的测试方法
	 * @param msg		消息内容	默认内容："20840##0##appclient msg";
	 * @param times		发送次数	
	 * @param timeout	每次发送间隔  s
	 */
	public void sendMsg(String msg ,int times ,int timeout){

		
		//ByteBuffer msgb = ByteBuffer.allocate(10);
		if(msg == null||msg == ""){
			// chatId##isPublice##msgContent
			msg = "chatroom1##0##appclient msg";
		}
		String msgSend = msg;
		while(times >= 0){
			times --;
			msgSend += " ，sendtimes= "+times;
			
			try {
				logger.log(Level.INFO, "sendMsgText 调用 sendMsgText 方法,将消息插入待发送队列");
				sendMsgText(msgSend, 0);
				if(timeout>0)
					TimeUnit.SECONDS.sleep(timeout);
			} catch (IllegalWebsocketException  e) {
				logger.log(Level.WARNING, "IllegalWebsocketException 异常"+"  ."+e);
			}
			 catch (InterruptedException e) {
				logger.log(Level.WARNING, "InterruptedException 异常"+"  ."+e);
			}
			msgSend = msg;
		}
	}
	

	/**
	 * 设置是否使用心跳
	 * @param heartbeat 0 表示不使用心跳，1表示使用心跳但是使用默认心跳周期，>1 表示使用心跳，是用当前heartbeat时间为心跳周期
	 * <br>
	 * heartbeat 不能为负数或者小数（表示不使用心跳）
	 */
	public void setPing(int heartbeat){
//		if(heartbeat == 0){
//			logger.log(Level.INFO, "不使用心跳");	
//		}else if(heartbeat == 1){
//			logger.log(Level.INFO, "使用心跳，并使用默认心跳周期");
//			sendPing( heartbeat);
//		}else if(heartbeat > 1){
//			logger.log(Level.INFO, "使用当前值作为心跳周期");
//			sendPing( heartbeat);
//		}else{
//			logger.log(Level.INFO, "参数错误，不使用心跳");
//		}
		
		
	}
	
	private void reConnect() throws IllegalWebsocketException, ConnectWebsocketException, HandshakeWebsocketException, IOException{
		connection(1);
		verify(loginUser, loginVerify, HomeURL);
	}


}
