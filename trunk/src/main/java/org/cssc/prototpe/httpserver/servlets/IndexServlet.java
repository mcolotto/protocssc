package org.cssc.prototpe.httpserver.servlets;

import java.io.IOException;

import org.cssc.prototpe.http.HttpRequest;
import org.cssc.prototpe.httpserver.model.Authorization;
import org.cssc.prototpe.httpserver.model.HttpServletResponse;
import org.cssc.prototpe.httpserver.model.MyHttpServlet;
import org.cssc.prototpe.net.Application;
import org.cssc.prototpe.net.ApplicationConfiguration;

public class IndexServlet extends MyHttpServlet {

	@Override
	public void doGet(HttpRequest request, HttpServletResponse response)
	throws IOException {
		//Must implement a way of getting url GET parameters
		ApplicationConfiguration configuration = Application.getInstance().getApplicationConfiguration();
		
		Authorization auth = getAuthorization(request);;
		if( auth == null ){
			response.unauthorize();
			return;
		}
		
		System.out.println(auth);

		StringBuffer buf = response.getBuffer();
		buf.append("<html><body>");

		buf.append("<h1>CSSC Proxy Server Configuration</h1>");
		
		buf.append("Welcome, " + auth.getUsername());
		buf.append("<hr/>");

		buf.append("<h2>Configuration parameters</h2>");
		buf.append("<table>");
		
		buf.append("<tr>");
		buf.append("<th>Parameter</th>");
		buf.append("<th>Current value</th>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td>Thread pool size</td>");
		buf.append("<td>" + configuration.getThreadPoolSize() + "</td>");
		buf.append("</tr>");

		buf.append("<tr>");
		buf.append("<td>Chaining proxy address</td>");
		buf.append("<td>" + configuration.getChainingProxyAddress() + "</td>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td>Chaining proxy port</td>");
		buf.append("<td>" + configuration.getChainingProxyPort() + "</td>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td>Maximum server persistent connections</td>");
		buf.append("<td>" + configuration.getMaxPersistantServerConnections() + "</td>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td>Maximum server persistent connections per server</td>");
		buf.append("<td>" + configuration.getMaxPersistantServerConnectionsPerServer() + "</td>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td>Client persistent connection timeout</td>");
		buf.append("<td>" + configuration.getClientKeepAliveTimeout() + "</td>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td>Server persistent connection timeout</td>");
		buf.append("<td>" + configuration.getServerConnectionPersistentTimeout() + "</td>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td>Logging file name</td>");
		buf.append("<td>" + configuration.getLoggingFileName() + "</td>");
		buf.append("</tr>");
		
		
		buf.append("<form action=\"configure\" method=\"post\" enctype=\"multipart/form-data\">");
		
		buf.append("<tr>");
		buf.append("<td>New configuration:</td>");
		buf.append("<td> <input type=\"file\" name=\"configuration\"/></td>");
		buf.append("</tr>");
		
		buf.append("<tr>");
		buf.append("<td></td>");
		buf.append("<td><input type=\"submit\" value=\"Upload configuration\" /></td>");
		buf.append("</tr>");
		
		buf.append("</form>");

		buf.append("</table>");
		
		buf.append("<br/>");
		

		buf.append("</body></html>");

	}

	@Override
	public void doPost(HttpRequest request, HttpServletResponse response)
	throws IOException {

	}

}