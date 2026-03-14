package io.github.qishr.cascara.lang.java.token;

public enum SemanticType {
    /** Default for all non-identifier tokens and identifiers that couldn't be resolved. */
    NONE,

    GLOBAL,

    /** Identifies the name of a class, record, or enum. */
    CLASS_NAME,

    /** Identifies the name of an interface or annotation type. */
    INTERFACE_NAME,

    /** Identifies the name of an annotation (e.g., @Override, @SuppressWarnings). */
    ANNOTATION_NAME,

    /** Identifies the name of a method, including constructors. */
    METHOD_NAME,

    /** Identifies a field (instance variable) or static variable within a class. */
    FIELD_NAME,

    /** Identifies a local variable, including formal parameters in method declarations. */
    LOCAL_VARIABLE_NAME,

    /** Identifies the name of a package or part of a package name (e.g., 'java' in java.util). */
    PACKAGE_NAME,

    /** Reserved for primitive types that are often highlighted differently (e.g., int, boolean).
     * These are typically handled by JavaTokenType.KEYWORD_INT, etc., but included here for completeness
     * if the Analyzer needs to differentiate them from Class names in Type contexts. */
    PRIMITIVE_TYPE,

    PARAMETER_NAME,
    ENUM_CONSTANT,
    TYPE_REFERENCE,
    OPERATOR,
    MODIFIER,
    CONSTRUCTOR_NAME

}