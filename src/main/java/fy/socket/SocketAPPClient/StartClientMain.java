package fy.socket.SocketAPPClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import fy.socket.SocketAPPClient.exception.*;
import fy.socket.SocketAPPClient.service.*;


public class StartClientMain {

	public static void main(String[] args) {

		//new URI("ws://localhost:8887")
		try {
			APPClient client = new APPClient("localhost", 8877);
			client.connection();
			TimeUnit.SECONDS.sleep(5);
			client.virify("user3", "verify3","homewtb");
			TimeUnit.SECONDS.sleep(10);
			client.sendMsg(null,100,20);
		} catch (ConnectWebsocketException   e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HandshakeWebsocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}
