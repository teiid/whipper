package org.whipper.web.rest;

import org.json.JSONObject;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

@Provider
@Priority(Priorities.ENTITY_CODER)
public class WrapResponseInterceptor implements WriterInterceptor{

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException{
        if(context.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)){
            Object original = context.getEntity();
            if(original != null){
                JSONObject wrap = new JSONObject();
                wrap.put("data", original);
                context.setEntity(wrap);
                context.setType(JSONObject.class);
            }
        }
        context.proceed();
    }
}
