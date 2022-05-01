package com.olezo.feign;

// This is a fake implementation of the class that is used to build Feign clients
public class FeignClientBuilder {
    public <T> T buildClient(Class<T> apiType, String url, boolean admin) {
        // empty implementation
        return null;
    }
}
