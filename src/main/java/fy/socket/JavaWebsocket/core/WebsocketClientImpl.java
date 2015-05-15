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
	
	private boolean hsStatus  = false;
	private boolean vfStatus = false;
	private FeedbackInterf feedbackInterf;
	/**
	 * 待发送消息队列
	 */
	public  SendMsgQueue sendMsgQueue  ;


	
	public WebsocketClientImpl(URI serverUri, Draft draft,FeedbackInterf feedbackInterf) {
		super(serverUri, draft);
		this.feedbackInterf = feedbackInterf;
	}

	public WebsocketClientImpl(URI serverURI,FeedbackInterf feedbackInterf) {
		super(serverURI);
		this.feedbackInterf = feedbackInterf;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		
	}

	@Override
	public void onMessage(String message) {
		feedbackInterf.onMessageT(message);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		feedbackInterf.onClose(new CloseWebsocketException(), "code="+code+",reason="+reason+",remote="+remote);
		
	}

	@Override
	public void onError(Exception ex) {
		feedbackInterf.onError(ex, "java-websocket1.3 捕获异常");
		
	}
	
	class WriteThread implements Runnable{

		@Override
		public void run() {
			
		}
		
	} 
}
