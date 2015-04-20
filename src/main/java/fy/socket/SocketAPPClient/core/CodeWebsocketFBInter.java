package fy.socket.SocketAPPClient.core;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * 
 * 将消息打包为websocket协议，通知连接核心的回调接口
 * add at 20150319
 * @author wurunzhou
 *
 */
public interface CodeWebsocketFBInter {

	/**
	 * 编码回调
	 * <br>
	 * 将消息处理为websocket包，回调给连接核心
	 * <br>
	 * 通过该方法直接IO（发送）
	 * @param websocketMsg	websocket包，可直接发送
	 * @throws IOException  
	 */
	public void encodeFine(ByteBuffer websocketMsg) ;
	
	/**
	 * 解码回调
	 * <br>
	 * 将websocket包解析为消息包
	 * @param msg	消息包，用户可直接理解格式
	 * @throws IOException 
	 */
	public void decodeFine(ByteBuffer msg) ;
	
}
