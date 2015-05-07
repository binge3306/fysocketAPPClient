package fy.socket.javawebsocket.core;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import fy.socket.SocketAPPClient.exception.IllegalWebsocketException;
import fy.socket.SocketAPPClient.util.ByteBufferSwap;
import fy.socket.SocketAPPClient.util.logger.LoggerUtil;

public class WebsocketAppClient extends WebSocketClient {

	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());
	
	public WebsocketAppClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}
	
	public WebsocketAppClient(URI serverUri){
		super(serverUri);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		logger.log(Level.INFO,"You are connected to ChatServer: " + getURI()  );
	}

	@Override
	public void onMessage(String message) {
		logger.log(Level.INFO,"got: " + message  );
	}
	
	@Override
	public void onMessage(ByteBuffer bytes) {
		
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		logger.log(Level.INFO,"You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason );
	}

	@Override
	public void onError(Exception ex) {
		logger.log(Level.INFO, "Exception occured ...\n" + ex );
	}

	public void verifyUser(String username,String verifyCode,String url){
		send(username+"##"+verifyCode+"##"+url);
	}
	

	public void sendMsg(String msg ,int times ,int timeout){
		//ByteBuffer msgb = ByteBuffer.allocate(10);
		if(msg == null||msg == ""){
			// chatId##isPublice##msgContent
			msg = "20840##0##appclient msg";
			// wurunzhou 记得又该为 抛出异常
		}
		String msgSend = msg;
		while(times >= 0){
			times --;
			msgSend += " ，sendtimes= "+times;
			
			try {
				send(msgSend);
//				sendMsgText(ByteBufferSwap.stringToBytebuffer(msgSend), 0);
				if(timeout>0)
					TimeUnit.SECONDS.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			msgSend = msg;
		}
	}
}
