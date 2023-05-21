package org.konveyor.dgi.code2graph.utils.graph;

import java.io.Serializable;
import java.util.Map;
import org.jgrapht.nio.Attribute;


public abstract class AbstractGraphNode implements Serializable {

    public abstract String getId();

    public abstract Map<String, Attribute> getAttributes();
}