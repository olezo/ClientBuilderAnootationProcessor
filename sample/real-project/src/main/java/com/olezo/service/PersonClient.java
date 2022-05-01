package com.olezo.service;

import com.olezo.config.PlatformBaseUrlConfig;
import com.olezo.service.annotation.ClientBuilder;

@ClientBuilder(propertyName = "person",
        config = PlatformBaseUrlConfig.class,
        holderName = "PlatformClientHolder")
public interface PersonClient {
    // this is one of the Feign Clients
}
