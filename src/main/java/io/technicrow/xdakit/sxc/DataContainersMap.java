package io.technicrow.xdakit.sxc;

import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class DataContainersMap {

    private boolean encoded;

    private boolean compressed;

    private Integer uncombinedDataContainersCount;

    private Integer combinedDataContainersCount;

    private List<DataContainerEntry> uncombinedDataContainersInformation;

    private List<CombinedDataContainerEntry> combinedDataContainersInformation;

    DataContainersMap(boolean encoded, boolean compressed, Integer uncombinedDataContainersCount, Integer combinedDataContainersCount, List<DataContainerEntry> uncombinedDataContainersInformation, List<CombinedDataContainerEntry> combinedDataContainersInformation) {
        this.encoded = encoded;
        this.compressed = compressed;
        this.uncombinedDataContainersCount = uncombinedDataContainersCount;
        this.combinedDataContainersCount = combinedDataContainersCount;
        this.uncombinedDataContainersInformation = uncombinedDataContainersInformation;
        this.combinedDataContainersInformation = combinedDataContainersInformation;
    }

    public static DataContainersMapBuilder builder() {
        return new DataContainersMapBuilder();
    }

    public boolean isEncoded() {
        return this.encoded;
    }

    public boolean isCompressed() {
        return this.compressed;
    }

    public Integer getUncombinedDataContainersCount() {
        return this.uncombinedDataContainersCount;
    }

    public Integer getCombinedDataContainersCount() {
        return this.combinedDataContainersCount;
    }

    public List<DataContainerEntry> getUncombinedDataContainersInformation() {
        return this.uncombinedDataContainersInformation;
    }

    public List<CombinedDataContainerEntry> getCombinedDataContainersInformation() {
        return this.combinedDataContainersInformation;
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public void setUncombinedDataContainersCount(Integer uncombinedDataContainersCount) {
        this.uncombinedDataContainersCount = uncombinedDataContainersCount;
    }

    public void setCombinedDataContainersCount(Integer combinedDataContainersCount) {
        this.combinedDataContainersCount = combinedDataContainersCount;
    }

    public void setUncombinedDataContainersInformation(List<DataContainerEntry> uncombinedDataContainersInformation) {
        this.uncombinedDataContainersInformation = uncombinedDataContainersInformation;
    }

    public void setCombinedDataContainersInformation(List<CombinedDataContainerEntry> combinedDataContainersInformation) {
        this.combinedDataContainersInformation = combinedDataContainersInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DataContainersMap that = (DataContainersMap) o;

        return new EqualsBuilder().append(encoded, that.encoded).append(compressed, that.compressed).append(uncombinedDataContainersCount, that.uncombinedDataContainersCount).append(combinedDataContainersCount, that.combinedDataContainersCount).append(uncombinedDataContainersInformation, that.uncombinedDataContainersInformation).append(combinedDataContainersInformation, that.combinedDataContainersInformation).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(encoded).append(compressed).append(uncombinedDataContainersCount).append(combinedDataContainersCount).append(uncombinedDataContainersInformation).append(combinedDataContainersInformation).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("encoded", encoded)
                .append("compressed", compressed)
                .append("uncombinedDataContainersCount", uncombinedDataContainersCount)
                .append("combinedDataContainersCount", combinedDataContainersCount)
                .append("uncombinedDataContainersInformation", uncombinedDataContainersInformation)
                .append("combinedDataContainersInformation", combinedDataContainersInformation)
                .toString();
    }

    public static class DataContainersMapBuilder implements Builder<DataContainersMap> {
        private boolean encoded;
        private boolean compressed;
        private Integer uncombinedDataContainersCount;
        private Integer combinedDataContainersCount;
        private List<DataContainerEntry> uncombinedDataContainersInformation;
        private List<CombinedDataContainerEntry> combinedDataContainersInformation;

        DataContainersMapBuilder() {
        }

        public DataContainersMapBuilder encoded(boolean encoded) {
            this.encoded = encoded;
            return this;
        }

        public DataContainersMapBuilder compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        public DataContainersMapBuilder uncombinedDataContainersCount(Integer uncombinedDataContainersCount) {
            this.uncombinedDataContainersCount = uncombinedDataContainersCount;
            return this;
        }

        public DataContainersMapBuilder combinedDataContainersCount(Integer combinedDataContainersCount) {
            this.combinedDataContainersCount = combinedDataContainersCount;
            return this;
        }

        public DataContainersMapBuilder uncombinedDataContainersInformation(List<DataContainerEntry> uncombinedDataContainersInformation) {
            this.uncombinedDataContainersInformation = uncombinedDataContainersInformation;
            return this;
        }

        public DataContainersMapBuilder combinedDataContainersInformation(List<CombinedDataContainerEntry> combinedDataContainersInformation) {
            this.combinedDataContainersInformation = combinedDataContainersInformation;
            return this;
        }

        @Override
        public DataContainersMap build() {
            return new DataContainersMap(encoded, compressed, uncombinedDataContainersCount, combinedDataContainersCount, uncombinedDataContainersInformation, combinedDataContainersInformation);
        }
    }
}
