package io.technicrow.xdakit.sxc;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class CombinedDataContainerEntry {

    private Integer combinedDataContainerCount;

    private Integer combinedDataContainerLength;

    private List<DataContainerEntry> dataContainersInformation;


    public CombinedDataContainerEntry(Integer combinedDataContainerCount, Integer combinedDataContainerLength, List<DataContainerEntry> dataContainersInformation) {
        this.combinedDataContainerCount = combinedDataContainerCount;
        this.combinedDataContainerLength = combinedDataContainerLength;
        this.dataContainersInformation = dataContainersInformation;
    }

    public Integer getCombinedDataContainerCount() {
        return this.combinedDataContainerCount;
    }

    public Integer getCombinedDataContainerLength() {
        return this.combinedDataContainerLength;
    }

    public List<DataContainerEntry> getDataContainersInformation() {
        return this.dataContainersInformation;
    }

    public void setCombinedDataContainerCount(Integer combinedDataContainerCount) {
        this.combinedDataContainerCount = combinedDataContainerCount;
    }

    public void setCombinedDataContainerLength(Integer combinedDataContainerLength) {
        this.combinedDataContainerLength = combinedDataContainerLength;
    }

    public void setDataContainersInformation(List<DataContainerEntry> dataContainersInformation) {
        this.dataContainersInformation = dataContainersInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CombinedDataContainerEntry that = (CombinedDataContainerEntry) o;

        return new EqualsBuilder()
                .append(combinedDataContainerCount, that.combinedDataContainerCount)
                .append(combinedDataContainerLength, that.combinedDataContainerLength)
                .append(dataContainersInformation, that.dataContainersInformation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(combinedDataContainerCount)
                .append(combinedDataContainerLength)
                .append(dataContainersInformation)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("combinedDataContainerCount", combinedDataContainerCount)
                .append("combinedDataContainerLength", combinedDataContainerLength)
                .append("dataContainersInformation", dataContainersInformation)
                .toString();
    }
}
