package io.github.qishr.cascara.lang.java.exception;

public class ClassLoadException extends Exception {
    public ClassLoadException(String message) {
        super(message);
    }

    public ClassLoadException(String message, Throwable t) {
        super(message, t);
    }
}
