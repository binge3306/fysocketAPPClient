package fy.socket.JavaWebsocket.util;

import java.nio.ByteBuffer;

public class ByteBufferSwap {


	/**
	 * bytebuffer 有效内容转byte数组
	 * @param buf
	 * @return
	 */
	public static byte[] byteBufferToByte(ByteBuffer buf){
		// Retrieve bytes between the position and limit
		// (see Putting Bytes into a ByteBuffer)
		byte[] bytes = new byte[buf.remaining()];

		// transfer bytes from this buffer into the given destination array
		buf.get(bytes, 0, bytes.length);
		return bytes;
	}
	/**
	 * 将buffer中有效部分转换为String
	 * @param buf
	 * @return
	 */
	public static String byteBufferToString(ByteBuffer buf){
		buf.position(0);
		return new String(byteBufferToByte(buf));
	}
	
	
	public static ByteBuffer stringToBytebuffer(String msg){
		byte[] msgb = msg.getBytes();
		ByteBuffer msgBuf = ByteBuffer.allocate(msgb.length);
		msgBuf.put(msgb);
		msgBuf.flip();
		
		return msgBuf;
	}
}
