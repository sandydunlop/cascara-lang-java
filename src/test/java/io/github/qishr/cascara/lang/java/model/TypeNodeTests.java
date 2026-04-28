package io.github.qishr.cascara.lang.java.model;

import org.junit.jupiter.api.Test;

import io.github.qishr.cascara.lang.java.model.JlsName;
import io.github.qishr.cascara.lang.java.model.Modifier;
import io.github.qishr.cascara.lang.java.model.NameUtil;
import io.github.qishr.cascara.lang.java.model.TypeNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeNodeTests {
    @Test
    void bla() {
        JlsName packageName = NameUtil.createPackageName("com.exmaple");
        TypeNode typeNode = new TypeNode(NameUtil.createTypeName(packageName, "com.example.Test"));
        typeNode.addModifier(Modifier.STATIC);
        typeNode.addModifier(Modifier.FINAL);

        String actual = typeNode.getModifiersString();
        assertEquals("static final ", actual);
    }
}
