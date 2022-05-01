package com.olezo.service;

import com.olezo.config.PlatformBaseUrlConfig;
import com.olezo.service.annotation.ClientBuilder;

@ClientBuilder(propertyName = "action",
        config = PlatformBaseUrlConfig.class,
        holderName = "PlatformClientHolder")
public interface ActionClient {
    // this is one of the Feign Clients
}
