package org.konveyor.dgi.code2graph.utils.graph;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodNode extends AbstractGraphNode {

    public final String id;
    public final String method;
    public final String className;
    public final String classShortName;

    public MethodNode(String method, String className) {
        this.method = method;
        this.className = className.substring(1).replace("/", ".");
        this.classShortName = className
                .substring(className.lastIndexOf('/') + 1)
                .replace("$", "_");
        this.id = generateNodeId();
    }

    private String generateNodeId() {
        return getMethodSignature();
    }

    public String getId() {
        return id;
    }

    public String getMethodSignature() {
        return className + "." + method;
    }
    public String getClassSignature() {
        return className;
    }

    @Override
    public String toString() {
        return method;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MethodNode) && (toString().equals(o.toString()));
    }

    public String getMethod() {
        return method;
    }

    public String getClassName() {
        return classShortName;
    }

    @Override
    public Map<String, Attribute> getAttributes() {
        Map<String, Attribute> map = new LinkedHashMap<>();
        map.put("id", DefaultAttribute.createAttribute(getId()));
        map.put("class", DefaultAttribute.createAttribute(getClassName()));
        map.put("method", DefaultAttribute.createAttribute(getMethod()));
        return map;
    }
}

