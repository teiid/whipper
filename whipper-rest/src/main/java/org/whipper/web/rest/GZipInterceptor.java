package org.whipper.web.rest;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class GZipInterceptor implements WriterInterceptor{

    private static final MediaType APP_ZIP = new MediaType("application", "zip");
    @Context
    private HttpServletRequest req;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException{
        String ae = req.getHeader("Accept-Encoding");
        if(ae != null && ae.toLowerCase().contains("gzip")){ // only if client accepts GZIP
            if(!context.getMediaType().equals(APP_ZIP)){
                context.getHeaders().putSingle("Content-Encoding", "gzip");
            }
        }
        context.proceed();
    }
}
