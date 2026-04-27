package io.github.qishr.cascara.lang.java.model;

import io.github.qishr.cascara.common.util.Pair;
import io.github.qishr.cascara.lang.java.model.VariableTypeNode.BoundingKind;
import io.github.qishr.cascara.lang.java.model.VariableTypeNode.Generic;
import io.github.qishr.cascara.lang.java.model.VariableTypeNode.Sequence;
import io.github.qishr.cascara.lang.java.model.VariableTypeNode.TypeParameter;

public class ModelUtil {

    private ModelUtil() {
        // Hide the constructor
    }

    //


    public static VariableTypeNode parseVariableType(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return new VariableTypeNode();
        }
        VariableTypeNode typeParam = null;
        String fullTypeName = typeName.strip();
        if (fullTypeName.startsWith("<")) {
            int closingTypeParamChevron = matchingClosingChevron(fullTypeName, 0);
            String typeParamString = fullTypeName.substring(0, closingTypeParamChevron + 1);
            typeParam = parseGeneric(typeParamString.strip());
            fullTypeName = fullTypeName.substring(closingTypeParamChevron + 1).strip();
        }

        VariableTypeNode type = parseType(fullTypeName);
        type.typeParameterDeclaration = typeParam;
        if (type instanceof Generic generic) {
            generic.setFullTypeName(fullTypeName);
            return generic;
        }

        // Hide whatever subtype `type` might be...
        VariableTypeNode variableType = new VariableTypeNode();
        variableType.fullTypeName = fullTypeName;
        variableType.rawTypeName = type.rawTypeName;
        variableType.typeParameterDeclaration = typeParam;
        variableType.link = type.link;
        variableType.fullTypeName = fullTypeName;
        variableType.arrayDimensions = type.arrayDimensions;
        return variableType;
    }

    private static int matchingClosingChevron(String string, int pos) {
        int depth = 0;
        for (; pos < string.length(); pos++) {
            char c = string.charAt(pos);
            if (c=='<') {
                depth++;
            } else if (c== '>') {
                depth--;
            }
            if (depth == 0) break;
        }
        return pos;
    }

    public static String stringify(VariableTypeNode typeReference) {
        StringBuilder sb = new StringBuilder();
        if (typeReference.typeParameterDeclaration != null) {
            sb.append(typeReference.typeParameterDeclaration);
            sb.append(" ");
        }
        switch (typeReference) {
            case Generic generic -> {
                sb.append(generic.rawTypeName);
                sb.append("<");
                sb.append(boundingConstraint(generic));
                sb.append(stringify(generic.params));
                sb.append(">");
            }
            case Sequence sequence -> {
                for (VariableTypeNode element : sequence) {
                    if (!sb.isEmpty()) {
                        sb.append(",");
                    }
                    sb.append(stringify(element));
                }
            }
            case TypeParameter typeParameter -> {
                sb.append(boundingConstraint(typeParameter));
                sb.append(typeParameter.rawTypeName);
            }
            default -> sb.append(typeReference.rawTypeName);
        }
        for (int d = 0; d < typeReference.arrayDimensions; d++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    private static String boundingConstraint(TypeParameter type) {
        if (type.getBoundingKind() == BoundingKind.UPPER) {
            return type.getBoundingParameter() + " extends ";
        } else if (type.getBoundingKind() == BoundingKind.LOWER) {
            return type.getBoundingParameter() + " super ";
        }
        return "";
    }

    //

    private static TypeParameter parseType(String str) {
        int lastComma = str.lastIndexOf(",");
        int lastChevron = str.lastIndexOf(">");

        if (lastComma > -1 && lastChevron > -1) {
            if (lastComma > lastChevron) {
                return parseSequence(str, null);
            } else {
                return parseGeneric(str);
            }
        } else if (lastComma > -1) {
            return parseSequence(str, null);
        } else if (lastChevron > -1) {
            return parseGeneric(str);
        }
        TypeParameter typeParameter = new TypeParameter();
        parseTypeParameter(str, typeParameter);
        return typeParameter;
        // return new TypeParameter(str);
    }

    private static TypeParameter parseSequence(String before, TypeParameter inner) {
        int comma = before.lastIndexOf(",");
        String beforeComma = before.substring(0, comma).strip();
        String typeName = before.substring(comma + 1).strip();
        TypeParameter typeRefAfterComma;

        if (inner != null) {
            Generic generic = new Generic();
            parseTypeParameter(typeName, generic);
            generic.params = inner;
            typeRefAfterComma = generic;
        } else {
            typeRefAfterComma = parseType(typeName);
        }

        TypeParameter typeRefBeforeComma = parseType(beforeComma);
        Sequence sequence = new Sequence();
        sequence.append(typeRefBeforeComma);
        sequence.append(typeRefAfterComma);
        return sequence;
    }

    private static TypeParameter parseGeneric(String str) {
        int closingChevron = str.indexOf(">");
        int openingChevron = str.lastIndexOf("<", closingChevron);
        String before = str.substring(0, openingChevron).strip();
        String mid = str.substring(openingChevron + 1, closingChevron).strip();
        String after = str.substring(closingChevron + 1).strip();
        TypeParameter inner = null;

        // `T extends` is called a type bound. It is a constraint rather than a statement or expression.
        // `<T extends Type>` is a type parameter declaration
        Pair<String,String> upperBound = parseBoundingConstraint("extends", mid);
        Pair<String,String> lowerBound = parseBoundingConstraint("super", mid);
        BoundingKind boundingType = BoundingKind.NONE;
        String typeParameter = "";
        if (upperBound != null) {
            boundingType = BoundingKind.UPPER;
            typeParameter = upperBound.getL();
            mid = upperBound.getR();
        } else if (lowerBound != null) {
            boundingType = BoundingKind.LOWER;
            typeParameter = lowerBound.getL();
            mid = lowerBound.getR();
        }

        inner = parseType(mid);

        if (before.contains(",")) {
            return parseSequence(before, inner);
        }

        Generic generic = new Generic();
        parseTypeParameter(before, generic);
        generic.params = inner;
        generic.boundingKind = boundingType;
        generic.boundingParameter = typeParameter;
        parseArray(after, generic);
        return generic;
    }

    private static Pair<String,String> parseBoundingConstraint(String keyword, String type) {
        String nameOrWildcard = "";
        String boundingType = "";
        int nameStart = -1;
        int boundingTypeStart = -1;
        int pos = 0;
        while (pos < type.length()) {
            char c = type.charAt(pos);
            if (nameStart == -1 && !Character.isWhitespace(c)) {
                nameStart = pos;
            } else if (nameStart > -1 && boundingTypeStart == -1 && Character.isWhitespace(c)) {
                nameOrWildcard = type.substring(nameStart, pos);
            } else if (boundingTypeStart == -1 && !Character.isWhitespace(c)) {
                boundingTypeStart = pos;
            } else if (boundingTypeStart > -1 && Character.isWhitespace(c)) {
                boundingType = type.substring(boundingTypeStart, pos);
                if (boundingType.equals(keyword)) {
                    String remainder = type.substring(pos + 1);
                    return Pair.of(nameOrWildcard,remainder);
                }
            }
            pos++;
        }
        return null;
    }

    private static void parseArray(String str, TypeParameter type) {
        int pos = 0;
        while (pos < str.length()) {
            char c = str.charAt(pos);
            if (c == ']' || Character.isWhitespace(c)) {
                pos++;
            } else if (c == '[') {
                type.arrayDimensions++;
                pos++;
            } else {
                break;
            }
        }
    }

    static void parseTypeParameter(String str, TypeParameter typeParameter) {
        if (str.isEmpty()) return;

        Pair<String,String> upperBound = parseBoundingConstraint("extends", str);
        Pair<String,String> lowerBound = parseBoundingConstraint("super", str);
        BoundingKind boundingType = BoundingKind.NONE;
        String typeParameterStr = "";
        if (upperBound != null) {
            boundingType = BoundingKind.UPPER;
            typeParameterStr = upperBound.getL();
            str = upperBound.getR();
        } else if (lowerBound != null) {
            boundingType = BoundingKind.LOWER;
            typeParameterStr = lowerBound.getL();
            str = lowerBound.getR();
        }
        typeParameter.boundingKind = boundingType;
        typeParameter.boundingParameter = typeParameterStr;

        int bracket = str.indexOf("[");
        if (bracket > -1) {
            typeParameter.rawTypeName = str.substring(0, bracket);
            parseArray(str.substring(bracket), typeParameter);
        } else {
            typeParameter.rawTypeName = str;
        }
        if (!typeParameter.rawTypeName.equals("?")) {
            try {
                Reference ref = NameUtil.createReference(typeParameter.rawTypeName);
                typeParameter.link = Link.to(ref);
            } catch (IllegalArgumentException e) {
                // This should never happen
            }
        }
    }

}
