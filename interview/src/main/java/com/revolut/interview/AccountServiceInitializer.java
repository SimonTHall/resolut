package com.revolut.interview;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class AccountServiceInitializer {
    private final static int port = 8080;
    private final static String host = "http://localhost/";
     
    public static void main(String[] args) {
        URI baseUri = UriBuilder.fromUri(host).port(port).build();
        ResourceConfig config = new ResourceConfig(AccountServiceController.class);
        JdkHttpServerFactory.createHttpServer(baseUri, config);
    }
}
