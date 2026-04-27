package io.github.qishr.cascara.lang.java.model;

import java.util.ArrayList;
import java.util.List;

/// A node representing a Java interface type
public class InterfaceNode extends TypeNode {

    private List<Link> implementingClasses = new ArrayList<>();

    /// Create an InterfaceNode with the specified details
    /// @param name The name of the enum.
    public InterfaceNode(JlsName name) {
        super(name);
        kind = JavaSemanticNode.Kind.INTERFACE;
    }

    public void addImplementingClass(Link classLink) {
        implementingClasses.add(classLink);
    }

    public List<Link> getImplementingClasses() {
        return implementingClasses;
    }
}
