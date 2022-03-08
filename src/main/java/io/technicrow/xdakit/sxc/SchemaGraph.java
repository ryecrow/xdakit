package io.technicrow.xdakit.sxc;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Binary Schema Graph
 */
public class SchemaGraph {

    private byte[] fileHeader;

    private byte[] checkInformation;

    private Integer schemaNodeCount;

    private List<SchemaNode> schemaNodes;

    private List<Integer> elementTable;

    public SchemaGraph(byte[] fileHeader, byte[] checkInformation, Integer schemaNodeCount, List<SchemaNode> schemaNodes, List<Integer> elementTable) {
        this.fileHeader = fileHeader;
        this.checkInformation = checkInformation;
        this.schemaNodeCount = schemaNodeCount;
        this.schemaNodes = schemaNodes;
        this.elementTable = elementTable;
    }

    public byte[] getFileHeader() {
        return this.fileHeader;
    }

    public byte[] getCheckInformation() {
        return this.checkInformation;
    }

    public Integer getSchemaNodeCount() {
        return this.schemaNodeCount;
    }

    public List<SchemaNode> getSchemaNodes() {
        return this.schemaNodes;
    }

    public List<Integer> getElementTable() {
        return this.elementTable;
    }

    public void setFileHeader(byte[] fileHeader) {
        this.fileHeader = fileHeader;
    }

    public void setCheckInformation(byte[] checkInformation) {
        this.checkInformation = checkInformation;
    }

    public void setSchemaNodeCount(Integer schemaNodeCount) {
        this.schemaNodeCount = schemaNodeCount;
    }

    public void setSchemaNodes(List<SchemaNode> schemaNodes) {
        this.schemaNodes = schemaNodes;
    }

    public void setElementTable(List<Integer> elementTable) {
        this.elementTable = elementTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SchemaGraph that = (SchemaGraph) o;

        return new EqualsBuilder().append(fileHeader, that.fileHeader).append(checkInformation, that.checkInformation).append(schemaNodeCount, that.schemaNodeCount).append(schemaNodes, that.schemaNodes).append(elementTable, that.elementTable).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(fileHeader).append(checkInformation).append(schemaNodeCount).append(schemaNodes).append(elementTable).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("fileHeader", fileHeader)
                .append("checkInformation", checkInformation)
                .append("schemaNodeCount", schemaNodeCount)
                .append("schemaNodes", schemaNodes)
                .append("elementTable", elementTable)
                .toString();
    }
}
