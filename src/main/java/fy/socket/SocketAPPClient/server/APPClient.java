package fy.socket.SocketAPPClient.server;

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

import fy.socket.SocketAPPClient.abs.APPClientAbs;
import fy.socket.SocketAPPClient.exception.IllegalWebsocketException;
import fy.socket.SocketAPPClient.util.ByteBufferSwap;
import fy.socket.SocketAPPClient.util.logger.LoggerUtil;


public class APPClient extends APPClientAbs{

	
	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());
	
	
	public APPClient(String host,int port) throws URISyntaxException {
		super(new URI("ws://"+host+":"+port));
	}

	@Override
	public void onMessageB(ByteBuffer msg) {
		logger.log(Level.INFO, "收到一条二进制消息");
	}

	@Override
	public void onMessageT(ByteBuffer msg) {
		logger.log(Level.INFO, "收到一条文本消息"+ByteBufferSwap.byteBufferToString(msg));
	}

	@Override
	public void onError(Exception e, String info) {
		logger.log(Level.INFO, "接收过程发生错误 :"+info);
		
	}

	@Override
	public void onClose(Exception e, String info) {
		logger.log(Level.INFO, "通道关闭 ="+info);
		
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
			msg = "20840##0##appclient msg";
		}
		String msgSend = msg;
		while(times >= 0){
			times --;
			msgSend += " = "+times;
			
			try {
				sendMsgText(ByteBufferSwap.stringToBytebuffer(msgSend), 0);
				TimeUnit.SECONDS.sleep(timeout);
			} catch (IllegalWebsocketException  e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			msgSend = msg;
		}
	}
	


}
