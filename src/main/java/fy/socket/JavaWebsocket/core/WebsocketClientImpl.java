package fy.socket.JavaWebsocket.core;

import java.net.URI;
import java.util.logging.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import fy.socket.JavaWebsocket.exception.CloseWebsocketException;
import fy.socket.JavaWebsocket.interf.FeedbackInterf;
import fy.socket.JavaWebsocket.util.ReceiveMsgQueue;
import fy.socket.JavaWebsocket.util.SendMsgQueue;
import fy.socket.JavaWebsocket.util.logger.LoggerUtil;


/**
 * @author Bryan-zhou
 * @date 2015年5月15日下午3:03:38
 **/
public class WebsocketClientImpl extends WebSocketClient {

	
	
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


	
	public WebsocketClientImpl(URI serverUri, Draft draft,WebsocketCoreInterf wCoreInterf) {
		super(serverUri, draft);
		this.wCoreInterf = wCoreInterf;
	}

	public WebsocketClientImpl(URI serverURI,WebsocketCoreInterf wCoreInterf) {
		super(serverURI);
		this.wCoreInterf = wCoreInterf;
	}

	/**
	 * 握手成功
	 * <br>
	 * 修改握手状态为true，准备用户验证
	 */
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		hsStatus = true;
		wCoreInterf.onHandshake("ok");
	}

	@Override
	public void onMessage(String message) {
		wCoreInterf.onWebsocketMessageT(message);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		wCoreInterf.onWebsocketClose(new CloseWebsocketException(), "code="+code+",reason="+reason+",remote="+remote);
	}

	@Override
	public void onError(Exception ex) {
		wCoreInterf.onWebsocketError(ex, "java-websocket1.3 捕获异常");
	}
	
	public void sendMsgQT(String msg){
		
	}
	
	public void sendMsgQB(){
		
	}
	
	class WriteThread implements Runnable{

		@Override
		public void run() {
			
		}
		
	} 
}
