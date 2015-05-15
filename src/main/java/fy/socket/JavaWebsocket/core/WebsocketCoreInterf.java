package fy.socket.JavaWebsocket.core;

import java.nio.ByteBuffer;

import fy.socket.JavaWebsocket.exception.VerifyWebsocketException;

public interface WebsocketCoreInterf {

	/**
	 * 接收二进制消息
	 * @param msg
	 */
	public void onWebsocketMessageB(ByteBuffer msg);
	
	/**
	 * 接收文本消息
	 * @param msg 文件消息内容
	 * @throws VerifyWebsocketException 
	 */
	public void onWebsocketMessageT(String msg)  ;
	
	/**
	 * 读取和写入异常		
	 * @param e 异常
	 * @param info 异常的补充消息
	 */
	public void onWebsocketError(Exception e,String info);
	
	/**
	 * 异常关闭
	 * @param e 异常
	 * @param info 异常的补充消息
	 */
	public void onWebsocketClose(Exception e,String info);
	
	/**
	 * 获取服务器发送握手信息
	 * <br>
	 * access 实际无用户
	 * @throws IllegalWebsocketException 
	 */
	public void onHandshake(String access) ;
	
	/**
	 * 用户验证结果
	 * @throws VerifyWebsocketException 
	 * @throws IllegalWebsocketException 
	 */
	public void onVirify(String msg,boolean pass) throws VerifyWebsocketException ;
}
