package fy.socket.JavaWebsocket.exception;

public class CommonWebsocketException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3344793317742212529L;

	private String errorMsg;
	private Integer errorCode;
	
	public CommonWebsocketException(){
		
	}
	
	public CommonWebsocketException(String errorMsg){
		
	}
	
	public CommonWebsocketException(Integer errorCode,String errorMsg){
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	
	public CommonWebsocketException(String message ,Throwable cause){
		super(message, cause);
	}
	
	
}
