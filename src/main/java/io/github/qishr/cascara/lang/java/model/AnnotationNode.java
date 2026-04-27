package io.github.qishr.cascara.lang.java.model;

/// A [JavaSemanticNode] that represents an [Annotation](java.text.Annotation) class
public class AnnotationNode extends TypeNode {
    /// Constructor that sets the minimum required information for an AnnotationNode.
    /// @param name the name of the Annotation
    public AnnotationNode(JlsName name) {
        super(name);
        kind = JavaSemanticNode.Kind.ANNOTATION;
    }
}

