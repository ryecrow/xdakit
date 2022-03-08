package io.technicrow.xdakit.sxc;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class DataContainerEntry {

    private Integer nodeId;

    private Integer dataContainerLength;

    public DataContainerEntry(Integer nodeId, Integer dataContainerLength) {
        this.nodeId = nodeId;
        this.dataContainerLength = dataContainerLength;
    }

    public Integer getNodeId() {
        return this.nodeId;
    }

    public Integer getDataContainerLength() {
        return this.dataContainerLength;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public void setDataContainerLength(Integer dataContainerLength) {
        this.dataContainerLength = dataContainerLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DataContainerEntry that = (DataContainerEntry) o;

        return new EqualsBuilder().append(nodeId, that.nodeId).append(dataContainerLength, that.dataContainerLength).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(nodeId).append(dataContainerLength).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nodeId", nodeId)
                .append("dataContainerLength", dataContainerLength)
                .toString();
    }
}
