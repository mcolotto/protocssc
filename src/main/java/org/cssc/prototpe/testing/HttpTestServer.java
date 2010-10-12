package org.cssc.prototpe.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.cssc.prototpe.http.HttpRequest;
import org.cssc.prototpe.parsers.HttpRequestParser;

public class HttpTestServer {

	private ServerSocketChannel serverSocket;

	public HttpTestServer(int port) {
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.socket().bind(new InetSocketAddress(port));
			
			System.out.println("Listening on " + InetAddress.getLocalHost() + ":" + port);
			
			while(true) {
				SocketChannel socket = serverSocket.accept();
//				ByteBuffer buffer = ByteBuffer.allocate(1000);
//				socket.read(buffer);
//				print(buffer.array());
				
				HttpRequestParser parser = new HttpRequestParser(socket.socket().getInputStream());
				parser.parse();
				
				HttpRequest req = parser.getParsedRequest();
				
				System.out.println("Parsed request:");
				System.out.println("Method: " + req.getMethod());
				System.out.println("Path: " + req.getPath());
				System.out.println("Version: " + req.getVersion());
				
				Map<String, String> contentMap = req.getHeader().getContentMap();
				for(String key: contentMap.keySet()) {
					System.out.println("Field: \"" + key + "\"" + " - Value: \"" + contentMap.get(key) + "\"");
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void print(byte[] buffer){
		byte last = 0;
		
		System.out.print("\"");
		for( int i = 0; i < buffer.length && buffer[i] != 0; i++){
			System.out.print((char)buffer[i]);
		}
		System.out.print("\"");
	}

	public static void main(String[] args) {
		System.out.println("Started HTTP test server...");
		new HttpTestServer(8080);
	}
}