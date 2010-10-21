package org.cssc.prototpe.net.filters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.cssc.prototpe.http.HttpRequest;
import org.cssc.prototpe.http.exceptions.MissingHostException;
import org.cssc.prototpe.net.Application;
import org.cssc.prototpe.net.filters.application.ApplicationFilter;

public class HttpRequestFilter extends Filter {

	private HttpRequest request;

	public HttpRequestFilter(Socket clientSocket, HttpRequest request) {
		super(clientSocket);
		this.request = request;
	}

	public boolean filter() throws IOException {
		ApplicationFilter filter = Application.getInstance().getApplicationConfiguration().getFilterForCondition(clientSocket.getInetAddress(), request.getHeader().getField("user-agent"));

		System.out.println("Filter: " + filter);
		
		if(filter != null) {
			return applyFilter(filter);
		}

		return false;
	}

	private boolean applyFilter(ApplicationFilter filter) throws IOException {
		boolean allAccessesBlocked = filter.isAllAccessesBlocked();
		List<InetAddress> blockedIPs = filter.getBlockedIPs();
		List<String> blockedURIs = filter.getBlockedURIs();
		
		try {
			if(allAccessesBlocked) {
				writeResponse("src/main/resources/html/errors/accessDenied.html");
				clientSocket.close();
				return true;
			} else if(blockedIPs != null && blockedIPs.contains(InetAddress.getByName(request.getEffectiveHost()))) {
				writeResponse("src/main/resources/html/errors/ipAccessDenied.html");
				clientSocket.close();
				return true;
			} else if(blockedURIs != null) {
				String requestURI = "http://" + request.getEffectiveHost() + request.getEffectivePath();
				
				for(String s: filter.getBlockedURIs()) {
					String regExpURI = s.replace("*", ".*");
					
					if(requestURI.matches(regExpURI)) {
						writeResponse("src/main/resources/html/errors/uriAccessDenied.html");
						clientSocket.close();
						return true;
					}
				}
			}
		} catch(MissingHostException e) {
			e.printStackTrace();
		}
		
		return false;
	}


}
