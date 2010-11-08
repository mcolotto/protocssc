package org.cssc.prototpe.configuration.filters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.cssc.prototpe.http.HttpRequest;
import org.cssc.prototpe.http.exceptions.MissingHostException;
import org.cssc.prototpe.net.Application;
import org.cssc.prototpe.net.Logger;

public class HttpRequestFilter extends Filter {

	private HttpRequest request;

	public HttpRequestFilter(Socket clientSocket, HttpRequest request, Logger logger) {
		super(clientSocket, Application.getInstance().getApplicationConfiguration().getFilterForCondition(clientSocket.getInetAddress(), request.getHeader().getField("user-agent")), logger);
		this.request = request;
	}

	private boolean matchesBlockedIP(List<String> blockedIPs, InetAddress currentIP){
		String currentIPStr = currentIP.toString();
		String currentIPStrClean = currentIPStr.substring(currentIPStr.indexOf("/"));
		for(String ip: blockedIPs){
			System.out.println(currentIPStrClean);
			System.out.println("/" + ip);
			
			if( currentIPStrClean.matches("/" + ip)){
				System.out.println("Match!");
				return true;
			}
		}
		return false;
	}
	
	public boolean filter() throws IOException {
		if(filter == null) {
			return false;
		}
		
		boolean allAccessesBlocked = filter.isAllAccessesBlocked();
		List<String> blockedIPs = filter.getBlockedIPs();
		List<String> blockedURIs = filter.getBlockedURIs();
		
		try {
			if(allAccessesBlocked) {
				writeResponse("src/main/resources/html/errors/accessDenied.html");
				Application.getInstance().getMonitoringService().registerWholeBlock();
				return true;
			} else if(blockedIPs != null && matchesBlockedIP(blockedIPs, InetAddress.getByName(request.getEffectiveHost()))) {
				writeResponse("src/main/resources/html/errors/ipAccessDenied.html");
				Application.getInstance().getMonitoringService().registerIpBlock();
				return true;
			} else if(blockedURIs != null) {
				String requestUri = "http://" + request.getEffectiveHost() + request.getEffectivePath();
				
				for(String s: filter.getBlockedURIs()) {
					if(uriMatchesExpression(requestUri, s)) {
						writeResponse("src/main/resources/html/errors/uriAccessDenied.html");
						Application.getInstance().getMonitoringService().registerUriBlock();
						return true;
					}
				}
			}
		} catch(MissingHostException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	private boolean uriMatchesExpression(String uri, String expression) {
		if(expression.startsWith("http://")) {
			expression = expression.substring(7);
		}
		
		if(!expression.startsWith("www.")) {
			expression = "www." + expression;
		}
		
		expression = "http://" + expression;
		
		String regExp = expression;
		if(uri.charAt(uri.length() - 1) == '/' && regExp.charAt(regExp.length() - 1) != '/') {
			int index = expression.lastIndexOf('*');
			
			if(index != -1) {
				char character = expression.charAt(index - 1);
				
				if(character != '/') {
					regExp = regExp + "/";
				}
			} else {
				regExp = regExp + "/";
			}
		}
		
		regExp = regExp.replace(".", "\\.");
		regExp = regExp.replace("*", ".*");
		
		return uri.matches(regExp);
	}


}
