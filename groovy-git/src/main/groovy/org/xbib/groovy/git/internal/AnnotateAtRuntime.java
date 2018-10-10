package org.xbib.groovy.git.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("org.xbib.groovy.git.internal.AnnotateAtRuntimeASTTransformation")
public @interface AnnotateAtRuntime {
    String[] annotations() default {};
}
