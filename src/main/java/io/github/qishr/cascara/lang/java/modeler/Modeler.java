package io.github.qishr.cascara.lang.java.modeler;

import io.github.qishr.cascara.lang.java.model.ClassNode;
import io.github.qishr.cascara.lang.java.model.FieldNode;
import io.github.qishr.cascara.lang.java.model.MethodNode;
import io.github.qishr.cascara.lang.java.model.ModuleNode;
import io.github.qishr.cascara.lang.java.model.PackageNode;
import io.github.qishr.cascara.lang.java.model.ParamNode;
import io.github.qishr.cascara.lang.java.model.TypeNode;

public interface Modeler<A,B,C,F,M,P> {
    public ModuleNode modelModule(A m);
    public PackageNode modelPackage(B p);
    public TypeNode modelType(C t);
    public ClassNode modelClass(C t);
    public FieldNode modelField(F f);
    public MethodNode modelMethod(M m);
    public ParamNode modelParam(P p);
}

