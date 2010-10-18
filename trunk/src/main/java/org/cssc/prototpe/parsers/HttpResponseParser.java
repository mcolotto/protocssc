package org.cssc.prototpe.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.cssc.prototpe.http.HttpResponse;
import org.cssc.prototpe.parsers.exceptions.HttpParserException;
import org.cssc.prototpe.parsers.exceptions.InvalidPacketException;
import org.cssc.prototpe.parsers.lex.HttpResponseLexParser;

public class HttpResponseParser {

	private static int CHUNK_BUFFER_SIZE = 50;

	private InputStream inputStream;
	private HttpResponse parsedResponse;
	private int readContentBytes;
	private boolean lastChunkRead = false;

	public HttpResponseParser(InputStream inputStream) {
		this.inputStream = inputStream;
		this.readContentBytes = 0;
	}

	public HttpResponse parse() throws IOException {
		StringBuffer buffer = new StringBuffer();

		boolean firstCrRead = false;
		boolean firstLfRead = false;
		boolean secondCrRead = false;
		boolean secondLfRead = false;

		while(!(firstCrRead && firstLfRead &&
				secondCrRead && secondLfRead)) {
			int readInt = -1;
			while(readInt == -1) {
				readInt = inputStream.read();
			}
			char readChar = (char)readInt;

			if(readChar == '\r') {
				if(firstCrRead && firstLfRead) {
					secondCrRead = true;
				} else {
					firstCrRead = true;
					firstLfRead = false;
					secondCrRead = false;
					secondLfRead = false;
				}
			} else if(readChar == '\n') {
				if(firstCrRead && !secondCrRead) {
					firstLfRead = true;
				} else if(secondCrRead) {
					secondLfRead = true;
				}
			} else {
				firstCrRead = false;
				firstLfRead = false;
				secondCrRead = false;
				secondLfRead = false;
			}

			buffer.append(readChar);
		}

		String parsedString = buffer.toString();

		HttpResponseLexParser parser = new HttpResponseLexParser(new StringReader(parsedString));
		parser.parse();

		parsedResponse = parser.getParsedResponse();
		return parsedResponse;
	}

	
	/**
	 * Reads next n bytes from a response body.
	 * This method only works if the response has a content-length field
	 * within its header.
	 * @param buffer The buffer where the bytes will be stored.
	 * @param offset The offset to write in the buffer.
	 * @param n The number of bytes to read.
	 * @return The number of read bytes.
	 */
	public int readNextNBodyBytes(byte[] buffer, int offset, int n) throws IOException {
		if(parsedResponse == null) {
			parsedResponse = parse();
		}

		String contentLengthString = parsedResponse.getHeader().getField("content-length");

		if(contentLengthString == null) {
			throw new HttpParserException("This response has not a content-length field within its header.");
		}

		if(buffer.length - offset + 1 < n) {
			throw new HttpParserException("There is not enough space in the buffer to store " + n + " bytes.");
		}

		int contentLength = Integer.valueOf(contentLengthString);


		//2: because of the \r\n at the end of the content.
		if(n + readContentBytes > contentLength) {
			n = contentLength - readContentBytes;
		}
		
		if(n <= 0) {
			return -1;
		}

		int aux = inputStream.read(buffer, offset, n);
		readContentBytes += aux;

		return aux;
	}


	/**
	 * Reads the next chunk of a chunked response body.
	 * This method only works if the response has a transfer-encoding field
	 * within its header, and its value is "chunked".
	 */
	public byte[] readNextChunk() throws IOException {
		if(parsedResponse == null) {
			parsedResponse = parse();
		}

		if(lastChunkRead) {
			return null;
		}

		String transferEncodingString = parsedResponse.getHeader().getField("transfer-encoding");

		if(transferEncodingString == null) {
			throw new HttpParserException("This response has not a transfer-encoding field within its header.");
		}

		if(!transferEncodingString.toLowerCase().equals("chunked")) {
			throw new HttpParserException("The transfer-encoding for this response is not \"chunked\".");
		}

		byte[] ret = new byte[CHUNK_BUFFER_SIZE];

		int i = 0;
		StringBuffer buffer = new StringBuffer();
		int chunkSize;
		boolean crRead = false;
		boolean lfRead = false;

		/* Firstly, the chunk size is read. */
		while(!(crRead && lfRead)) {
			if(i == ret.length) {
				byte[] temp = new byte[ret.length + CHUNK_BUFFER_SIZE];
				System.arraycopy(ret, 0, temp, 0, ret.length);
				ret = temp;
			}

			int readInt = -1;
			while(readInt == -1) {
				readInt = inputStream.read();
			}
			ret[i] = (byte)readInt;
			i++;
			char readChar = (char)readInt;

			if(readChar == '\r') {
				crRead = true;
			} else if(readChar == '\n') {
				if(crRead) {
					lfRead = true;
				} else {
					crRead = false;
					lfRead = false;
				}
			} else {
				crRead = false;
				lfRead = false;
			}

			buffer.append(readChar);
		}

		try {
			chunkSize = Integer.parseInt(buffer.toString().trim(), 16);
		} catch(NumberFormatException e) {
			throw new InvalidPacketException("Chunk size must be hexadecimal.");
		}

		if(chunkSize > 0) {
			int tempLength = i;

			byte[] temp = new byte[tempLength + chunkSize + 2];
			System.arraycopy(ret, 0, temp, 0, tempLength);
			ret = temp;

			while(i < tempLength + chunkSize) {
				int readInt = -1;
				while(readInt == -1) {
					readInt = inputStream.read();
				}

				ret[i] = (byte)readInt;
				i++;
			}

			int cr = inputStream.read();
			int lf = inputStream.read();
			if(cr != 13 || lf != 10) {
				throw new HttpParserException("Invalid chunked data.");
			}

			ret[i] = (byte)cr;
			ret[i + 1] = (byte)lf;

		} else {
			lastChunkRead = true;
			byte[] temp = new byte[i];
			System.arraycopy(ret, 0, temp, 0, i);
			ret = temp;
		}

		return ret;
	}

}
