package com.olezo.service.processor;

import com.google.auto.service.AutoService;
import com.olezo.service.annotation.ClientBuilder;
import com.olezo.service.processor.generator.FeignClientConfigGenerator;
import com.olezo.service.processor.generator.ClientHolderConfigGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class ClientBuilderProcessor extends AbstractProcessor {
    private static final Class<ClientBuilder> CLIENT_BUILDER_CLASS = ClientBuilder.class;

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(CLIENT_BUILDER_CLASS.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            // Scan classes
            Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(CLIENT_BUILDER_CLASS);

            if (elementsAnnotatedWith.isEmpty()) {
                return true;
            }

            // check if the annotation was applied to interfaces only
            elementsAnnotatedWith.forEach(this::isInterface);

            var list  = elementsAnnotatedWith.stream()
                    .map(TypeElement.class::cast)
                    .map(ClientBuilderAnnotatedClass::new)
                    .collect(Collectors.toList());

            info("Building feign client configs...");
            var generator = new FeignClientConfigGenerator();
            for (ClientBuilderAnnotatedClass clazz : list) {
                info("Generating: " + clazz.getAnnotatedClass());

                generator.generate(typeUtils, elementUtils, filer, clazz);
            }
            info("Feign client configs were built");

            info("Building client holder...");
            var holderGenerator = new ClientHolderConfigGenerator();
            holderGenerator.generate(typeUtils, messager, elementUtils, filer, list);
            info("Client holder was build");
        } catch (ProcessingException e) {
            info("Exception occurred: " + e.getMessage(), e.getElement());
            error(e.getElement(), e.getMessage());
        } catch (Exception e) {
            info("Exception occurred: " + e.getMessage());
            error(null, e.getMessage());
        }
        return true;
    }

    private void isInterface(Element annotatedElement) {
        if (annotatedElement.getKind() != ElementKind.INTERFACE) {
            throw new ProcessingException(annotatedElement, "Only interface can be annotated with @%s",
                    CLIENT_BUILDER_CLASS.getSimpleName());
        }
    }

    public void error(Element element, String msg) {
        if (element == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, msg, element);
        }
    }

    public void info(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    public void info(String message, Element element) {
        if (element == null) {
            info(message);
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, message, element);
        }
    }
}
