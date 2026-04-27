package io.github.qishr.cascara.lang.java.model;

public class VariableNode extends AbstractMember {
    private VariableTypeNode type;

    /// Constructs a VariableNode with the given type and name.
    /// @param type The VariableTypeNode representing the variable's type.
    /// @param simpleName The simple name of the variable.
    public VariableNode(VariableTypeNode type, JlsName simpleName) {
        this.type = type;
        this.name = simpleName;
    }

    /// Returns the type Text of this variable.
    /// @return The Text representing the variable's type.
    public VariableTypeNode getType() {
        return type;
    }

    /// Sets the type VariableTypeNode of this variable.
    /// @param type The VariableTypeNode to set as this variable's type.
    public void setType(VariableTypeNode type) {
        this.type = type;
    }
}


