package com.olezo.service.processor.generator;

import com.olezo.service.processor.ClientBuilderAnnotatedClass;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.olezo.service.processor.Util.buildGetter;
import static com.olezo.service.processor.Util.getClassName;

public class ClientHolderConfigGenerator {
    public static final String CONTEXT = "context";
    public static final String ADMIN = "admin";

    private Messager messager;

    public void generate(Types typeUtils, Messager messager, Elements elementUtils, Filer filer,
            List<ClientBuilderAnnotatedClass> annotationHolders) {
        this.messager = messager;

        Map<String, List<ClientBuilderAnnotatedClass>> holderNameToHoldersClass = annotationHolders.stream()
                .collect(Collectors.groupingBy(ClientBuilderAnnotatedClass::getHolderName));

        holderNameToHoldersClass.forEach((holderClass, annotationClassList)
                -> createHolderClass(elementUtils, filer, annotationClassList, holderClass));
    }

    @SneakyThrows
    private void createHolderClass(Elements elementUtils, Filer filer,
            List<ClientBuilderAnnotatedClass> annotationHolders, String holderClassName) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(holderClassName); // TODO: replace class name, get it from annotation

        for (ClientBuilderAnnotatedClass annotationHolder : annotationHolders) {
            TypeElement feignClientTypeElement = elementUtils.getTypeElement(annotationHolder.getAnnotatedClassNameWithPackage());
            String feignClientClassNameString = getClassName(feignClientTypeElement); // TODO: move to method?

            String contextFieldName = getFieldName(CONTEXT, feignClientClassNameString);
            String adminFieldName = getFieldName(ADMIN, feignClientClassNameString);

            String name = buildGetter(feignClientClassNameString);
            info("Building method: " + name);
            MethodSpec isAdmin = MethodSpec.methodBuilder(name) // getPersonClient
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Boolean.TYPE, "isAdmin")
                    .addStatement("return isAdmin ? $L : $L", adminFieldName, contextFieldName)
                    .returns(TypeName.get(feignClientTypeElement.asType()))
                    .build();

            builder
                    .addMethod(isAdmin)
                    .addField(buildField(contextFieldName, feignClientTypeElement))
                    .addField(buildField(adminFieldName, feignClientTypeElement));
        }

        TypeSpec typeSpec = builder.addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addAnnotation(RequiredArgsConstructor.class)
                .build();

        // example: annotatedClassTypeElement -> PersonClient
        TypeElement annotatedClassTypeElement = elementUtils.getTypeElement(annotationHolders.get(0).getAnnotatedClassNameWithPackage());

        PackageElement pkg = elementUtils.getPackageOf(annotatedClassTypeElement);
        String packageName = replacePackageName(pkg.getQualifiedName().toString());
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }

    private String getFieldName(String context, String feignClientClassNameString) {
        return context + feignClientClassNameString;
    }

    private FieldSpec buildField(String fieldName, TypeElement fieldType) {
        var className = ClassName.get(fieldType); // example: PersonClient

        return FieldSpec
                .builder(className, fieldName, Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private String replacePackageName(String packageName) {
        String[] directories = packageName.split("[.]"); // example com.package.service -> com.package.config
        return packageName.replace(directories[directories.length - 1], "config");
    }

    public void info(String message) {
        messager.printMessage(Diagnostic.Kind.OTHER, message);
    }
}
