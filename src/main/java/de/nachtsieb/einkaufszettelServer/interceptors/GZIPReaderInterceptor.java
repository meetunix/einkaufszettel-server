package de.nachtsieb.einkaufszettelServer.interceptors;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GZIPReaderInterceptor implements ReaderInterceptor {
    private static Logger logger = LogManager.getLogger(GZIPReaderInterceptor.class);
    
    
	public GZIPReaderInterceptor() {
		super();
	}

	private HttpHeaders context;

	public GZIPReaderInterceptor(@Context HttpHeaders context) {
		this.context = context;
	}

	@Override
	public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext)
			throws IOException, WebApplicationException {

		String contentEncoding = context.getHeaderString("Content-Encoding");

		if (contentEncoding != null && contentEncoding.contains("gzip")) {
			logger.debug("gzip encoded request arrived");
			final InputStream inputStream = readerInterceptorContext.getInputStream();
			readerInterceptorContext.setInputStream(new GZIPInputStream(inputStream));
			//readerInterceptorContext.getHeaders().putSingle("Content-Encoding", "gzip");
		}

		return readerInterceptorContext.proceed();
	}
}