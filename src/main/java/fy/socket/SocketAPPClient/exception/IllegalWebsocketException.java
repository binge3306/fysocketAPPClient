package fy.socket.SocketAPPClient.exception;

public class IllegalWebsocketException extends CommonWebsocketException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8363965506256021935L;
	private String errorMsg;
	private Integer errorCode;
	
	public IllegalWebsocketException(){
		
	}
	
	public IllegalWebsocketException(String errorMsg){
		
	}
	
	public IllegalWebsocketException(Integer errorCode,String errorMsg){
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	
	public IllegalWebsocketException(String message ,Throwable cause){
		super(message, cause);
	}
	
}
