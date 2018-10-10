package org.xbib.groovy.git.util

import java.nio.file.Path

final class CoercionUtil {

    private CoercionUtil() {
    }

    static File toFile(Object obj) {
        if (obj instanceof File) {
            return obj
        } else if (obj instanceof Path) {
            return obj.toFile()
        } else {
            return new File(obj.toString())
        }
    }
}
