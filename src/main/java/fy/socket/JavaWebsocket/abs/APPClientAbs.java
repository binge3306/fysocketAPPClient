package fy.socket.JavaWebsocket.abs;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.java_websocket.exceptions.ExceptionErrorCode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.WebsocketPongResponseException;
import org.java_websocket.util.TimeQueue;
import org.java_websocket.util.logger.LoggerUtil;

import fy.socket.JavaWebsocket.core.WebsocketClientImpl;
import fy.socket.JavaWebsocket.core.WebsocketCoreInterf;
import fy.socket.JavaWebsocket.exception.ConnectWebsocketException;
import fy.socket.JavaWebsocket.exception.HandshakeWebsocketException;
import fy.socket.JavaWebsocket.exception.IllegalWebsocketException;
import fy.socket.JavaWebsocket.exception.VerifyWebsocketException;
import fy.socket.JavaWebsocket.interf.FeedbackInterf;
import fy.socket.JavaWebsocket.interf.WebsocketClientInterf;
import fy.socket.JavaWebsocket.util.ByteBufferSwap;

/**
 * 
 * @author wurunzhou
 *
 */
public abstract class  APPClientAbs implements WebsocketCoreInterf,FeedbackInterf{

	/**
	 * 日志
	 */
	private Logger logger = LoggerUtil.getLogger(this.getClass().getName()); 
	
	/**
	 * 真正生产环境不需要，只是用来
	 */
	private int USERID;
	
	/**
	 * java-websocket 连接核心
	 */
	private WebsocketClientImpl coreClient;

	/**
	 * websocket连接状态
	 * <br>
	 * 握手状态，默认为否
	 */
	private boolean handshakeStatus = false;
	/**
	 * websocket连接状态
	 * <br>
	 * 验证状态，默认为否
	 */
	private boolean verifyStatus = false;
	
	private String loginUser;
	private String loginVerify;
	private String HomeURL;

	/**
	 * 用来计算消息延时
	 */
	private TimeQueue delayTime;
	
	private URI url;
	
	public APPClientAbs(URI url){
		this.url = url;
	}
	
	public APPClientAbs(URI url,int userid){
		this.url = url;
		this.USERID = userid;
	}

	public void connection(int heartbeat) throws IllegalWebsocketException {
		if(coreClient == null){
			logger.log(Level.INFO,"new coreclient,url:"+url);
			coreClient = new WebsocketClientImpl(url,this,USERID);
		}
		coreClient.connection( heartbeat);
		delayTime = new TimeQueue();
	}

	public void connection() throws IllegalWebsocketException {
		connection( 0);
	}
	
	public void verify(String userKey, String virifyCode, String url)
			throws IOException, ConnectWebsocketException,
			HandshakeWebsocketException, InterruptedException {
		this.HomeURL = url;
		this.loginUser = userKey;
		this.loginVerify = virifyCode;
		String tag = ":app";
		url = "app"+url;
		logger.log(Level.INFO, "发送用户验证消息");
		// wurunzhou eidt for (握手没有完成之前，如果碰到发送用户验证消息就抛出异常是反人类的) at 20150410 begin
		int htime = 0;
		while(!handshakeStatus){
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,"error 异常"+e.toString());
			}
			if(htime++>15) break;
		}
		if(handshakeStatus){
			coreClient.verify(userKey,virifyCode,url+tag);
		}else {
			// 如果五秒之后 ，还是没有握手成功，那就等着验证用户抛出异常吧
			logger.log(Level.INFO,"15秒之后 ，还是没有握手成功，那就让验证用户抛出异常吧");
			throw new  HandshakeWebsocketException();
		}
	}

	public void sendMsgBinary(ByteBuffer msg, long timeout) {
		
		coreClient.sendMsgBinary( msg,timeout);
	}

	public void sendMsgBinary(List<ByteBuffer> msg, long timeout) {

			coreClient.sendMsgBinary(msg,  timeout);
		
	}

	public void sendMsgText(String msg, long timeout)
			throws IllegalWebsocketException, InterruptedException {
		
		int htime = 0;
		while(!verifyStatus){
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE,"error 异常"+e.toString());
			}
			if(htime++>15) break;
		}
		if(verifyStatus){
			// 标记发送时间
			delayTime.put(new Date());
			coreClient.sendMsgText(msg,  timeout);
		}else {
			// 如果五秒之后 ，还是没有握手成功，那就等着验证用户抛出异常吧
			logger.log(Level.INFO,"15秒之后 ，还是没有验证成功，那就抛出异常吧");
			throw new  IllegalWebsocketException();
		}
		
	}
	
//	public void sendPing(int heartbeat){
//		coreClient.sendPing( heartbeat);
//	}

	public void close(long timeout) {
		coreClient.close(0);
		coreClient = null;
	}

	@Override
	public void onWebsocketMessageB(ByteBuffer msg) {
		onMessageB(msg);
		
	}

	@Override
	public void onWebsocketMessageT(String msg)   {
		if(verifyStatus){
			delayTime();
			onMessageT(msg);
		}else{
			onVerify(msg,true);
		}
		
		
	}
	private void delayTime(){
		Date date1 = delayTime.get();
		long times = new Date().getTime()-date1.getTime();
		logger.log(Level.INFO,Thread.currentThread().getName()+" 消息延时时间 "+times  +" (毫秒)");
	}

	@Override
	public void onWebsocketError(Exception e, String info) {
		//coreClient.sendMsgQueue.setPendingStatus(false);
		if(info.contains("重连")&&e instanceof WebsocketPongResponseException){
			logger.log(Level.WARNING, "连接失效异常:"+info+" ."+e);
			logger.log(Level.INFO, "准备重连……");
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
//			new Thread(new ReconnectThread()).start();

		}else{
			onError(e,info);	
		}
		
		
	}

	@Override
	public void onWebsocketClose(Exception e, String info) {
		//coreClient.sendMsgQueue.setPendingStatus(false);
		onClose(e,info);
		
	}

//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		coreClient.connect();
//	}

	/* (non-Javadoc)
	 * @see fy.socket.JavaWebsocket.interf.FeedbackInterf#onMessageB(java.nio.ByteBuffer)
	 */
	@Override
	public abstract void onMessageB(ByteBuffer msg);

	/* (non-Javadoc)
	 * @see fy.socket.JavaWebsocket.interf.FeedbackInterf#onMessageT(java.nio.ByteBuffer)
	 */
	@Override
	public abstract void onMessageT( String msg);
	
	/* (non-Javadoc)
	 * @see fy.socket.JavaWebsocket.interf.FeedbackInterf#onError(java.lang.Exception, java.lang.String)
	 */
	@Override
	public abstract void onError(Exception e, String info) ;
	
	/* (non-Javadoc)
	 * @see fy.socket.JavaWebsocket.interf.FeedbackInterf#onClose(java.lang.Exception, java.lang.String)
	 */
	@Override
	public abstract void onClose(Exception e, String info) ;

	/* (non-Javadoc)
	 * @see fy.socket.JavaWebsocket.interf.FeedbackInterf#onHandshake(java.lang.String)
	 */

	@Override
	public void onHandshake(String access) {
		handshakeStatus  = true;
		logger.log(Level.INFO,"握手成功");
		
	}

	/* (non-Javadoc)
	 * @see fy.socket.JavaWebsocket.interf.FeedbackInterf#onVirify(java.nio.ByteBuffer, boolean)
	 */
	@Override
	public void onVerify(String msg, boolean pass){
		logger.log(Level.INFO,"服务器放回验证信息"+ msg);
		if("ok".equals(msg)){
			verifyStatus = true;
			logger.log(Level.INFO,"验证成功");
		}else{
			logger.log(Level.INFO,"验证bu成功");
		}
		
	}


}
