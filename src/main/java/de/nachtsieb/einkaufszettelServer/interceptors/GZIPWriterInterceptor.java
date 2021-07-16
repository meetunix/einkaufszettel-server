package de.nachtsieb.einkaufszettelServer.interceptors;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GZIPWriterInterceptor implements WriterInterceptor {
  private static Logger logger = LogManager.getLogger(GZIPWriterInterceptor.class);


  public GZIPWriterInterceptor() {
    super();
  }

  private HttpHeaders context;

  public GZIPWriterInterceptor(@Context HttpHeaders context) {
    this.context = context;
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext)
      throws IOException, WebApplicationException {

    String acceptEncoding = context.getHeaderString("Accept-Encoding");

    if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
      logger.debug("gzip encoding requested");
      final OutputStream outputStream = writerInterceptorContext.getOutputStream();
      writerInterceptorContext.setOutputStream(new GZIPOutputStream(outputStream));
      writerInterceptorContext.getHeaders().putSingle("Content-Encoding", "gzip");
    }

    writerInterceptorContext.proceed();
  }
}
