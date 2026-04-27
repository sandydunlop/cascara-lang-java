package io.github.qishr.cascara.lang.java.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

///
/// See Java Language Specification (JLS) [JlsChapter 4. Types, Values, and Variables](https://docs.oracle.com/javase/specs/jls/se24/html/jls-4.html).
public class VariableTypeNode implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    String fullTypeName = "";
    Link link = null;
    String rawTypeName = "";
    int arrayDimensions = 0;
    VariableTypeNode typeParameterDeclaration = null;
    String variableName = "";
    boolean isVar = false;

    public boolean isVar() {
        return isVar;
    }

    public void setVar(boolean isVar) {
        this.isVar = isVar;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setIdentifier(String variableName) {
        this.variableName = variableName;
    }

    public VariableTypeNode() {
        // Nothing to see here
    }

    public boolean isArray() {
        return arrayDimensions > 0;
    }

    public int arrayDimensions() {
        return arrayDimensions;
    }


    public String getRawTypeName() {
        return rawTypeName;
    }

    public Link getLink() {
        return link;
    }

    public void setRawTypeName(String name) {
        rawTypeName = name;
    }

    public void setFullTypeName(String name) {
        fullTypeName = name;
    }

    public String getFullTypeName() {
        return fullTypeName;
    }

    public VariableTypeNode getTypeParameterDeclaration() {
        return typeParameterDeclaration;
    }

    public Generic asGeneric() {
        if (this instanceof Generic parameterized) {
            return parameterized;
        }
        return null;
    }

    public Sequence asSequence() {
        if (this instanceof Sequence sequence) {
            return sequence;
        }
        return null;
    }

    public String toString() {
        return ModelUtil.stringify(this);
    }


    public enum BoundingKind {
        NONE,
        UPPER,
        LOWER
    }

    public static class TypeParameter extends VariableTypeNode {
        String boundingParameter = null;
        BoundingKind boundingKind = BoundingKind.NONE;

        TypeParameter() {
            // Nothing to see here
        }


        public String getBoundingParameter() {
            return boundingParameter;
        }

        public BoundingKind getBoundingKind() {
            return boundingKind;
        }
    }

    public static class Generic extends TypeParameter {
        TypeParameter params;

        Generic() {
            // Nothing to see here
        }

        public TypeParameter getParams() {
            return params;
        }
    }

    public static class Sequence extends TypeParameter implements Iterable<TypeParameter> {
        TypeParameter[] elements;
        int size;
        static final int INITIAL_CAPACITY = 4;

        Sequence() {
            elements = new TypeParameter[INITIAL_CAPACITY];
            size = 0;
        }

        private void resize() {
            int newCapacity = elements.length * 2;
            TypeParameter[] newArray = new TypeParameter[newCapacity];
            System.arraycopy(elements, 0, newArray, 0, elements.length);
            elements = newArray;
        }

        public int size() {
            return size;
        }

        public void append(TypeParameter element) {
            if (element instanceof Sequence sequence) {
                for (TypeParameter typeRef : sequence) {
                    append(typeRef);
                }
            } else {
                if (size == elements.length) {
                    resize();
                }
                elements[size++] = element;
            }
        }

        public TypeParameter get(int index) {
            if (index >= size) {
                throw new NoSuchElementException();
            }
            return elements[index];
        }

        public TypeParameter getFirst() {
            return get(0);
        }

        public TypeParameter getLast() {
            return get(size - 1);
        }

        @Override
        public Iterator<TypeParameter> iterator() {
            return new SequenceIterator();
        }

        class SequenceIterator implements Iterator<TypeParameter> {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public TypeParameter next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[currentIndex++];
            }
        }
    }
}
