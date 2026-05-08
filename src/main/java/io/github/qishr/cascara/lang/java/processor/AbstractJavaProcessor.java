package io.github.qishr.cascara.lang.java.processor;

import io.github.qishr.cascara.common.diagnostic.Reporter;
import io.github.qishr.cascara.common.lang.LanguageOptions;
import io.github.qishr.cascara.common.lang.processor.Processor;
import io.github.qishr.cascara.common.util.ContentType;
import io.github.qishr.cascara.common.util.Properties;
import io.github.qishr.cascara.lang.java.JavaOptions;

public abstract class AbstractJavaProcessor<P extends Processor> implements Processor {
    static final ContentType contentType = new ContentType("JSON")
            .withMimeType("text/json")
            .withMimeType("application/json")
            .withMimeType("application/schema+json")
            .withSuffix(".json");

    protected JavaOptions options = new JavaOptions();
    protected Reporter reporter;
    private Properties capabilities;

    protected abstract P self();

    public Properties getCapabilities() {
        if (capabilities == null) {
            capabilities = new Properties();
            capabilities.set("processor.contentType", "application/json");
        }
        return capabilities;
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    /// {@inheritDoc}
    @Override
    public P setReporter(Reporter reporter) {
        this.reporter = reporter;
        return self();
    }

    /// {@inheritDoc}
    @Override
    public P setOptions(LanguageOptions<?> options) {
        this.options = (JavaOptions) options;
        return self();
    }
}
