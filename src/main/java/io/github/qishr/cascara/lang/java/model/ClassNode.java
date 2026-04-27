package io.github.qishr.cascara.lang.java.model;

/// Represents a Java class.
/// This class is intended to model a class within a program's structure.
/// It inherits properties from TypeNode and specifies its own kind.
public class ClassNode extends TypeNode {
    /// Constructs a new ClassNode.
    /// @param name The name of the class.
    public ClassNode(JlsName name) {
        super(name);
        kind = JavaSemanticNode.Kind.CLASS;
    }
}
