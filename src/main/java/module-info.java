module cascara.lang.java {
    requires jdk.compiler;
    requires transitive jdk.javadoc;
    requires transitive java.compiler;

    requires transitive cascara.common;
    requires cascara.common.io;

    exports io.github.qishr.cascara.lang.java;
    exports io.github.qishr.cascara.lang.java.exception;
    exports io.github.qishr.cascara.lang.java.model;
    exports io.github.qishr.cascara.lang.java.modeler;
    exports io.github.qishr.cascara.lang.java.processor;
    exports io.github.qishr.cascara.lang.java.token;
    exports io.github.qishr.cascara.lang.java.util;

    opens io.github.qishr.cascara.lang.java.model;
    opens io.github.qishr.cascara.lang.java.modeler;
    opens io.github.qishr.cascara.lang.java.processor;
    opens io.github.qishr.cascara.lang.java.util;
}
