package com.olezo.service;

import com.olezo.config.PlatformBaseUrlConfig;
import com.olezo.service.annotation.ClientBuilder;

@ClientBuilder(
        propertyName = "favorite",
        config = PlatformBaseUrlConfig.class,
        holderName = "PlatformClientHolder"
)
public interface FavoriteClient {
    // this is one of the Feign Clients
}
