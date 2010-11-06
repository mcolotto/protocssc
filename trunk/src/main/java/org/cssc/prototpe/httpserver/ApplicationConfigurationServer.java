package org.cssc.prototpe.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.cssc.prototpe.http.HttpHeader;
import org.cssc.prototpe.http.HttpMethod;
import org.cssc.prototpe.http.HttpRequest;
import org.cssc.prototpe.http.HttpResponse;
import org.cssc.prototpe.http.HttpResponseCode;
import org.cssc.prototpe.httpserver.model.HttpServletResponse;
import org.cssc.prototpe.httpserver.model.MyHttpServlet;
import org.cssc.prototpe.httpserver.servlets.ConfigurationServlet;
import org.cssc.prototpe.httpserver.servlets.IndexServlet;
import org.cssc.prototpe.httpserver.servlets.LoginServlet;
import org.cssc.prototpe.net.exceptions.FatalException;
import org.cssc.prototpe.parsers.HttpRequestParser;

public class ApplicationConfigurationServer implements Runnable{

	private void mapURLs(){
		urlMapping.put("/", IndexServlet.class);
		urlMapping.put("/login", LoginServlet.class);
		urlMapping.put("/configure", ConfigurationServlet.class);
	}
	
	
	ServerSocket serverSocket;
	Map<String, Class<? extends MyHttpServlet>> urlMapping;

	public ApplicationConfigurationServer(int port){
		urlMapping = new HashMap<String, Class<? extends MyHttpServlet>>();
		
		mapURLs();
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new FatalException(e);
		}
		
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		
		Socket socket;
		
		while(true){
			try {
				socket = serverSocket.accept();
				HttpRequestParser reqparser = new HttpRequestParser(socket.getInputStream());
				HttpRequest request = reqparser.parse();
				
				Class<? extends MyHttpServlet> servletClass = urlMapping.get(request.getEffectivePath());
				if( servletClass == null ){
					notFound(socket);
					continue;
				}
				MyHttpServlet servlet = servletClass.newInstance();
				
				servlet.setResponse(new HttpServletResponse());
				
				if( HttpMethod.GET.equals(request.getMethod()) || HttpMethod.HEAD.equals(request.getMethod())){
					servlet.doGet(request, servlet.getResponse());
				} else if( HttpMethod.POST.equals(request.getMethod())){
					servlet.doPost(request, servlet.getResponse());
				} else {
					methodNotAllowed(socket);
					continue;
				}
				
				HttpServletResponse response = servlet.getResponse();
				if( !servlet.setResponse()){
					unAuthorized(socket);
					continue;
				}
				
				socket.getOutputStream().write(response.getActualResponse().toString().getBytes(Charset.forName("US-ASCII")));
				if( !request.getMethod().equals(HttpMethod.HEAD)){
					socket.getOutputStream().write(response.getBuffer().toString().getBytes(Charset.forName("US-ASCII")));
				}
				socket.getOutputStream().flush();
				socket.close();
					
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void unAuthorized(Socket socket) throws IOException{
		HttpHeader header = new HttpHeader();
		header.setField("connection", "close");
		header.setField("WWW-Authenticate", "Basic realm=\"Secure area\"");
		HttpResponse response = new HttpResponse("1.1", header, HttpResponseCode.UNAUTHORIZED, "Authorization Required", new byte[0]);
		socket.getOutputStream().write(response.toString().getBytes(Charset.forName("US-ASCII")));
		socket.getOutputStream().flush();
		socket.close();
	}
	
	private void notFound(Socket socket) throws IOException{
		HttpHeader header = new HttpHeader();
		header.setField("connection", "close");
		HttpResponse response = HttpResponse.emptyResponse(HttpResponseCode.NOT_FOUND);
		socket.getOutputStream().write(response.toString().getBytes(Charset.forName("US-ASCII")));
		socket.getOutputStream().flush();
		socket.close();
	}
	
	private void methodNotAllowed(Socket socket) throws IOException {
		HttpHeader header = new HttpHeader();
		header.setField("connection", "close");
		HttpResponse response = HttpResponse.emptyResponse(HttpResponseCode.METHOD_NOT_ALLOWED);
		socket.getOutputStream().write(response.toString().getBytes(Charset.forName("US-ASCII")));
		socket.getOutputStream().flush();
		socket.close();
	}

	
}