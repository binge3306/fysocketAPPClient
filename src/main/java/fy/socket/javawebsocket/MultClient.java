package fy.socket.javawebsocket;

import java.net.URISyntaxException;
import java.util.concurrent.Phaser;
import java.util.logging.Logger;

import fy.socket.SocketAPPClient.util.logger.LoggerUtil;
import fy.socket.javawebsocket.core.ClientConnect;

public class MultClient {

	private final String HOST = "ws://222.201.139.159:8877";
	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());
	
	private final static int StaticNum = 100;

	Phaser phaser=new Phaser();
	
	public void start0(){
		int i = 0;
		while(i<StaticNum){
			new Thread(new ClientConnect(HOST,phaser,i)).start();
		}
	}

}
