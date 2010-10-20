package org.cssc.prototpe.parsers.lex;

import org.cssc.prototpe.http.HttpHeader;
import org.cssc.prototpe.parsers.exceptions.InvalidPacketParsingException;
import java.util.HashMap;
import java.util.Map;

%%
%class HttpHeaderParser
%public
%function parse
%standalone

%{
	private HttpHeader header;
	private String currentName;
	private String currentValue;
	
	public HttpHeader getParsedHeader() {
		return header;
	}
%}

%init{
	header = new HttpHeader();
%init}



FIELD_NAME =	[A-Za-z0-9\-_]+
FIELD_VALUE =	[^\r\n]+
NEWLINE = \r\n

%state PARSING_VALUE

%%

<YYINITIAL> {
	[ ]?{FIELD_NAME}/:	{
		//System.out.println("Searching name.");
		currentName = yytext().toLowerCase().trim();
		//System.out.println("Found name " + currentName);
	}
	
	: {
		//System.out.println("Going to parsing value.");
		yybegin(PARSING_VALUE);
	}
}

<PARSING_VALUE> {
	[ ]?{FIELD_VALUE}[ ]?	{
		currentValue = yytext().trim();
		header.setField(currentName, currentValue);
		//System.out.println("Found value " + currentValue);
	}
	
	{NEWLINE} {
		//System.out.println("Going to YYINITIAL.");
		yybegin(YYINITIAL);
	}
}

. {
	//System.out.println("Found error: \"" + yytext() + "\"");
	throw new InvalidPacketParsingException("Invalid package.");
}