package fy.socket.socketNettyAPPC;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import fy.socket.SocketAPPClient.exception.ConnectWebsocketException;
import fy.socket.SocketAPPClient.exception.HandshakeWebsocketException;
import fy.socket.socketNettyAPPC.service.APPClient;

public class StartMain {

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
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (HandshakeWebsocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}

}
