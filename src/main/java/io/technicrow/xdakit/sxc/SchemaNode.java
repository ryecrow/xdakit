package io.technicrow.xdakit.sxc;

import io.technicrow.xdakit.constant.DataType;
import io.technicrow.xdakit.constant.SchemaType;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Schema Node Information
 */
public class SchemaNode {

    private Integer index;

    private SchemaType schemaNodeType;

    private Integer schemaNodeNameLength;

    private String schemaNodeName;

    private DataType dataType;

    private Integer minOccurs;

    private Integer maxOccurs;

    private boolean attributeRequired;

    private boolean mixed;

    private Integer attributeCount;

    private List<Integer> attributes;

    private Integer childrenCount;

    private List<Integer> children;

    SchemaNode(Integer index, SchemaType schemaNodeType, Integer schemaNodeNameLength, String schemaNodeName, DataType dataType, Integer minOccurs, Integer maxOccurs, boolean attributeRequired, boolean mixed, Integer attributeCount, List<Integer> attributes, Integer childrenCount, List<Integer> children) {
        this.index = index;
        this.schemaNodeType = schemaNodeType;
        this.schemaNodeNameLength = schemaNodeNameLength;
        this.schemaNodeName = schemaNodeName;
        this.dataType = dataType;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.attributeRequired = attributeRequired;
        this.mixed = mixed;
        this.attributeCount = attributeCount;
        this.attributes = attributes;
        this.childrenCount = childrenCount;
        this.children = children;
    }

    public static SchemaNodeBuilder builder() {
        return new SchemaNodeBuilder();
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public SchemaType getSchemaNodeType() {
        return this.schemaNodeType;
    }

    public void setSchemaNodeType(SchemaType schemaNodeType) {
        this.schemaNodeType = schemaNodeType;
    }

    public Integer getSchemaNodeNameLength() {
        return this.schemaNodeNameLength;
    }

    public void setSchemaNodeNameLength(Integer schemaNodeNameLength) {
        this.schemaNodeNameLength = schemaNodeNameLength;
    }

    public String getSchemaNodeName() {
        return this.schemaNodeName;
    }

    public void setSchemaNodeName(String schemaNodeName) {
        this.schemaNodeName = schemaNodeName;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Integer getMinOccurs() {
        return this.minOccurs;
    }

    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    public Integer getMaxOccurs() {
        return this.maxOccurs;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public boolean isAttributeRequired() {
        return this.attributeRequired;
    }

    public void setAttributeRequired(boolean attributeRequired) {
        this.attributeRequired = attributeRequired;
    }

    public boolean isMixed() {
        return this.mixed;
    }

    public void setMixed(boolean mixed) {
        this.mixed = mixed;
    }

    public Integer getAttributeCount() {
        return this.attributeCount;
    }

    public void setAttributeCount(Integer attributeCount) {
        this.attributeCount = attributeCount;
    }

    public List<Integer> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(List<Integer> attributes) {
        this.attributes = attributes;
    }

    public Integer getChildrenCount() {
        return this.childrenCount;
    }

    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }

    public List<Integer> getChildren() {
        return this.children;
    }

    public void setChildren(List<Integer> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SchemaNode that = (SchemaNode) o;

        return new EqualsBuilder().append(attributeRequired, that.attributeRequired).append(mixed, that.mixed).append(index, that.index).append(schemaNodeType, that.schemaNodeType).append(schemaNodeNameLength, that.schemaNodeNameLength).append(schemaNodeName, that.schemaNodeName).append(dataType, that.dataType).append(minOccurs, that.minOccurs).append(maxOccurs, that.maxOccurs).append(attributeCount, that.attributeCount).append(attributes, that.attributes).append(childrenCount, that.childrenCount).append(children, that.children).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(index).append(schemaNodeType).append(schemaNodeNameLength).append(schemaNodeName).append(dataType).append(minOccurs).append(maxOccurs).append(attributeRequired).append(mixed).append(attributeCount).append(attributes).append(childrenCount).append(children).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("index", index)
                .append("schemaNodeType", schemaNodeType)
                .append("schemaNodeNameLength", schemaNodeNameLength)
                .append("schemaNodeName", schemaNodeName)
                .append("dataType", dataType)
                .append("minOccurs", minOccurs)
                .append("maxOccurs", maxOccurs)
                .append("attributeRequired", attributeRequired)
                .append("mixed", mixed)
                .append("attributeCount", attributeCount)
                .append("attributes", attributes)
                .append("childrenCount", childrenCount)
                .append("children", children)
                .toString();
    }

    public static class SchemaNodeBuilder implements Builder<SchemaNode> {
        private Integer index;
        private SchemaType schemaNodeType;
        private Integer schemaNodeNameLength;
        private String schemaNodeName;
        private DataType dataType;
        private Integer minOccurs;
        private Integer maxOccurs;
        private boolean attributeRequired;
        private boolean mixed;
        private Integer attributeCount;
        private List<Integer> attributes;
        private Integer childrenCount;
        private List<Integer> children;

        SchemaNodeBuilder() {
        }

        public SchemaNodeBuilder index(Integer index) {
            this.index = index;
            return this;
        }

        public SchemaNodeBuilder schemaNodeType(SchemaType schemaNodeType) {
            this.schemaNodeType = schemaNodeType;
            return this;
        }

        public SchemaNodeBuilder schemaNodeNameLength(Integer schemaNodeNameLength) {
            this.schemaNodeNameLength = schemaNodeNameLength;
            return this;
        }

        public SchemaNodeBuilder schemaNodeName(String schemaNodeName) {
            this.schemaNodeName = schemaNodeName;
            return this;
        }

        public SchemaNodeBuilder dataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public SchemaNodeBuilder minOccurs(Integer minOccurs) {
            this.minOccurs = minOccurs;
            return this;
        }

        public SchemaNodeBuilder maxOccurs(Integer maxOccurs) {
            this.maxOccurs = maxOccurs;
            return this;
        }

        public SchemaNodeBuilder attributeRequired(boolean attributeRequired) {
            this.attributeRequired = attributeRequired;
            return this;
        }

        public SchemaNodeBuilder mixed(boolean mixed) {
            this.mixed = mixed;
            return this;
        }

        public SchemaNodeBuilder attributeCount(Integer attributeCount) {
            this.attributeCount = attributeCount;
            return this;
        }

        public SchemaNodeBuilder attributes(List<Integer> attributes) {
            this.attributes = attributes;
            return this;
        }

        public SchemaNodeBuilder childrenCount(Integer childrenCount) {
            this.childrenCount = childrenCount;
            return this;
        }

        public SchemaNodeBuilder children(List<Integer> children) {
            this.children = children;
            return this;
        }

        @Override
        public SchemaNode build() {
            return new SchemaNode(index, schemaNodeType, schemaNodeNameLength, schemaNodeName, dataType, minOccurs, maxOccurs, attributeRequired, mixed, attributeCount, attributes, childrenCount, children);
        }
    }
}
