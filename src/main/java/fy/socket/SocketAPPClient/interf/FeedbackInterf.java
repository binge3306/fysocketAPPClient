package fy.socket.SocketAPPClient.interf;

import java.nio.ByteBuffer;
import fy.socket.SocketAPPClient.exception.VerifyWebsocketException;


public interface FeedbackInterf {

	
	
	/**
	 * 接收二进制消息
	 * @param msg
	 */
	public  void onMessageB(ByteBuffer msg);
	
	/**
	 * 接收文本消息
	 * @param msg 文件消息内容
	 */
	public  void onMessageT(ByteBuffer msg);
	
	/**
	 * 读取和写入异常		
	 * @param e 异常
	 * @param info 异常的补充消息
	 */
	public  void onError(Exception e,String info);
	
	/**
	 * 异常关闭
	 * @param e 异常
	 * @param info 异常的补充消息
	 */
	public  void onClose(Exception e,String info);
	
	
	/**
	 * 获取握手信息
	 * <br>
	 * 返回服务器发送access
	 * @throws IllegalWebsocketException 
	 */
	public void onHandshake(String access) ;
	
	/**
	 * 用户验证结果
	 * @throws VerifyWebsocketException 
	 * @throws IllegalWebsocketException 
	 */
	public void onVirify(ByteBuffer msg,boolean pass) throws VerifyWebsocketException ;
	

}
