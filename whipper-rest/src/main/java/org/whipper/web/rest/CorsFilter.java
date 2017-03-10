package org.whipper.web.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter{

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext response){
        String origin = requestContext.getHeaderString("Origin");
        if(origin != null && !origin.isEmpty()){
            response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException{
        if("OPTIONS".equalsIgnoreCase(requestContext.getMethod())){
            String origin = requestContext.getHeaderString("Origin");
            String reqMethod = requestContext.getHeaderString("Access-Control-Request-Method");
            String reqHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
            if(origin != null && !origin.isEmpty() && reqMethod != null && !reqMethod.isEmpty()
                    && reqHeaders != null && reqHeaders.isEmpty()){
                requestContext.abortWith(Response.ok().header("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE")
                        .header("Access-Control-Allow-Headers", "Content-Type").build());
            }
        }
    }
}
