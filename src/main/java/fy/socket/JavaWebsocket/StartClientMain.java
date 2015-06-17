package fy.socket.JavaWebsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import javax.swing.InputMap;

import fy.socket.JavaWebsocket.exception.ConnectWebsocketException;
import fy.socket.JavaWebsocket.exception.HandshakeWebsocketException;
import fy.socket.JavaWebsocket.exception.IllegalWebsocketException;
import fy.socket.JavaWebsocket.service.APPClient;


public class StartClientMain {

	public static void main(String[] args) {

		//new URI("ws://localhost:8887")
		try {
			APPClient client = new APPClient("222.201.139.159", 8877);
			client.connection(1);
			TimeUnit.SECONDS.sleep(5);
			
			client.verify("user3", "verify3","homewtb");

			TimeUnit.SECONDS.sleep(10);
			//client.sendMsg(null,10,2);
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			
			while(true){
				String command = console.readLine();
				if(command == null){
					break;
				}else if("bye".equals(command)){
					client.close(0);
					break;
				}else if("hello".equals(command)){
					client.sendMsgText("chatroom1##1##content:hello,senderAccount\":\"user3\",\"chatview:chatroom1",0);
				}else {
					client.sendMsgText(command,0);
				}
			}
			
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
