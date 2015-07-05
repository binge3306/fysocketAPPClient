package fy.socket.JavaWebsocket.interf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import fy.socket.JavaWebsocket.exception.ConnectWebsocketException;
import fy.socket.JavaWebsocket.exception.HandshakeWebsocketException;
import fy.socket.JavaWebsocket.exception.IllegalWebsocketException;

/**
 * 提供给APPClient 用于操作fysocket
 * @author wurunzhou
 *
 */
public interface WebsocketClientInterf {

	

	/**
	 * 连接服务器
	 * <br>
	 * 在风控完成之后，用户验证已经交给风控处理，则可以直接连接
	 * @param heartbeat 设置客户端心跳周期
	 * @throws IllegalWebsocketException
	 */
	public void connection(int heartbeat) throws IllegalWebsocketException;
	
	/**
	 * 连接服务器
	 * <br>
	 * 在风控完成之后，用户验证已经交给风控处理，则可以直接连接
	 */
	public void connection() throws IllegalWebsocketException;

	/**
	 * 验证用户
	 * <br> 
	 * 发送用户关键字和验证码
	 * 执行顺序是将用户ID和验证码打包成socketFy 可以解析的固定格式
	 * 解锁待发送队列
	 * 将验证信息放入待发送队列
	 * 将待发送队列上锁
	 * @param userKey 用户ID（或者是用户名）
	 * @param virifyCode 用户验证码（该验证码由吾托帮随机生成）
	 * @throws IOException 
	 * @throws ConnectWebsocketException 
	 * @throws HandshakeWebsocketException 
	 * @throws InterruptedException 
	 */
	public void verify(String userKey,String virifyCode,String url) throws IOException, ConnectWebsocketException, HandshakeWebsocketException, InterruptedException;
	
	/**
	 * 发送二进制消息
	 * <br>
	 * 二进制消息（如截图和语音）
	 * @param msg 二进制消息内容
	 * @param timeout	最长阻塞时间，如果超过该阻塞时间取消发送
	 */
	public void	sendMsgBinary(ByteBuffer msg,long timeout);
	
	/**
	 * 发送二进制大文件
	 * <br>
	 * 发送如压缩文件，文档等比较大的二进制消息 
	 * 将大文件切分为64K大小片段
	 * @param msg  大文件消息列表
	 * @param timeout  最长阻塞时间，如果超过该阻塞时间取消发送
	 */
	public void sendMsgBinary(List<ByteBuffer> msg,long timeout);
	
	/**
	 * 发送文本消息
	 * <br>
	 * 将文本消息首先发到待发送消息队列
	 * 在放进消息队列之前，首先判断消息队列是否还接收消息如果不，则抛出非法操作异常。
	 * @param msg 文本消息内容
	 * @param timeout 最长阻塞时间，如果超过该阻塞时间取消发送
	 * @throws IllegalWebsocketException 非法操作异常，表示待发送队列已经不再接收消息
	 * @throws InterruptedException 
	 */
	public void sendMsgText(String msg,long timeout) throws IllegalWebsocketException, InterruptedException;


	/**
	 * 主动关闭连接
	 * @param timeout 最长阻塞时间，如果超过该阻塞时间，强制关闭
	 */
	public void close(long timeout);

	
}
