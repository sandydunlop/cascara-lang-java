package io.github.qishr.cascara.lang.java.model;

/// A class to hold information about method parameters.
public class ParamNode extends VariableNode {

    /// Constructs a ParamNode with the given type and name.
    /// @param type The TypeNode representing the parameter's type.
    /// @param simpleName The simple name of the parameter.
    public ParamNode(VariableTypeNode type, JlsName simpleName) {
        super(type, simpleName);
        kind = JavaSemanticNode.Kind.PARAMETER;
    }

}
