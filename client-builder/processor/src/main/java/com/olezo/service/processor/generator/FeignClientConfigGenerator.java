package com.olezo.service.processor.generator;

import com.olezo.service.processor.ClientBuilderAnnotatedClass;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.olezo.service.processor.Util.buildFieldCallingMethod;
import static com.olezo.service.processor.Util.buildGetterMethod;
import static com.olezo.service.processor.Util.getClassName;
import static com.olezo.service.processor.Util.makeFirstLetterLowerCase;

public class FeignClientConfigGenerator {
    private final static String FEIGN_CONFIG_SUFFIX = "FeignConfig";
    private static final String CONTEXT_PREFIX = "context";
    private static final String ADMIN_PREFIX = "admin";
    private static final String METHOD_STATEMENT = "return feignClientBuilder.buildClient($L.class, $L, $L)";

    private static final String FEIGN_CLIENT_BUILDER_CLASS = "FeignClientBuilder"; // TODO: should NOT be hardcoded
    private static final String FEIGN_CLIENT_BUILDER_FIELD = "feignClientBuilder";
    private static final String FEIGN_CLIENT_BUILDER_PACKAGE = "com.olezo.feign";
    private static final String PACKAGE_TO_PLACE_CLASSES = "config";

    @SneakyThrows
    public void generate(Types typeUtils, Elements elementUtils, Filer filer, ClientBuilderAnnotatedClass annotationHolder) {

        var feignClientClassName = annotationHolder.getAnnotatedClassName();

        TypeElement baseUrlConfigTypeElement = annotationHolder.getBaseUrlConfigTypeElement(typeUtils);
        TypeElement feignClientTypeElement = elementUtils.getTypeElement(annotationHolder.getAnnotatedClassNameWithPackage());

        var configClassName = feignClientClassName + FEIGN_CONFIG_SUFFIX;

        var baseUrlConfigClassNameString = getClassName(baseUrlConfigTypeElement);
        checkIfContainsGivenField(baseUrlConfigClassNameString, annotationHolder);

        var getterMethodName = buildGetterMethod(annotationHolder.getPropertyName());
        var configInvocation = buildFieldCallingMethod(baseUrlConfigClassNameString, getterMethodName);

        MethodSpec.Builder contextMethod = MethodSpec.methodBuilder(CONTEXT_PREFIX + feignClientClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Bean.class)
                .addStatement(METHOD_STATEMENT, feignClientClassName, configInvocation, false)
                .returns(TypeName.get(feignClientTypeElement.asType()));

        MethodSpec.Builder adminMethod = MethodSpec.methodBuilder(ADMIN_PREFIX + feignClientClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Bean.class)
                .addStatement(METHOD_STATEMENT, feignClientClassName, configInvocation, true)
                .returns(TypeName.get(feignClientTypeElement.asType()));


        ClassName baseUrlConfigClassName = ClassName.get(baseUrlConfigTypeElement);

        FieldSpec urlConfigField = FieldSpec
                .builder(baseUrlConfigClassName, makeFirstLetterLowerCase(baseUrlConfigClassNameString), Modifier.PRIVATE, Modifier.FINAL)
                .build();

        FieldSpec feignClientBuilder = getFeignClientBuilderField();

        TypeSpec typeSpec = TypeSpec.classBuilder(configClassName)
                .addField(feignClientBuilder)
                .addField(urlConfigField)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(org.springframework.context.annotation.Configuration.class)
                .addAnnotation(lombok.RequiredArgsConstructor.class)
                .addMethod(contextMethod.build())
                .addMethod(adminMethod.build())
                .build();

        PackageElement pkg = elementUtils.getPackageOf(feignClientTypeElement);
        String packageName = replacePackageName(pkg.getQualifiedName().toString());
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }

    private void checkIfContainsGivenField(String className, ClientBuilderAnnotatedClass annotatedClass) {
      // TODO: implement
    }

    private String replacePackageName(String packageName) {
        String[] directories = packageName.split("[.]");
        return packageName.replace(directories[directories.length - 1], PACKAGE_TO_PLACE_CLASSES);
    }

    private FieldSpec getFeignClientBuilderField() {
        var className
                = ClassName.get(FEIGN_CLIENT_BUILDER_PACKAGE, FEIGN_CLIENT_BUILDER_CLASS);
        return FieldSpec
                .builder(className, FEIGN_CLIENT_BUILDER_FIELD, Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }
}
