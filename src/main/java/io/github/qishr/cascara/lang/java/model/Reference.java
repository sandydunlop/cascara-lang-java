package io.github.qishr.cascara.lang.java.model;

import java.io.Serial;
import java.io.Serializable;

public class Reference implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;

    JlsName name;
    String module = "";
    String parameters = "";

    public Reference() {

    }

    public void setModule(String n) {
        module = n;
    }

    public String getModuleName() {
        return module;
    }

    public void setName(JlsName n) {
        name = n;
    }

    public JlsName getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (module != null && !module.isEmpty()) {
            sb.append(module);
            sb.append("/");
        }
        if (name != null && !name.isEmpty()) {
            sb.append(name);
        }
        sb.append(parameters);
        return sb.toString();
    }
}
