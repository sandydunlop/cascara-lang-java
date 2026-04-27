package io.github.qishr.cascara.lang.java.modeler;

import java.lang.annotation.Annotation;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Opens;
import java.lang.module.ModuleDescriptor.Provides;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import io.github.qishr.cascara.lang.java.exception.JavaModelerException;
// import io.github.qishr.cascara.lang.java.exception.JavaParserException;
import io.github.qishr.cascara.lang.java.model.AnnotationNode;
import io.github.qishr.cascara.lang.java.model.AppliedAnnotationNode;
import io.github.qishr.cascara.lang.java.model.ClassNode;
import io.github.qishr.cascara.lang.java.model.DirectiveNode;
import io.github.qishr.cascara.lang.java.model.EnumNode;
import io.github.qishr.cascara.lang.java.model.FieldNode;
import io.github.qishr.cascara.lang.java.model.InterfaceNode;
import io.github.qishr.cascara.lang.java.model.JlsName;
import io.github.qishr.cascara.lang.java.model.MethodNode;
import io.github.qishr.cascara.lang.java.model.ModelUtil;
import io.github.qishr.cascara.lang.java.model.Modifier;
import io.github.qishr.cascara.lang.java.model.ModuleNode;
import io.github.qishr.cascara.lang.java.model.NameUtil;
import io.github.qishr.cascara.lang.java.model.PackageNode;
import io.github.qishr.cascara.lang.java.model.PackageReference;
import io.github.qishr.cascara.lang.java.model.ParamNode;
import io.github.qishr.cascara.lang.java.model.RecordNode;
import io.github.qishr.cascara.lang.java.model.Reference;
import io.github.qishr.cascara.lang.java.model.JavaSemanticNode;
import io.github.qishr.cascara.lang.java.model.TypeNode;
import io.github.qishr.cascara.lang.java.model.VariableTypeNode;

public class StandardModeler implements Modeler<Module, Package, Class<?>, Field, Method, Parameter> {
    @Override
    public ModuleNode modelModule(Module m) {
        ModuleNode moduleNode = new ModuleNode(m.getName());
        for (String packageString : m.getPackages()) {
            PackageReference packageReference = new PackageReference(packageString);
            // TODO
            // moduleNode.addPackage(packageReference);
            throw new JavaModelerException("unimplemented");
        }
        for (Annotation annotation : m.getAnnotations()) {
            AnnotationNode annoNode = modelAnnotation(annotation);
            AppliedAnnotationNode aan = new AppliedAnnotationNode(annoNode.getName());
            moduleNode.addAppliedAnnotationNode(aan);
        }
        ModuleDescriptor md = m.getDescriptor();
        moduleNode.setMainClass(md.mainClass().get());
        moduleNode.setOpen(md.isOpen());
        moduleNode.setAutomatic(md.isAutomatic());
        for (Requires re : md.requires()) {
            DirectiveNode directiveNode = new DirectiveNode();
            directiveNode.setKind(DirectiveNode.Kind.REQUIRES);
            //TODO: Requires modifier
            directiveNode.addModule(re.name());
            moduleNode.addDirective(directiveNode);
        }
        for (Exports ex : md.exports()) {
            DirectiveNode directiveNode = new DirectiveNode();
            directiveNode.setKind(DirectiveNode.Kind.EXPORTS);
            PackageReference packageReference = new PackageReference(ex.source());
            directiveNode.setPackageReference(packageReference);
            for (String target : ex.targets()) {
                directiveNode.addModule(target);
            }
            moduleNode.addDirective(directiveNode);
        }
        for (Opens op : md.opens()) {
            DirectiveNode directiveNode = new DirectiveNode();
            directiveNode.setKind(DirectiveNode.Kind.OPENS);
            PackageReference packageReference = new PackageReference(op.source());
            directiveNode.setPackageReference(packageReference);
            for (String target : op.targets()) {
                directiveNode.addModule(target);
            }
            moduleNode.addDirective(directiveNode);
        }
        for (String us : md.uses()) {
            DirectiveNode directiveNode = new DirectiveNode();
            directiveNode.setKind(DirectiveNode.Kind.USES);
            Reference typeName = NameUtil.createReference(us);
            directiveNode.setTypeName(typeName);
            moduleNode.addDirective(directiveNode);
        }
        for (Provides pr : md.provides()) {
            DirectiveNode directiveNode = new DirectiveNode();
            directiveNode.setKind(DirectiveNode.Kind.PROVIDES);
            Reference typeName = NameUtil.createReference(pr.service());
            directiveNode.setTypeName(typeName);
            for (String provider : pr.providers()) {
                typeName = NameUtil.createReference(provider);
                directiveNode.addTypeName(typeName);
            }
            moduleNode.addDirective(directiveNode);
        }
        return moduleNode;
    }

    @Override
    public PackageNode modelPackage(Package p) {
        return null;
    }

    @Override
    public TypeNode modelType(Class<?> type) {
        if (type.isEnum()) {
            return modelType(type, JavaSemanticNode.Kind.ENUM);
        } else if (type.isInterface()) {
            return modelType(type, JavaSemanticNode.Kind.INTERFACE);
        } else if (type.isRecord()) {
            return modelType(type, JavaSemanticNode.Kind.RECORD);
        } else if (type.isAnnotation()) {
            return modelType(type, JavaSemanticNode.Kind.ANNOTATION);
        } else {
            return modelType(type, JavaSemanticNode.Kind.CLASS);
        }
    }

    @Override
    public ClassNode modelClass(Class<?> type) {
        return (ClassNode)modelType(type, JavaSemanticNode.Kind.CLASS);
    }

    @Override
    public FieldNode modelField(Field field) {
        VariableTypeNode vt = ModelUtil.parseVariableType(field.getType().getTypeName());
        JlsName typeName = NameUtil.createTypeName(field.getDeclaringClass().getName());
        JlsName fieldName = NameUtil.createMemberName(typeName, field.getName());
        FieldNode fieldNode = new FieldNode(vt, fieldName);
        // TODO: Modifiiers including static

        int modifiers = field.getModifiers();
        String modifierString = java.lang.reflect.Modifier.toString(modifiers);
        List<Modifier> modifiersList = parseModifiers(modifierString);
        for (Modifier modifier : modifiersList) {
            fieldNode.addModifier(modifier);
        }
        return fieldNode;
    }

    private List<Modifier> parseModifiers(String modifierString) {
        List<Modifier> modifiers = new ArrayList<>();
        String[] splitted = modifierString.split("\\s");
        for (String modString : splitted) {
            Modifier modifier = Modifier.valueOf(modString.toUpperCase());
            modifiers.add(modifier);
        }
        return modifiers;
    }

    @Override
    public MethodNode modelMethod(Method method) {
        MethodNode methodNode = createMethodNode(method.toGenericString(), method.getDeclaringClass());
        for (Parameter param : method.getParameters()) {
            ParamNode paramNode = modelParam(param);
            methodNode.addParam(paramNode);
        }
        return methodNode;
    }

    @Override
    public ParamNode modelParam(Parameter parameter) {
        VariableTypeNode vt = ModelUtil.parseVariableType(parameter.getType().getTypeName());
        JlsName paramName = NameUtil.createMemberName(parameter.getName());
        return new ParamNode(vt, paramName);
    }

    public AnnotationNode modelAnnotation(Annotation annotation) {
        Class<?> annotationType = annotation.annotationType();
        JlsName annoPackageName = NameUtil.createPackageName(annotationType.getPackageName());
        JlsName annoName = NameUtil.createTypeName(annoPackageName, annotationType.getName());
        AnnotationNode annotationNode = new AnnotationNode(annoName);
        // TODO: Other details
        return annotationNode;
    }

    private TypeNode modelType(Class<?> type, JavaSemanticNode.Kind kind) {
        TypeNode typeNode;
        JlsName packageName = NameUtil.createPackageName(type.getPackageName());
        JlsName name = NameUtil.createTypeName(packageName, type.getName());
        switch(kind) {
            case ANNOTATION:
                typeNode = new AnnotationNode(name);
                break;
            case CLASS:
                typeNode = new ClassNode(name);
                break;
            case ENUM:
                typeNode = new EnumNode(name);
                break;
            case INTERFACE:
                typeNode = new InterfaceNode(name);
                break;
            case RECORD:
                typeNode = new RecordNode(name);
                break;
            default:
                typeNode = new TypeNode(name);
        }

        typeNode.setModuleName(type.getModule().getName());

        for (Field field : type.getFields()) {
            FieldNode node = modelField(field);
            typeNode.addField(node);
        }
        for (Method method : type.getMethods()) {
            MethodNode node = modelMethod(method);
            typeNode.addMethod(node);
        }
        return typeNode;
    }

    private MethodNode createMethodNode(String method, Class<?> owner) {
        int openParenthesis = method.indexOf("(");
        String modifiersTypeAndName = method.substring(0, openParenthesis);
        int pos = modifiersTypeAndName.lastIndexOf(" ");
        String qualifiedName = modifiersTypeAndName.substring(pos + 1);
        String modifiersAndType = modifiersTypeAndName.substring(0, pos);
        pos = qualifiedName.lastIndexOf(".");
        String simpleName = qualifiedName.substring(pos + 1);
        String returnType = "";
        StringBuilder modifiers = new StringBuilder();
        String[] parts = modifiersAndType.split(" ");
        pos = 0;
        while (pos < parts.length) {
            String part = parts[pos];
            Modifier mod = Modifier.DEFAULT;
            try{
                mod = Modifier.valueOf(part.toUpperCase());
                if (!modifiers.isEmpty()) {
                    modifiers.append(" ");
                }
                modifiers.append(mod.name());
            }catch(IllegalArgumentException e) {
                returnType = modifiersAndType.substring(modifiers.length()).strip();
            }
            pos++;
        }
        JlsName packageName = NameUtil.createPackageName(owner.getPackageName());
        JlsName typeName = NameUtil.createTypeName(packageName, owner.getName());
        JlsName name = NameUtil.createMemberName(typeName, simpleName);
        MethodNode methodNode = new MethodNode(returnType, name);
        parts = modifiers.toString().split(" ");
        for (String part : parts) {
            methodNode.addModifier(Modifier.valueOf(part));
        }
        return methodNode;
    }
}
