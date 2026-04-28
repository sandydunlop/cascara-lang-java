package io.github.qishr.cascara.lang.java.model;

import org.junit.jupiter.api.Test;

import io.github.qishr.cascara.lang.java.model.JlsName;
import io.github.qishr.cascara.lang.java.model.NameUtil;
import io.github.qishr.cascara.lang.java.model.Reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;

class ReferenceTests {
    @Test
    void test_toString() {
        Reference ref = NameUtil.createReference("java.base/java.lang.String#toString()");
        assertEquals("java.base", ref.getModuleName());
        JlsName name = ref.getName();
        assertTrue(name.isMember());
        assertEquals("java.base/java.lang.String.toString()", ref.toString());
    }

    @Test
    void test_toString_noModule_1() {
        Reference ref = NameUtil.createReference("java.lang.String#toString()");
        assertEquals("", ref.getModuleName());
        JlsName name = ref.getName();
        assertTrue(name.isMember());
        assertEquals("java.lang.String.toString()", ref.toString());
    }

    @Test
    void test_toString_noModule_2() {
        Reference ref = NameUtil.createReference("java.lang.String#toString()");
        ref.setModule(null);
        assertNull(ref.getModuleName());
        JlsName name = ref.getName();
        assertTrue(name.isMember());
        assertEquals("java.lang.String.toString()", ref.toString());
    }

    @Test
    void test_toString_module_nameEmpty() {
        Reference ref = NameUtil.createReference("java.base/");
        ref.setName(new JlsName());
        assertEquals("java.base", ref.getModuleName());
        assertEquals("java.base/", ref.toString());
    }

    @Test
    void test_toString_module_nameUnset() {
        Reference ref = NameUtil.createReference("java.base/");
        assertEquals("java.base", ref.getModuleName());
        assertEquals("java.base/", ref.toString());
    }

    @Disabled
    @Test
    void test_method_only() {
        Reference ref = NameUtil.createReference("#methodname");
        assertEquals("methodname", ref.getName().simpleName());
    }

    @Test
    void test_null() {
        assertThrows(IllegalArgumentException.class, () -> {
            NameUtil.createReference(null);
        });
    }

    @Test
    void test_blank() {
        assertThrows(IllegalArgumentException.class, () -> {
            NameUtil.createReference("");
        });
    }
}
