package io.github.qishr.cascara.lang.java.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/// Represents a Java package
public class PackageNode extends JavaSemanticNode {
    private String moduleName;
    private Path sourcePath;
    private boolean hasPackageInfo = false;
    private final List<PackageNode> packages = new ArrayList<>();
    private Link link;

    public void setLink(Link l) {
        link = l;
    }

    public Link getLink() {
        return link;
    }

    // store children by the interface type (no concrete TypeNode mention)
    private final List<TypeNode> types = new ArrayList<>();

    public PackageNode() {
        // Nothing to see here
    }

    /// Constructs a PackageNode with the specified qualified package name.
    /// @param name The qualified name of the package.
    public PackageNode(String name) {
        this.name = NameUtil.createPackageName(name);
        this.kind = JavaSemanticNode.Kind.PACKAGE;
    }

    public PackageNode(JlsName name) {
        this.name = NameUtil.createPackageName(name);
        kind = JavaSemanticNode.Kind.PACKAGE;
    }

    @Override
    public List<JavaSemanticNode> getChildren() {
        ArrayList<JavaSemanticNode> children = new ArrayList<>();
        children.addAll(packages);
        children.addAll(types);
        return children;
    }

    @Override
    public String getDisplayName() {
        if (name == null) {
            throw new IllegalStateException("JLS name is null");
        }
        return name.toString();
    }

    /// Sets the module for this package.
    /// @param module The ModuleNode that owns this package.
    public void setModuleName(String module) {
        this.moduleName = module;
    }

    /// Returns the module that owns this package.
    /// @return The ModuleNode instance.
    public String getModuleName() {
        return moduleName;
    }

    public void setSourcePath(Path path) {
        this.sourcePath = path;
    }

    @Override
    public Path getSourcePath() {
        return sourcePath;
    }

    /// Returns the list of package members owned by this package.
    /// @return List of PackageNode objects.
    public List<PackageNode> getPackages() {
        return packages;
    }

    /// Adds a subpackage to this package.
    /// @param packageNode The PackageNode to add as a member.
    public void addPackage(PackageNode packageNode) {
        packages.add(packageNode);
    }

    public void setHasPackageInfo(boolean b) {
        hasPackageInfo = b;
    }

    public boolean hasPackageInfo() {
        return hasPackageInfo;
    }

    public void addType(TypeNode typeNode) {
        TypeNode existingTypeNode = getTypeNode(typeNode.getName());
        if (existingTypeNode != null) {
            types.remove(existingTypeNode);
        }
        types.add(typeNode);
    }

    /// Gets the list of types *owned* by this instance.
    public List<TypeNode> getTypes() {
        return List.copyOf(types);
    }

    /// Gets the list of classes *owned* by this instance.
    public List<TypeNode> getClasses() {
        List<TypeNode> out = new ArrayList<>();
        for (TypeNode t : types)
            if (t.isClass())
                out.add(t);
        return out;
    }

    /// Gets the list of interfaces *owned* by this instance.
    public List<TypeNode> getInterfaces() {
        List<TypeNode> out = new ArrayList<>();
        for (TypeNode t : types)
            if (t.isInterface())
                out.add(t);
        return out;
    }

    /// Gets the list of enums *owned* by this instance.
    public List<TypeNode> getEnums() {
        List<TypeNode> out = new ArrayList<>();
        for (TypeNode t : types)
            if (t.isEnum())
                out.add(t);
        return out;
    }

    /// Gets the list of records *owned* by this instance.
    public List<TypeNode> getRecords() {
        List<TypeNode> out = new ArrayList<>();
        for (TypeNode t : types)
            if (t.isRecord())
                out.add(t);
        return out;
    }

    /// Gets the list of annotations *owned* by this instance.
    public List<TypeNode> getAnnotations() {
        List<TypeNode> out = new ArrayList<>();
        for (TypeNode t : types)
            if (t.isAnnotation())
                out.add(t);
        return out;
    }

    public TypeNode getType(String typeName) {
        for (TypeNode type : types) {
            if (type.getName().simpleName().equals(typeName)) {
                return type;
            }
        }
        return null;
    }

    public TypeNode getTypeNode(JlsName typeName) {
        for (TypeNode type : types) {
            if (type.getName().equals(typeName)) {
                return type;
            }
        }
        return null;
    }

    /// Sorts the nodes owned by this instance into alphabetical order.
    public void sort() {
        types.sort((a, b) -> a.getName().simpleName().compareTo(b.getName().simpleName()));
    }
}
