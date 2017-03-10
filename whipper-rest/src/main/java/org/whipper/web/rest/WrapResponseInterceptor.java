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

import org.json.JSONObject;

@Provider
@Priority(Priorities.ENTITY_CODER)
public class WrapResponseInterceptor implements WriterInterceptor{

    @Context
    private HttpServletRequest req;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException{
        if(context.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)){
            Object original = context.getEntity();
            System.out.println("Original: " + original);
            if(original != null){
                JSONObject wrap = new JSONObject();
                wrap.put("data", original);
                System.out.println("Wrap: " + wrap);
                context.setEntity(wrap);
                context.setType(JSONObject.class);
            }
        }
        context.proceed();
    }
}
