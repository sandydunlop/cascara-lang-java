package io.github.qishr.cascara.lang.java;

import io.github.qishr.cascara.common.lang.LanguageOptions;

public class JavaOptions extends LanguageOptions<JavaOptions> {
    private boolean reportStack = false;
    private boolean reportTokens = false;
    private boolean trace = false;
    private boolean verbose = false;
    private boolean includeComments = false;

    public boolean getReportStack() {
        return reportStack;
    }
    public JavaOptions setReportStack(boolean reportStack) {
        this.reportStack = reportStack;
        return this;
    }
    public boolean getReportTokens() {
        return reportTokens;
    }
    public JavaOptions setReportTokens(boolean reportTokens) {
        this.reportTokens = reportTokens;
        return this;
    }
    public boolean getTrace() {
        return trace;
    }
    public JavaOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean getIncludeComments() {
        return includeComments;
    }

    public void setIncludeComments(boolean includeComments) {
        this.includeComments = includeComments;
    }
}
