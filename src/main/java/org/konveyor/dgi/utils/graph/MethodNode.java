package org.konveyor.dgi.utils.graph;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeReference;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MethodNode extends AbstractGraphNode {

    public final String id;
    public final String method;
    public final String returnType;
    public final List<String> arguments;
    public final String className;
    public final String classShortName;

    public MethodNode(IMethod method) {
        this.method = method.getName().toString();
        this.className = method.getDeclaringClass()
                .getName()
                .toString()
                .substring(1)
                .replace("/", ".");
        this.classShortName = className
                .substring(className.lastIndexOf('/') + 1)
                .replace("$", "_");
        this.id = generateNodeId();
        this.returnType = method.getReturnType()
                .getName()
                .toString()
                .substring(1)
                .replace("/", ".");
        this.arguments = getArgumentsList(method);
    }

    private List<String> getArgumentsList(IMethod method) {
        return IntStream.range(0, method.getNumberOfParameters())
                .mapToObj(i -> method.getParameterType(i)
                        .getName()
                        .toString()
                        .substring(1)
                        .replace("/", "."))
                .collect(Collectors.toList());
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

    public List<String> getArguments() {
        return arguments;
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public Map<String, Attribute> getAttributes() {
        Map<String, Attribute> map = new LinkedHashMap<>();
        map.put("id", DefaultAttribute.createAttribute(getId()));
        map.put("class", DefaultAttribute.createAttribute(getClassName()));
        map.put("method", DefaultAttribute.createAttribute(getMethod()));
        map.put("arguments", DefaultAttribute.createAttribute(getArguments().toString()));
        map.put("returnType", DefaultAttribute.createAttribute(getReturnType()));
        return map;
    }
}

