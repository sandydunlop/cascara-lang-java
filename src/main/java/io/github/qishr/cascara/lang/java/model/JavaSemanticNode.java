package io.github.qishr.cascara.lang.java.model;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.qishr.cascara.common.lang.semantic.SemanticNode;

/// The base class for all types of nodes in the API model.
public abstract class JavaSemanticNode implements Serializable, SemanticNode {
    @Serial
    private static final long serialVersionUID = 1L;

    /// A unique identifier
    protected UUID uuid = UUID.randomUUID();

    /// The kind of this type (e.g., class, interface, enum, annotation).
    protected JavaSemanticNode.Kind kind = JavaSemanticNode.Kind.NONE;

    /// The deprecation status of the node
    private Deprecation deprecation = Deprecation.NONE;

    /// Text describing the deprecation state of the node
    private Text deprecationText = Text.empty();

    /// Text showing when this node was added to the API
    private Text since = Text.empty();

    /// The body of the Javadoc for this node, not including the first sentence
    private Text body = Text.empty();

    /// The first sentence of the Javadoc for this node
    private Text firstSentence = Text.empty();

    /// A list of references specified in this node's Javadoc
    private List<Link> references = new ArrayList<>();

    protected JlsName name = null;

    protected JavaSemanticNode parent = null;

    private int line = 0;

    private int column = 0;

    /// The default constructor
    protected JavaSemanticNode() {
        // Only here for the Javadoc
    }

    public JlsName getName() {
        return name;
    }

    public void setName(JlsName name) {
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public JavaSemanticNode getParent() {
        return parent;
    }

    public void setParent(JavaSemanticNode parent) {
        this.parent = parent;
    }

    /// Sets the kind (class, interface, enum, annotation) of this type.
    /// @param kind the Kind enum value.
    public void setKind(JavaSemanticNode.Kind kind) {
        this.kind = kind;
    }

    /// Returns the kind of this type.
    /// @return the Kind enum value.
    public JavaSemanticNode.Kind getKind() {
        return kind;
    }

    /// Sets the deprecation status for this node.
    /// @param deprecation The deprecation enum value.
    public void setDeprecation(Deprecation deprecation) {
        this.deprecation = deprecation;
    }

    /// Retrieves the deprecation status of this node.
    /// @return The deprecation enum value.
    public Deprecation getDeprecation() {
        return deprecation;
    }

    /// Sets the deprecation text.
    /// @param text The text describing the deprecation.
    public void setDeprecationText(Text text) {
        deprecationText = text;
    }

    /// Returns the deprecation text.
    /// @return The deprecation descriptive text.
    public Text getDeprecationText() {
        return deprecationText;
    }

    /// Sets the 'since' documentation text.
    /// @param text The since text.
    public void setSince(Text text) {
        since = text;
    }

    /// Returns the 'since' documentation text.
    /// @return The since text.
    public Text getSince() {
        return since;
    }

    /// Sets the full body documentation text including tags.
    /// @param t The full body text.
    public void setFirstSentence(Text t) {
        firstSentence = t;
    }

    /// Returns the first sentence of the documentation.
    /// @return The first sentence text.
    public Text getFirstSentence() {
        return firstSentence;
    }

    /// Sets the body documentation text including tags.
    /// @param t The body text.
    public void setBody(Text t) {
        body = t;
    }

    /// Returns the body documentation text including tags.
    /// @return The body text.
    public Text getBody() {
        return body;
    }

    /// Gets the full body documentation text including tags.
    /// @return The full body text.
    public Text getFullBody() {
        Text fullBody = Text.empty();
        fullBody.append(firstSentence);
        fullBody.append(body);
        return fullBody;
    }

    /// Sets the list of references for this node.
    /// @param refs List of Reference objects.
    public void setReferences(List<Link> refs) {
        references = refs;
    }

    /// Returns the list of references associated with this node.
    /// @return List of Reference objects.
    public List<Link> getReferences() {
        return references;
    }

    public enum Kind {
        /// No type has been set
        NONE ("None"),

        MODULE ("Module"),

        PACKAGE ("Package"),

        /// A class, including abstract class and exception class
        CLASS ("Class"),

        /// An interface
        INTERFACE ("Interface"),

        /// A record
        RECORD ("Record"),

        /// An enum
        ENUM ("Enum"),

        /// An annotation
        ANNOTATION ("Annotation Interface"),

        /// An annotation
        FIELD ("Field"),

        /// An annotation
        METHOD ("Method"),

        /// An annotation
        PARAMETER ("Parameter");

        /// The display name for the kind.
        private final String name;

        /// Constructor assigning the display name.
        Kind(String s) {
            name = s;
        }

        /// Returns the display name of the kind.
        @Override
        public String toString() {
            return this.name;
        }
    }

    @Override
    public List<JavaSemanticNode> getChildren() {
        return List.of();
    }

    @Override
    public String getId() {
        if (name == null) {
            throw new IllegalStateException("JLS name is null");
        }
        return name.toString();
    }

    @Override
    public String getDisplayName() {
        if (name == null) {
            throw new IllegalStateException("JLS name is null");
        }
        return name.simpleName().toString();
    }

    /// Recursively find the source path by asking parents
    /// This method is overridden in subclasses that have a path field
    @Override
    public Path getSourcePath() {
        if (getParent() instanceof JavaSemanticNode javaParent) {
            return javaParent.getSourcePath();
        }
        return null; // Root reached with no path found
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
