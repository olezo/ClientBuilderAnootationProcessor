package com.olezo.service.processor;

import com.olezo.service.annotation.ClientBuilder;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;

public class ClientBuilderAnnotatedClass {
    private final TypeElement annotatedClass;
    private final ClientBuilder annotation;

    public ClientBuilderAnnotatedClass(TypeElement annotatedClass) throws ProcessingException {
        this.annotatedClass = annotatedClass;
        annotation = annotatedClass.getAnnotation(ClientBuilder.class);
    }

    // TypeElement of PlatformBaseUrlConfig.class
    @SuppressWarnings("ResultOfMethodCallIgnored") // the hack to get MirroredType
    public TypeElement getBaseUrlConfigTypeElement(Types typeUtils) {
        try {
            annotation.config();
        } catch (MirroredTypeException e) {
            return (TypeElement) typeUtils.asElement(e.getTypeMirror());
        }
        return null;
    }

    public String getPropertyName() {
        return annotation.propertyName();
    }

    public String getHolderName() {
        return annotation.holderName();
    }

    public String getAnnotatedClassName() {
        return Util.getClassName(annotatedClass);
    }

    public String getAnnotatedClassNameWithPackage() {
        return Util.getClassNameWithPackage(annotatedClass);
    }

    public TypeElement getAnnotatedClass() {
        return annotatedClass;
    }
}
