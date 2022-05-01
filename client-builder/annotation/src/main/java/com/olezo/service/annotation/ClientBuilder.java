package com.olezo.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ClientBuilder {
    /**
     * Returns the field name used to get the property value from {@link ClientBuilder#config()}
     * @return the field name used to get the property value from {@link ClientBuilder#config()}
     */
    String propertyName();

    /**
     * Returns the config class that stores all properties
     * @return the config class that stores all properties
     */
    Class<?> config();
    /**
     * Returns the holder name that will be created to hold all classes marked with ClientBuilder
     * @return the holder name that will be created to hold all classes marked with ClientBuilder
     */
    String holderName();
}
