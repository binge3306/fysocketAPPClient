package fy.socket.SocketAPPClient.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import fy.socket.SocketAPPClient.util.logger.LoggerUtil;

/**
 * 将消息包打包成websocket包
 * add at 20150319
 * @author wurunzhou
 *
 */
public class DeEncodeUtil {
	
	
	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());

	private CodeWebsocketFBInter feedback;
	
	public DeEncodeUtil(CodeWebsocketFBInter feedback){
		this.feedback = feedback;
	}
	
	/**
	 * 编码
	 * <br>
	 * 将消息包打包成websocket包
	 * @param msg
	 */
	public void encodeWebsocket(ByteBuffer msg){
		
		this.feedback.encodeFine(encode(msg,true));
	}
	

	/**
	 * 解码
	 * <br>
	 * 将websocket包解析为消息包
	 * @param msgWebsocket
	 */
	public void decodeWebsocket(ByteBuffer websocketMsg,boolean pass){
		if(!pass){
			// 握手没有完成 ，握手信息不要解密消息
			this.feedback.decodeFine(websocketMsg);
		}else{
			// 用户验证和收发消息需要解密消息（提取消息正文，不要掩码处理）
			this.feedback.decodeFine(decode(websocketMsg));
		}
		
	}
	

	/**
	 * 
	 * 客户端编码
	 * <br>
	 * 将消息内容打包成符合websocket格式
	 * <br>
	 * 内容包括消息头和掩码处理
	 * @param byteBuf	待译码部分
	 * @param finalFragment	是否为最后一个片段
	 * @return
	 */
	private ByteBuffer encode(ByteBuffer byteBuf,boolean finalFragment){
		//byteBuf.flip();
		// 常规来说添加websocket头长度为
		//（一个byte opcode,一个byte playload,两个byte add load 四个byte mask）
		ByteBuffer outs = ByteBuffer.allocate(byteBuf.limit()+8);
		outs.clear();
		// OutputStream out = socket.getOutputStream();
		int first = 0x00;
		// 是否是输出最后的WebSocket响应片段
		if (finalFragment) {
			first = first + 0x80;
			first = first + 0x1;
		}
		outs.put((byte) first);

		if (byteBuf.limit() < 126) {
			outs.put((byte)(byteBuf.limit()+0x80));
		} else if (byteBuf.limit() < 65536) {
			// 126 + 16bit
			outs.put((byte)(126+0x80));
			outs.put((byte)(byteBuf.limit() >>> 8));
			outs.put((byte)(byteBuf.limit() & 0xFF));
		} else {
			// 127 + 64bit
			outs.put((byte)(127+0x80));
			outs.put((byte)0);
			outs.put((byte)0);
			outs.put((byte)0);
			outs.put((byte)0);
			outs.put((byte)(byteBuf.limit() >>> 24));
			outs.put((byte)(byteBuf.limit() >>> 16));
			outs.put((byte)(byteBuf.limit() >>> 8));
			outs.put((byte)(byteBuf.limit() & 0xFF));

		}
		
		byte[] mask = getMask();
		outs.put(mask);
		
		for(int i=0;i<byteBuf.limit();i++){
			outs.put((byte) ((byteBuf.get())^mask[i%4]));
		}

		outs.flip();
		return outs;
	}
	
	private byte[] getMask(){
		byte[] mask = new byte[4];
		mask[0] = 36;
		mask[1] = 67;
		mask[2] = 97;
		mask[3] = 56;
		
		return mask;
	}
	/**
	 * 
	 * 解码实现
	 * <br>
	 * 读取websocket消息，
	 * 将其转化为可以理解的消息包
	 * @param byteBuf	待解码的websocket包
	 * @return
	 */
	private ByteBuffer decode(ByteBuffer websocketMsg){
		
		//System.out.println("p:"+websocketMsg.position()+".li:"+websocketMsg.limit()+".cp:"+websocketMsg.capacity());
		//websocketMsg.flip();
		// 该次读取是否可以读全该消息所有信息
		boolean readOver = true;
		// 消息类型（文本格式，或者是二进制）
		int msgType = 0;
		boolean doMask = false;
		ByteBuffer msgBuf = ByteBuffer.allocate(websocketMsg.limit());
		msgBuf.clear();
		
		/////// 1. 获取第一个byte  判断是否为最后一个片段，是何种类型数据
		byte first = websocketMsg.get();
		//System.out.println("p:"+websocketMsg.position()+".li:"+websocketMsg.limit()+".cp:"+websocketMsg.capacity());
		int b = first & 0xFF;
		// 1为字符数据，8为关闭socket
		
		byte opCode = (byte) (b & 0x0F);

		if ((first & 0x80) == 0) {
			// 未读完
			readOver = false;
		} else {
			// 读完
			readOver = true;
		}
		
		if (opCode == 8) {
			//socketOptions.closeSocket();
			return null;
		} else if (opCode == 1) {
			// 发送数据为文本格式
			msgType = 1;
		} else if (opCode == 2) {
			// 发送数据为二进制格式
			msgType = 2;
		}
		
		if(websocketMsg.hasRemaining()){
			///////2. 获取消息长度
			byte two = websocketMsg.get();
			//System.out.println("p:"+websocketMsg.position()+".li:"+websocketMsg.limit()+".cp:"+websocketMsg.capacity());
			int payloadLength =two & 0x7F;
			if((two &0x80)==0){
				// 第二个字节 第一位为0  不要 掩码处理
				doMask = false;
			}else if((two &0x80)==1){
				// 第二个字节 第一位为1   掩码处理
				doMask = true;
			}

			if (payloadLength == 126) {
				int shift = 0;
				payloadLength = 0;
				
				for (int i = 1; i >= 0; i--) {
					payloadLength = payloadLength
							+ ((websocketMsg.get() & 0xFF) << shift);
					shift += 8;
				}

			} else if (payloadLength == 127) {
				int shift = 0;
				payloadLength = 0;
				for (int i = 7; i >= 0; i--) {
					payloadLength = payloadLength
							+ ((websocketMsg.get() & 0xFF) << shift);
					shift += 8;
				}
			}
			
			////// 3.判断并获取掩码
			if(doMask){
				byte[] mask = new byte[4];
				for(int i=0;i<mask.length;i++){
					mask[i] = websocketMsg.get();
				}
				int ma = 0;
				while(websocketMsg.hasRemaining()){
					// 提取消息体 并
					msgBuf.put((byte) (websocketMsg.get()^mask[ma%4])); ma++;
				}
				
				
			}else{
				// 不要掩码处理，直接提取消息(如果服务器发过来的回复，就不需要掩码处理)
				while(websocketMsg.hasRemaining()){
					// 提取消息体 并
					msgBuf.put(websocketMsg.get()); 
				}
			}
		}
		websocketMsg.clear();
		msgBuf.flip();
		return msgBuf;
	
		
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
