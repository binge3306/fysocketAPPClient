package fy.socket.JavaWebsocket.core;

import java.nio.ByteBuffer;

public interface WebsocketCoreInterf {

	/**
	 * 接收二进制消息
	 * @param msg
	 */
	public void onWebsocketMessageB(ByteBuffer msg);
	
	/**
	 * 接收文本消息
	 * @param msg 文件消息内容
	 */
	public void onWebsocketMessageT(String msg);
	
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
}
