package fy.socket.JavaWebsocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import fy.socket.JavaWebsocket.exception.ConnectWebsocketException;
import fy.socket.JavaWebsocket.exception.HandshakeWebsocketException;
import fy.socket.JavaWebsocket.exception.IllegalWebsocketException;
import fy.socket.JavaWebsocket.service.APPClient;


public class StartClientMain {

	public static void main(String[] args) {

		//new URI("ws://localhost:8887")
		try {
			APPClient client = new APPClient("222.201.139.159", 8877);
			client.connection();
			TimeUnit.SECONDS.sleep(5);
			client.virify("user0", "verify0","homewtb");
			TimeUnit.SECONDS.sleep(10);
			//client.sendMsg(null,10,2);
			client.sendPing(0);
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
		} catch (IllegalWebsocketException e) {
			e.printStackTrace();
		}
		
	}
	
	

}
