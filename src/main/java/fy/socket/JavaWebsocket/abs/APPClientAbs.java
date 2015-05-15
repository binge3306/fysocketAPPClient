package fy.socket.JavaWebsocket.abs;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
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
public abstract class  APPClientAbs implements Runnable,WebsocketCoreInterf,FeedbackInterf,WebsocketClientInterf{

	private Logger logger = LoggerUtil.getLogger(this.getClass().getName()); 
	private WebsocketClientImpl coreClient;
	
	public APPClientAbs(URI url){
		this.coreClient = new WebsocketClientImpl(url,this);
	}

	@Override
	public void connection() throws IllegalWebsocketException {
		coreClient.connect();
	}

	@Override
	public void virify(String userKey, String virifyCode, String url)
			throws IOException, ConnectWebsocketException,
			HandshakeWebsocketException {
		
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
		coreClient.send(msg);
	}

	@Override
	public void close(long timeout) {
		coreClient.close();
	}

	@Override
	public void onWebsocketMessageB(ByteBuffer msg) {
		onMessageB(msg);
		
	}

	@Override
	public void onWebsocketMessageT(String msg) {
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

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
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see fy.socket.JavaWebsocket.interf.FeedbackInterf#onVirify(java.nio.ByteBuffer, boolean)
	 */
	@Override
	public void onVirify(ByteBuffer msg, boolean pass)
			throws VerifyWebsocketException {
		// TODO Auto-generated method stub
		
	}
	
}
