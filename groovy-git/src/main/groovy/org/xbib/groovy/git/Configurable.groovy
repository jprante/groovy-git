package org.xbib.groovy.git

import org.xbib.groovy.git.internal.AnnotateAtRuntime

@FunctionalInterface
@AnnotateAtRuntime(annotations = "org.gradle.api.HasImplicitReceiver")
interface Configurable<T> {
  void configure(T t)
}
