package org.whipper.web;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.whipper.WhipperProperties;

/**
 * Reader for reading Whipper properties in JSON format.
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class WhipperPropertiesReader implements MessageBodyReader<WhipperProperties>{

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType){
        return type == WhipperProperties.class && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public WhipperProperties readFrom(Class<WhipperProperties> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException{
        try{
            JSONObject o = new JSONObject(new JSONTokener(entityStream));
            Properties p = new Properties();
            p.putAll(o.toMap());
            return new WhipperProperties(p);
        } catch (JSONException ex){
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("Cannot parse JSON object.").build());
        }
    }
}
