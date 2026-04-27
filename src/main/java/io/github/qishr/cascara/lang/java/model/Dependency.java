package io.github.qishr.cascara.lang.java.model;

import java.util.ArrayList;
import java.util.List;

public class Dependency {
    private String packageName = null;
    private List<TypeNode> types = new ArrayList<>();

    public Dependency(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<TypeNode> getTypes() {
        return types;
    }

    public void addType(TypeNode type) {
        types.add(type);
    }

    public boolean contains(String typeName) {
        TypeNode tn = getType(typeName);
        return (tn != null);
    }

    public TypeNode getType(String typeName) {
        for (TypeNode typeNode : types) {
            if (typeNode.getName().fullyQualifiedName().equals(typeName)) {
                return typeNode;
            }
        }
        return null;
    }
}
