package io.github.qishr.cascara.lang.java.model;

import java.util.Arrays;

import io.github.qishr.cascara.lang.java.model.JlsName.Kind;

public class NameUtil {

    private NameUtil() {
        // No public constructor
    }

    public static JlsName createName(String nameString) {
        if (!isValidName(nameString)) {
            throw new IllegalArgumentException("\"" + nameString + "\" is not a valid name");
        }
        JlsName name = new JlsName();
        int dollarIndex = nameString.indexOf('$');
        if (dollarIndex > -1) {
            String prefix = nameString.substring(0, dollarIndex);
            int dotIndex = prefix.lastIndexOf('.');
            String packageName = prefix.substring(0, dotIndex);
            nameString = nameString.replace("$", ".");
            name.components = extractComponents(nameString);
            name.packageComponentCount = extractComponents(packageName).length;
            name.setMember(true);
        } else {
            name.components = extractComponents(nameString);
        }
        return name;
    }

    public static JlsName createName(JlsName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        JlsName newname = new JlsName();
        newname.components = Arrays.copyOf(name.components, name.components.length);
        newname.setKind(name.kind);
        newname.setPackageComponentCount(name.packageComponentCount);
        newname.setTypeComponentCount(name.typeComponentCount);
        newname.setMember(name.isMember);
        return newname;
    }

    public static JlsName createModuleName(String moduleString) {
        if (moduleString == null) {
            moduleString = "";
        }
        if (!moduleString.isEmpty() && !isValidName(moduleString)) {
            throw new IllegalArgumentException("\"" + moduleString + "\" is not a valid name");
        }
        JlsName name = new JlsName();
        name.components = extractComponents(moduleString);
        name.setKind(JlsName.Kind.MODULE);
        return name;
    }

    public static JlsName createPackageName(String packageString) {
        JlsName name = createName(packageString);
        name.setKind(Kind.PACKAGE);
        name.setPackageComponentCount(name.componentCount());
        return name;
    }

    public static JlsName createPackageName(JlsName packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("packageName must not be null");
        }
        if (!packageName.isPackage() && !packageName.isType()) {
            throw new IllegalArgumentException("packageName must be a package name");
        }
        JlsName name = new JlsName();
        name.setKind(Kind.PACKAGE);
        name.components = Arrays.copyOf(packageName.components, packageName.components.length);
        name.packageComponentCount = packageName.componentCount();
        return name;
    }

    public static JlsName createTypeName(String typeString) {
        JlsName name = createName(typeString);
        name.setKind(Kind.TYPE);
        return name;
    }

    public static JlsName createTypeName(JlsName packageOrTypeName, String typeString) {
        if (packageOrTypeName == null) {
            throw new IllegalArgumentException("packageOrTypeName must not be null");
        }
        if (typeString == null) {
            throw new IllegalArgumentException("typeString must not be null");
        }
        if (!packageOrTypeName.isPackage() && !packageOrTypeName.isType()) {
            throw new IllegalArgumentException("packageOrTypeName must be a package or type name");
        }
        if (typeString.startsWith(packageOrTypeName.fullyQualifiedName())) {
            // It's a qualified type name string
            JlsName name = createName(typeString);
            name.setKind(JlsName.Kind.TYPE);
            name.setPackageComponentCount(packageOrTypeName.packageComponentCount);
            name.setTypeComponentCount(name.componentCount() - packageOrTypeName.packageComponentCount);
            return name;
        } else {
            // It's an unqualified type name string
            JlsName name = createTypeName(packageOrTypeName + "." + typeString);
            name.setKind(JlsName.Kind.TYPE);
            name.setPackageComponentCount(packageOrTypeName.packageComponentCount);
            name.setTypeComponentCount(name.componentCount() - packageOrTypeName.packageComponentCount);
            JlsName name2 = createTypeName(packageOrTypeName, name);
            return name2;
        }
    }

    public static JlsName createTypeName(JlsName packageOrTypeName, JlsName typeName) {
        if (packageOrTypeName == null) {
            throw new IllegalArgumentException("packageOrTypeName must not be null");
        }
        if (typeName == null) {
            throw new IllegalArgumentException("typeName must not be null");
        }
        if (!packageOrTypeName.isPackage() && !packageOrTypeName.isType()) {
            throw new IllegalArgumentException("packageOrTypeName must be a package or type name");
        }
        if (!typeName.isType()) {
            throw new IllegalArgumentException("typeName must be a type name");
        }
        if (typeName.startsWith(packageOrTypeName)) {
            // It's a qualified type name
            return createName(typeName);
        } else {
            // It's an unqualified type name
            JlsName name = new JlsName();
            name.components = Arrays.copyOf(packageOrTypeName.components, packageOrTypeName.componentCount() + typeName.componentCount());
            for (int i = 0; i < typeName.componentCount(); i++) {
                name.components[packageOrTypeName.componentCount() + i] = typeName.components[i];
            }
            name.setPackageComponentCount(packageOrTypeName.packageComponentCount);
            name.setTypeComponentCount(packageOrTypeName.typeComponentCount + typeName.componentCount());
            name.setKind(Kind.TYPE);
            name.setMember(packageOrTypeName.isType());
            return name;
        }
    }

    public static JlsName createMemberName(String memberString) {
        JlsName name = createName(memberString);
        name.setKind(Kind.MEMBER);
        return name;
    }

    public static JlsName createMemberName(JlsName typeName, String memberString) {
        if (typeName == null) {
            throw new IllegalArgumentException("typeName must not be null");
        }
        if (!typeName.isType()) {
            throw new IllegalArgumentException("typeName must be a type name");
        }
        String typeString = typeName.fullyQualifiedName();
        JlsName name = createName(typeString + "." + memberString);
        name.setPackageComponentCount(typeName.packageComponentCount());
        name.setKind(Kind.MEMBER);
        return name;
    }

    public static JlsName createMemberName(JlsName typeName, JlsName memberName) {
        if (typeName == null) {
            throw new IllegalArgumentException("typeName must not be null");
        }
        if (!typeName.isType()) {
            throw new IllegalArgumentException("typeName must be a type name");
        }
        String typeString = typeName.fullyQualifiedName();
        String memberString = memberName.fullyQualifiedName();
        JlsName name = createName(typeString + "." + memberString);
        name.setPackageComponentCount(typeName.packageComponentCount());
        name.setTypeComponentCount(typeName.typeComponentCount + memberName.componentCount());
        name.setKind(memberName.kind);
        name.setMember(true);
        return name;
    }

    private static boolean isValidName(String name) {
        if (name.isEmpty()) {
            return false;
        }
        char c = name.charAt(0);
        // TODO: names like $1
        // if (!Character.isLetter(c) && c!='_' && c!='$') {
        //     return false;
        // }
        for (int i=0; i<name.length(); i++) {
            c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c!='_' && c!='.' && c!='$') {
                return false;
            }
        }
        return true;
    }

    private static String[] extractComponents(String s) {
        String[] c = s.split("\\.");
        if (c.length == 1 && c[0].isBlank()) {
            return new String[0];
        } else {
            return c;
        }
    }

    public static JlsName firstNameComponents(JlsName name, int n) {
        if (n > name.components.length) {
            throw new IndexOutOfBoundsException(String.format(
                    "%d is out of bounds for 0 to %d", n, name.components.length));
        }
        if (n == 0) {
            return createName(name);
        }
        StringBuilder sb = new StringBuilder();
        int end = n < 0 ? name.components.length + n : n;
        for (int i = 0; i < end; i++) {
            if (!sb.isEmpty()) {
                sb.append(".");
            }
            sb.append(name.components[i]);
        }
        JlsName name2 = createName(sb.toString());
        if (end <= name.packageComponentCount) {
            name2.packageComponentCount = n;
            name2.setKind(JlsName.Kind.PACKAGE);
        } else {
            name2.packageComponentCount = name.packageComponentCount;
            if (name.isMember && end == name.components.length) {
                name2.isMember = true;
            } else {
                name2.setKind(JlsName.Kind.TYPE);
            }
        }
        return name2;
    }

    public static JlsName lastNameComponents(JlsName name, int n) {
        if (n > name.components.length) {
            throw new IndexOutOfBoundsException(String.format(
                    "%d is out of bounds for 0 to %d", n, name.components.length));
        }
        if (n == 0) {
            return createName(name);
        }
        StringBuilder sb = new StringBuilder();
        int start = n < 0 ? -n : name.components.length - n;
        for (int i = start; i < name.components.length; i++) {
            if (!sb.isEmpty()) {
                sb.append(".");
            }
            sb.append(name.components[i]);
        }
        JlsName name2 = createName(sb.toString());
        name2.packageComponentCount = name.packageComponentCount - start;
        name2.typeComponentCount = name.typeComponentCount;
        name2.setKind(name.kind);
        name2.setMember(name.isMember);
        return name2;
    }

    /// For module-only references, use null JlsName
    public static Reference createReference(String m, JlsName n) {
        Reference reference = new Reference();
        reference.module = m;
        reference.name = n;
        return reference;
    }

    public static Reference createReference(String n) {
        if (n == null || n.isBlank()) {
            throw new IllegalArgumentException("\"" + n + "\" is not a valid reference");
        }
        Reference reference = new Reference();
        String packageOrType = "";
        String member = "";

        int slash = n.indexOf("/");
        if (slash > -1) {
            reference.module = n.substring(0, slash);
            n = n.substring(slash + 1);
        }
        int hash = n.indexOf("#");
        if (hash > -1) {
            packageOrType = n.substring(0, hash);
            member = n.substring(hash + 1);
            int parenthesis = member.indexOf("(");
            if (parenthesis > -1) {
                reference.parameters = member.substring(parenthesis);
                member = member.substring(0, parenthesis);
            }
            if (packageOrType.isEmpty()) {
                reference.name = NameUtil.createMemberName(member);
            } else {
                JlsName typeName = createTypeName(packageOrType);
                reference.name = NameUtil.createMemberName(typeName, member);
            }
            reference.name.setMember(true);
        } else {
            if (!n.isBlank()) {
                packageOrType = n;
                reference.name = NameUtil.createName(packageOrType);
            }
        }
        return reference;
    }

}
