package org.konveyor.dgi.code2graph.utils.graph;

import com.ibm.wala.ipa.slicer.Statement;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

public class SystemDepEdge extends AbstractGraphEdge {
    public final Integer sourcePos;
    public final Integer destinationPos;
    public final String type;
    public static final long serialVersionUID = -8284030936836318929L;

    public SystemDepEdge(Statement sourceStatement, Statement destinationStatement, String type) {
        super();
        this.type = type;
        this.sourcePos = getStatementPosition(sourceStatement);
        this.destinationPos = getStatementPosition(destinationStatement);
    }
    public SystemDepEdge(Statement sourceStatement, Statement destinationStatement, String type, String context) {
        super(context);
        this.type = type;
        this.sourcePos = getStatementPosition(sourceStatement);
        this.destinationPos = getStatementPosition(destinationStatement);
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(sourcePos).append(destinationPos).append(context).append(type).build();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof SystemDepEdge) && (this.toString().equals(o.toString())) && Integer.valueOf(this.hashCode()).equals(o.hashCode()) && this.type.equals(((SystemDepEdge) o).getType());
    }
    public String getType() { return type; }
    public Integer getSourcePos() {
        return sourcePos;
    }

    public Integer getDestinationPos() {
        return destinationPos;
    }

    public Map<String, Attribute> getAttributes() {
        Map<String, Attribute> map = new LinkedHashMap<>();
        map.put("id", DefaultAttribute.createAttribute(getId()));
        map.put("type", DefaultAttribute.createAttribute(getType()));
        map.put("weight", DefaultAttribute.createAttribute(getWeight()));
        map.put("context", DefaultAttribute.createAttribute(getContext()));
        map.put("sourcePos", DefaultAttribute.createAttribute(getSourcePos()));
        map.put("destinationPos", DefaultAttribute.createAttribute(getDestinationPos()));
        return map;
    }
}