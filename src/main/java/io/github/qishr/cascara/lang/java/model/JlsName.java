package io.github.qishr.cascara.lang.java.model;

import java.io.Serial;
import java.io.Serializable;

/// See Java Language Specification (JLS) [JlsChapter 6. Names](https://docs.oracle.com/javase/specs/jls/se24/html/jls-6.html).
public class JlsName implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    String[] components = new String[0];
    int packageComponentCount = 0;
    int typeComponentCount = 0;
    Kind kind = Kind.UNDEFINED;
    boolean isMember = false;

    /// Represents a name.
    public JlsName() {
        // Nothing to see here
    }

    public void setKind(Kind kind) {
        this.kind = kind;
        if (kind == Kind.MEMBER) {
            isMember = true;
        }
    }

    public boolean isPackage() {
        return kind == Kind.PACKAGE;
    }

    public boolean isType() {
        return kind == Kind.TYPE;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean b) {
        isMember = b;
    }

    public boolean isEmpty() {
        return components.length == 0;
    }

    public void setPackageComponentCount(int n) {
        if (n < 0 || n > components.length) {
            throw new IllegalArgumentException("Package component count is out of bounds.");
        }
        packageComponentCount = n;
        if (typeComponentCount == 0) {
            typeComponentCount = components.length - n;
        }
    }

    public int packageComponentCount() {
        return packageComponentCount;
    }

    public void setTypeComponentCount(int n) {
        if (n < 0 || n > components.length) {
            throw new IllegalArgumentException("Package component count is out of bounds.");
        }
        typeComponentCount = n;
    }

    public int typeComponentCount() {
        return typeComponentCount;
    }

    public JlsName packageName() {
        if (components.length == 0) return new JlsName();
        JlsName name = firstComponents(packageComponentCount);
        name.setKind(Kind.PACKAGE);
        name.setPackageComponentCount(packageComponentCount);
        return name;
    }

    public JlsName typeName() {
        // A.B.C
        if (isPackage()) return new JlsName();
        JlsName qualifiedTypeName = NameUtil.createName(this);

        if (isMember && kind != Kind.TYPE) {
            qualifiedTypeName = firstComponents(-1);
            qualifiedTypeName.setKind(Kind.TYPE);
        }
        return qualifiedTypeName;
    }

    public JlsName memberName() {
        if (!isMember) return new JlsName();
        JlsName name = lastComponents(1);
        name.setKind(kind);
        name.isMember = true;
        return name;
    }

    public String fullyQualifiedName() {
        // p.A.B.C
        if (components.length == 0) {
            return "";
        }
        return String.join(".", components);
    }

    public String simpleName() {
        // C
        return lastComponents(1).toString();
    }

    public String fullyQualifiedJvmBinaryName() {
        // p.A$B$C
        if (packageComponentCount > 0 && typeComponentCount > 0) {
            return packageName().toString() + "." + jvmBinaryName();
        } else {
            // Can't tell what is package, type, or nested type
            return fullyQualifiedName();
        }
    }

    public String binaryName() {
        // A.B
        if (kind != Kind.TYPE) { //} || !isMember) {
            return "";
        }
        return lastComponents(typeComponentCount).toString();
    }

    public String jvmBinaryName() {
        // A$B$C
        return typeName().binaryName().replace(".", "$");
    }

    @Override
    public String toString() {
        return fullyQualifiedName();
    }

    public int componentCount() {
        return components.length;
    }

    public int commonComponentCount(JlsName n) {
        if (n == null) {
            return 0;
        }
        int commonCount = 0;
        while (commonCount < Math.min(components.length, n.componentCount()) &&
                components[commonCount].equals(n.components[commonCount])) {
            commonCount++;
        }
        return commonCount;
    }

    public JlsName firstComponents(int n) {
        return NameUtil.firstNameComponents(this, n);
    }

    public JlsName lastComponents(int n) {
        return NameUtil.lastNameComponents(this, n);
    }

    public boolean startsWith(JlsName start) {
        int commonCount = commonComponentCount(start);
        return commonCount == start.componentCount();
    }

    @Override
    public boolean equals(Object name) {
        // TODO: ensure component counts for package and type match
        if (name instanceof JlsName jlsName && componentCount() == commonComponentCount(jlsName)) {
            for (int i = 0; i < componentCount(); i++) {
                if (!components[i].equals(jlsName.components[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public enum Kind {
        UNDEFINED,
        MODULE,
        PACKAGE,
        TYPE,
        MEMBER
    }
}
