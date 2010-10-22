package org.cssc.prototpe.configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cssc.prototpe.configuration.filters.application.ApplicationFilter;
import org.cssc.prototpe.configuration.filters.application.FilterCondition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ConfigurationManager {
	
	private static ConfigurationManager instance;
	
	private ConfigurationManager(){
		try {
			parse();
		} catch (Exception e) {
			//mal formado el XML que hacer ??
		}
	}
	
	public static ConfigurationManager getInstance(){
		if(instance==null){
			instance = new ConfigurationManager();
		}
		return instance;
	}
	private void parse() throws SAXException, IOException{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		FilterCondition condition = null;
		ApplicationFilter filter = null;
		List<InetAddress> originIPs = null;
		String browser = null;
		String oS = null;
		boolean blockAllAccesses=false;
		List<InetAddress> blockedIPs = null;
		List<String> blockedURIs = null;
		List<String> blockedMediaTypes = null;
		double maxContentLength=0;
		boolean l33tTransform = false;
		boolean rotateImages = false;
	    try {
	    	DocumentBuilder builder = factory.newDocumentBuilder();
	    	Document document = builder.parse( System.getProperty("user.dir")+"\\config.xml");
	    	//for validating purposes only
	    	factory.setValidating(true);
	    	//for namespace awareness
	    	factory.setNamespaceAware(true);

	    	for(int p=0; p<document.getChildNodes().getLength(); p++){
	    		if(document.getChildNodes().item(p).getNodeName().equals("config")){
	    			NodeList configs = document.getChildNodes().item(p).getChildNodes();
	
			    	for(int l=0; l<configs.getLength(); l++){
			    		if(configs.item(l).getNodeName().equals("filters")){
			    			NodeList filtersNodeList = configs.item(l).getChildNodes();
			    			for(int n=0; n<filtersNodeList.getLength(); n++){
			    	    		if(filtersNodeList.item(n).getNodeName().equals("filter")){
				    				NodeList filterConf = filtersNodeList.item(n).getChildNodes();
				    				for(int conf=0; conf<filterConf.getLength(); conf++){
				    					if(filterConf.item(conf).getNodeName().equals("conditions")){
				    						NodeList conditions = filterConf.item(conf).getChildNodes();
				    				    	for(int i=0; i<conditions.getLength(); i++){ //por cada condition
				    				    		Node currNode = conditions.item(i);
				    				    		if(currNode.getNodeName().equals("origin-IPs")){
				    				    			originIPs = new LinkedList<InetAddress>();
				    				    			 NodeList ips = currNode.getChildNodes();
				    				    			 for(int j=0; j<ips.getLength(); j++){
				    				    				 if(ips.item(j).getNodeName().equals("IP")){
				    				    					 try {
				    											IP ip = new IP(ips.item(j).getChildNodes().item(0).getTextContent());
				    											originIPs.add(ip.getInetAddress());
				    				    					 } catch (Exception e) {
				    											break; //lo salteo si el ip es invalido
				    				    					 }
				    				    				 }
				    				    			 }
				    				    		}
				    				    		else if(currNode.getNodeName().equals("browser")){
				    				    			browser=currNode.getChildNodes().item(0).getTextContent();
				    				    		}
				    				    		else if(currNode.getNodeName().equals("OS")){
				    				    			oS=currNode.getChildNodes().item(0).getTextContent();
				    				    		}
				    				    	}
				    					}
				    					else if(filterConf.item(conf).getNodeName().equals("actions")){
				    						NodeList actionsNodeList = filterConf.item(conf).getChildNodes();
				    				    	for(int i=0; i<actionsNodeList.getLength(); i++){ //por cada config
				    				    		Node currNode = actionsNodeList.item(i);
				    				    		if(currNode.getNodeName().equals("block-all-accesses")){
				    				    			blockAllAccesses=currNode.getChildNodes().item(0).getTextContent().equalsIgnoreCase("true")?true:false;
				    				    		}
				    				    		else if(currNode.getNodeName().equals("blocked-IPs")){
				    				    			blockedIPs = new LinkedList<InetAddress>();
				    				    			 NodeList ips = currNode.getChildNodes();
				    				    			 for(int j=0; j<ips.getLength(); j++){
				    				    				 if(ips.item(j).getNodeName().equals("IP")){
				    				    					 try {
				    											IP ip = new IP(ips.item(j).getChildNodes().item(0).getTextContent());
				    											blockedIPs.add(ip.getInetAddress());
				    				    					 } catch (Exception e) {
				    											break; //lo salteo si el ip es invalido
				    				    					 }
				    				    				 }
				    				    			 }
				    				    		}
				    				    		else if(currNode.getNodeName().equals("blocked-URIs")){
				    				    			blockedURIs = new LinkedList<String>();
				    				    			 NodeList uris = currNode.getChildNodes();
				    				    			 for(int j=0; j<uris.getLength(); j++){
				    				    				 if(uris.item(j).getNodeName().equals("URI")){
				    			    						 blockedURIs.add(uris.item(j).getChildNodes().item(0).getTextContent());
				    				    				 }
				    				    			 }
				    				    		}
				    				    		else if(currNode.getNodeName().equals("blocked-MediaTypes")){
				    				    			blockedMediaTypes = new LinkedList<String>();
				    				    			 NodeList mediaTypes = currNode.getChildNodes();
				    				    			 for(int j=0; j<mediaTypes.getLength(); j++){
				    				    				 if(mediaTypes.item(j).getNodeName().equals("MediaType")){
				    				    					 blockedMediaTypes.add(mediaTypes.item(j).getChildNodes().item(0).getTextContent());
				    				    				 }
				    				    			 }
				    				    		}
				    				    		else if(currNode.getNodeName().equals("max-content-length")){
				    				    			try{
				    				    				maxContentLength=Double.parseDouble(currNode.getChildNodes().item(0).getTextContent());
				    				    			}catch (NumberFormatException e) {
				    				    				//si no es un numero (double), lo salteo y lo pongo en 0 (default)
				    				    				maxContentLength=0;
				    								}
				    				    		}
				    				    		else if(currNode.getNodeName().equals("transform")){
				    				    			 NodeList transforms = currNode.getChildNodes();
				    				    			 for(int j=0; j<transforms.getLength(); j++){
				    				    				 if(transforms.item(j).getNodeName().equals("tl33t")){
				    				    					 l33tTransform=transforms.item(j).getChildNodes().item(0).getTextContent().equalsIgnoreCase("true")?true:false;
				    				    				 }else if(transforms.item(j).getNodeName().equals("images180")){
				    				    					 rotateImages=transforms.item(j).getChildNodes().item(0).getTextContent().equalsIgnoreCase("true")?true:false;
				    				    				 }
				    				    			 }
				    				    		}
				    				    	}
//				    				    	actions = new Actions(blockAllAccesses, blockedIPs, blockedURIs, 
//				    				    			blockedMediaTypes, maxContentLength, rotateImages, l33tTransform);
				    					}
				    				}
//				    				filter = new Filter(actions, originIPs, browser, oS);
//				    				filters.add(filter);
				    				
				    				condition = new FilterCondition(originIPs, browser, oS);
				    				
				    				filter = new ApplicationFilter(
											condition,
											blockAllAccesses,
											blockedIPs,
											blockedURIs,
											blockedMediaTypes,
											maxContentLength,
											l33tTransform,
											rotateImages
									);
			    	    		}
			    			}
			    		}
			    	}
		    	}
	    	}
	    } catch (SAXParseException spe) {
	    	throw new SAXException(":"+spe.getLineNumber()+":"+spe.getColumnNumber());
	    } catch (SAXException se){
	    	throw new SAXException(":parseError");
	    } catch (ParserConfigurationException pce) {
	    	// Parser with specified options can't be built
	    	throw new SAXException(":unknownError");
	    }catch (IOException ioe){
	    	throw new IOException();
	    }
	}

//	@Override
//	public String toString() {
//		return "ConfigurationManager [users=" + filters + "]";
//	}
}