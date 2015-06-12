package fy.socket.JavaWebsocket.abs;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import fy.socket.JavaWebsocket.core.WebsocketClientImpl;
import fy.socket.JavaWebsocket.core.WebsocketCoreInterf;
import fy.socket.JavaWebsocket.exception.ConnectWebsocketException;
import fy.socket.JavaWebsocket.exception.HandshakeWebsocketException;
import fy.socket.JavaWebsocket.exception.IllegalWebsocketException;
import fy.socket.JavaWebsocket.exception.VerifyWebsocketException;
import fy.socket.JavaWebsocket.interf.FeedbackInterf;
import fy.socket.JavaWebsocket.interf.WebsocketClientInterf;
import fy.socket.JavaWebsocket.util.ByteBufferSwap;
import fy.socket.JavaWebsocket.util.logger.LoggerUtil;

/**
 * 
 * @author wurunzhou
 *
 */
public abstract class  APPClientAbs implements WebsocketCoreInterf,FeedbackInterf,WebsocketClientInterf{

	private Logger logger = LoggerUtil.getLogger(this.getClass().getName()); 
	
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
	
	public APPClientAbs(URI url){
		this.coreClient = new WebsocketClientImpl(url,this);
	}

	@Override
	public void connection(int heartbeat) throws IllegalWebsocketException {
		coreClient.connect( heartbeat);
	}

	@Override
	public void connection() throws IllegalWebsocketException {
		coreClient.connect( 1);
	}
	
	@Override
	public void virify(String userKey, String virifyCode, String url)
			throws IOException, ConnectWebsocketException,
			HandshakeWebsocketException {
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
			if(htime++>5) break;
		}
		if(handshakeStatus){
			coreClient.send(userKey+":"+virifyCode+":"+url+tag);
		}else {
			// 如果五秒之后 ，还是没有握手成功，那就等着验证用户抛出异常吧
			logger.log(Level.INFO,"五秒之后 ，还是没有握手成功，那就让验证用户抛出异常吧");
			throw new  HandshakeWebsocketException();
		}
	}

	@Override
	public void sendMsgBinary(ByteBuffer msg, long timeout) {
		byte[] data = ByteBufferSwap.byteBufferToByte(msg);
		coreClient.send(data);
	}

	@Override
	public void sendMsgBinary(List<ByteBuffer> msg, long timeout) {
		for(ByteBuffer bB : msg){
			byte[] data = ByteBufferSwap.byteBufferToByte(bB);
			coreClient.send(data);
		}
	}

	@Override
	public void sendMsgText(String msg, long timeout)
			throws IllegalWebsocketException {
		int htime = 0;
		while(!verifyStatus){
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(htime++>5) break;
		}
		if(verifyStatus){
			coreClient.send(msg);
		}else {
			// 如果五秒之后 ，还是没有握手成功，那就等着验证用户抛出异常吧
			logger.log(Level.INFO,"五秒之后 ，还是没有验证成功，那就抛出异常吧");
			throw new  IllegalWebsocketException();
		}
		
	}
	
//	public void sendPing(int heartbeat){
//		coreClient.sendPing( heartbeat);
//	}

	@Override
	public void close(long timeout) {
		coreClient.close();
	}

	@Override
	public void onWebsocketMessageB(ByteBuffer msg) {
		onMessageB(msg);
		
	}

	@Override
	public void onWebsocketMessageT(String msg)   {
		if(verifyStatus){
			onMessageT(msg);
		}else{
			onVirify(msg,true);
		}
		
		
	}

	@Override
	public void onWebsocketError(Exception e, String info) {
		//coreClient.sendMsgQueue.setPendingStatus(false);
		onError(e,info);
		
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
	public void onVirify(String msg, boolean pass){
		logger.log(Level.INFO,"服务器放回验证信息"+ msg);
		if("ok".equals(msg)){
			verifyStatus = true;
			logger.log(Level.INFO,"验证成功");
		}else{
			logger.log(Level.INFO,"验证bu成功");
		}
		
	}
	
}
