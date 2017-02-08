package org.whipper.gui.web.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonObjectSerializer implements MessageBodyWriter<JSONObject>{

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType){
        return type == JSONObject.class && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public long getSize(JSONObject t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType){
        return -1;
    }

    @Override
    public void writeTo(JSONObject t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException{
        OutputStreamWriter osw = new OutputStreamWriter(entityStream);
        t.write(osw);
        osw.flush();
    }
}
