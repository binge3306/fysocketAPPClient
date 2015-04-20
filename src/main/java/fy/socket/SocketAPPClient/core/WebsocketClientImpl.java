package fy.socket.SocketAPPClient.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;





import java.util.logging.Level;
import java.util.logging.Logger;

import fy.socket.SocketAPPClient.exception.*;
import fy.socket.SocketAPPClient.util.*;
import fy.socket.SocketAPPClient.util.logger.LoggerUtil;
import fy.socket.SocketAPPClient.core.handshake.*;
import fy.socket.SocketAPPClient.interf.*;

/**
 * 
 * websocket 连接核心实现
 * @author wurunzhou
 *
 */
public class WebsocketClientImpl implements CodeWebsocketFBInter{

	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());
	
	private boolean hsStatus  = false;
	private boolean vfStatus = false;
	private FeedbackInterf feedbackInterf;
	/**
	 * 待发送消息队列
	 */
	public  SendMsgQueue sendMsgQueue  ;
	/**
	 * 待处理消息队列
	 */
	public  ReceiveMsgQueue receiveMsgQueue ;
	
	public WebsocketClientImpl(FeedbackInterf feedbackInterf){
		this.feedbackInterf = feedbackInterf;
		sendMsgQueue = new SendMsgQueue(5);
		receiveMsgQueue = new ReceiveMsgQueue(5);
	}
	
	public WebsocketClientImpl(FeedbackInterf feedbackInterf,boolean handshakeStatus,boolean virifyStatus){
		this.feedbackInterf = feedbackInterf;
		sendMsgQueue = new SendMsgQueue(5);
		receiveMsgQueue = new ReceiveMsgQueue(5);
		
	}
	

	/**
	 * 客户端读取IO消息
	 * 解码之后，提示给用户
	 * @param websocketmsg 从IO读取的websocket包
	 */
	public void  processMsgT(ByteBuffer websocketmsg,boolean handpass){
//		logger.log(Level.INFO,""+ByteBufferSwap.byteBufferToString(msg));
		ByteBuffer rmsg = ByteBuffer.allocate(websocketmsg.remaining());
		rmsg.put(websocketmsg);
		rmsg.flip();
		if(!handpass){
			// 握手回复 不要解密
			new DeEncodeUtil(this).decodeWebsocket(rmsg,handpass);
		}else{
			// 用户验证和收消息 需要解密一下
			new DeEncodeUtil(this).decodeWebsocket(rmsg,handpass);
		}
		
		
	}

	/**
	 * 提示用户有新消息
	 */
	public void readMsgT(ByteBuffer msg){

		feedbackInterf.onMessageT(msg);

		
	}

	@Override
	public void encodeFine(ByteBuffer websocketMsg) {
		
	}

	@Override
	public void decodeFine(ByteBuffer msg) {
		
		if(!hsStatus){
			try {
				// 如果是握手返回信息
				logger.log(Level.INFO,"握手返回信息收到,大小："+msg.limit());
				feedbackInterf.onHandshake(getAccessKey(dealHandshake(msg)));
			} catch (HandshakeWebsocketException e) {
				e.printStackTrace();
			}
		}else if(!vfStatus){
			// 用户验证信息返回
			try {
				logger.log(Level.INFO,"用户验证返回信息收到，大小："+msg.limit()+ ",内容为："+ByteBufferSwap.byteBufferToString(msg));
				feedbackInterf.onVirify(msg,true);
			} catch (VerifyWebsocketException e) {
				e.printStackTrace();
			}
		}else{
			// 普通消息
			logger.log(Level.INFO,"收到消息包，大小："+msg.limit()+ ",内容为："+ByteBufferSwap.byteBufferToString(msg));
			readMsgT(msg);
		}
		
	}
	
	private HandshakeBuilder dealHandshake(ByteBuffer buf) throws HandshakeWebsocketException{

		HandshakeBuilder handshake = new HandshakedataImpl1();
		ByteBuffer src = ByteBuffer.allocate(buf.limit());
		src.put(buf);
		src.flip();
		byte[] tmp = ByteBufferSwap.byteBufferToByte(src);
		logger.log(Level.INFO,src.limit()+"");
		logger.log(Level.INFO,new String(tmp));
		buf.position(0);
		String line = readStringLine( buf );
		if( line == null )
			throw new HandshakeWebsocketException();

		String[] firstLineTokens = line.split( " ", 3 );// eg. HTTP/1.1 101 Switching the Protocols
		if( firstLineTokens.length != 3 ) {
			throw new  HandshakeWebsocketException();
		}

		line = readStringLine( buf );
		while ( line != null && line.length() > 0 ) {
			String[] pair = line.split( ":", 2 );
			if( pair.length != 2 )
				throw new HandshakeWebsocketException();;
			handshake.put( pair[ 0 ], pair[ 1 ].replaceFirst( "^ +", "" ) );
			line = readStringLine( buf );
		}
		if( line == null )
			throw new  HandshakeWebsocketException();;
		return handshake;
	
	}
	
	
	private String getAccessKey(Handshakedata response){

		return response.getFieldValue( "Sec-WebSocket-Accept" );

	}
	
	
	private  String readStringLine( ByteBuffer buf ) {
		ByteBuffer b = readLine( buf );
		return b == null ? null : stringAscii( b.array(), 0, b.limit() );
	}
	
	private  ByteBuffer readLine( ByteBuffer buf ) {
		ByteBuffer sbuf = ByteBuffer.allocate( buf.remaining() );
		byte prev = '0';
		byte cur = '0';
		while ( buf.hasRemaining() ) {
			prev = cur;
			cur = buf.get();
			sbuf.put( cur );
			if( prev == (byte) '\r' && cur == (byte) '\n' ) {
				sbuf.limit( sbuf.position() - 2 );
				sbuf.position( 0 );
				return sbuf;

			}
		}
		// ensure that there wont be any bytes skipped
		buf.position( buf.position() - sbuf.position() );
		return null;
	}
	
	private  String stringAscii( byte[] bytes, int offset, int length ){
		try {
			return new String( bytes, offset, length, "ASCII" );
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
	}

	public boolean isHsStatus() {
		return hsStatus;
	}

	public void setHsStatus(boolean hsStatus) {
		this.hsStatus = hsStatus;
	}

	public boolean isVfStatus() {
		return vfStatus;
	}

	public void setVfStatus(boolean vfStatus) {
		this.vfStatus = vfStatus;
	}

	
	
}
