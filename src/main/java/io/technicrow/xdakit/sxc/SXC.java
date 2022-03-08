package io.technicrow.xdakit.sxc;

import io.technicrow.xdakit.constant.SXCContentAppearance;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Set;

/**
 * SXC Data Structure
 */
public class SXC {

    private Integer versionNo;

    private Byte compressMethod;

    private Set<SXCContentAppearance> contentAppearanceInformation;

    private Integer namespaceInformationOffset;

    private Integer namespaceInformationLength;

    private Integer dictionaryOffset;

    private Integer dictionaryLength;

    private Integer structureDataOffset;

    private Integer structureDataLength;

    private Integer dataContainersOffset;

    private Integer dataContainersLength;

    private Integer dataContainersMapOffset;

    private Integer dataContainersMapLength;

    /**
     * @deprecated not implemented yet
     */
    @Deprecated
    private byte[] namespaceInformation;

    /**
     * @deprecated not implemented yet
     */
    @Deprecated
    private byte[] dictionary;

    private StructureData structureData;

    private List<String> dataContainers;

    private DataContainersMap dataContainersMap;

    SXC(Integer versionNo, Byte compressMethod, Set<SXCContentAppearance> contentAppearanceInformation,
        Integer namespaceInformationOffset, Integer namespaceInformationLength, Integer dictionaryOffset, Integer dictionaryLength,
        Integer structureDataOffset, Integer structureDataLength, Integer dataContainersOffset, Integer dataContainersLength,
        Integer dataContainersMapOffset, Integer dataContainersMapLength,
        StructureData structureData, List<String> dataContainers, DataContainersMap dataContainersMap) {
        this.versionNo = versionNo;
        this.compressMethod = compressMethod;
        this.contentAppearanceInformation = contentAppearanceInformation;
        this.namespaceInformationOffset = namespaceInformationOffset;
        this.namespaceInformationLength = namespaceInformationLength;
        this.dictionaryOffset = dictionaryOffset;
        this.dictionaryLength = dictionaryLength;
        this.structureDataOffset = structureDataOffset;
        this.structureDataLength = structureDataLength;
        this.dataContainersOffset = dataContainersOffset;
        this.dataContainersLength = dataContainersLength;
        this.dataContainersMapOffset = dataContainersMapOffset;
        this.dataContainersMapLength = dataContainersMapLength;
        this.structureData = structureData;
        this.dataContainers = dataContainers;
        this.dataContainersMap = dataContainersMap;
    }

    public static SXCBuilder builder() {
        return new SXCBuilder();
    }

    public Integer getVersionNo() {
        return this.versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public Byte getCompressMethod() {
        return this.compressMethod;
    }

    public void setCompressMethod(Byte compressMethod) {
        this.compressMethod = compressMethod;
    }

    public Set<SXCContentAppearance> getContentAppearanceInformation() {
        return this.contentAppearanceInformation;
    }

    public void setContentAppearanceInformation(Set<SXCContentAppearance> contentAppearanceInformation) {
        this.contentAppearanceInformation = contentAppearanceInformation;
    }

    public Integer getNamespaceInformationOffset() {
        return this.namespaceInformationOffset;
    }

    public void setNamespaceInformationOffset(Integer namespaceInformationOffset) {
        this.namespaceInformationOffset = namespaceInformationOffset;
    }

    public Integer getNamespaceInformationLength() {
        return this.namespaceInformationLength;
    }

    public void setNamespaceInformationLength(Integer namespaceInformationLength) {
        this.namespaceInformationLength = namespaceInformationLength;
    }

    public Integer getDictionaryOffset() {
        return this.dictionaryOffset;
    }

    public void setDictionaryOffset(Integer dictionaryOffset) {
        this.dictionaryOffset = dictionaryOffset;
    }

    public Integer getDictionaryLength() {
        return this.dictionaryLength;
    }

    public void setDictionaryLength(Integer dictionaryLength) {
        this.dictionaryLength = dictionaryLength;
    }

    public Integer getStructureDataOffset() {
        return this.structureDataOffset;
    }

    public void setStructureDataOffset(Integer structureDataOffset) {
        this.structureDataOffset = structureDataOffset;
    }

    public Integer getStructureDataLength() {
        return this.structureDataLength;
    }

    public void setStructureDataLength(Integer structureDataLength) {
        this.structureDataLength = structureDataLength;
    }

    public Integer getDataContainersOffset() {
        return this.dataContainersOffset;
    }

    public void setDataContainersOffset(Integer dataContainersOffset) {
        this.dataContainersOffset = dataContainersOffset;
    }

    public Integer getDataContainersLength() {
        return this.dataContainersLength;
    }

    public void setDataContainersLength(Integer dataContainersLength) {
        this.dataContainersLength = dataContainersLength;
    }

    public Integer getDataContainersMapOffset() {
        return this.dataContainersMapOffset;
    }

    public void setDataContainersMapOffset(Integer dataContainersMapOffset) {
        this.dataContainersMapOffset = dataContainersMapOffset;
    }

    public Integer getDataContainersMapLength() {
        return this.dataContainersMapLength;
    }

    public void setDataContainersMapLength(Integer dataContainersMapLength) {
        this.dataContainersMapLength = dataContainersMapLength;
    }

    @Deprecated
    public byte[] getNamespaceInformation() {
        return namespaceInformation;
    }

    @Deprecated
    public void setNamespaceInformation(byte[] namespaceInformation) {
    }

    @Deprecated
    public byte[] getDictionary() {
        return dictionary;
    }

    @Deprecated
    public void setDictionary(byte[] dictionary) {
    }

    public StructureData getStructureData() {
        return this.structureData;
    }

    public void setStructureData(StructureData structureData) {
        this.structureData = structureData;
    }

    public List<String> getDataContainers() {
        return this.dataContainers;
    }

    public void setDataContainers(List<String> dataContainers) {
        this.dataContainers = dataContainers;
    }

    public DataContainersMap getDataContainersMap() {
        return this.dataContainersMap;
    }

    public void setDataContainersMap(DataContainersMap dataContainersMap) {
        this.dataContainersMap = dataContainersMap;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SXC sxc = (SXC) o;

        return new EqualsBuilder().append(versionNo, sxc.versionNo).append(compressMethod, sxc.compressMethod).append(contentAppearanceInformation, sxc.contentAppearanceInformation).append(namespaceInformationOffset, sxc.namespaceInformationOffset).append(namespaceInformationLength, sxc.namespaceInformationLength).append(dictionaryOffset, sxc.dictionaryOffset).append(dictionaryLength, sxc.dictionaryLength).append(structureDataOffset, sxc.structureDataOffset).append(structureDataLength, sxc.structureDataLength).append(dataContainersOffset, sxc.dataContainersOffset).append(dataContainersLength, sxc.dataContainersLength).append(dataContainersMapOffset, sxc.dataContainersMapOffset).append(dataContainersMapLength, sxc.dataContainersMapLength).append(namespaceInformation, sxc.namespaceInformation).append(dictionary, sxc.dictionary).append(structureData, sxc.structureData).append(dataContainers, sxc.dataContainers).append(dataContainersMap, sxc.dataContainersMap).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(versionNo).append(compressMethod).append(contentAppearanceInformation).append(namespaceInformationOffset).append(namespaceInformationLength).append(dictionaryOffset).append(dictionaryLength).append(structureDataOffset).append(structureDataLength).append(dataContainersOffset).append(dataContainersLength).append(dataContainersMapOffset).append(dataContainersMapLength).append(namespaceInformation).append(dictionary).append(structureData).append(dataContainers).append(dataContainersMap).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("versionNo", versionNo)
                .append("compressMethod", compressMethod)
                .append("contentAppearanceInformation", contentAppearanceInformation)
                .append("namespaceInformationOffset", namespaceInformationOffset)
                .append("namespaceInformationLength", namespaceInformationLength)
                .append("dictionaryOffset", dictionaryOffset)
                .append("dictionaryLength", dictionaryLength)
                .append("structureDataOffset", structureDataOffset)
                .append("structureDataLength", structureDataLength)
                .append("dataContainersOffset", dataContainersOffset)
                .append("dataContainersLength", dataContainersLength)
                .append("dataContainersMapOffset", dataContainersMapOffset)
                .append("dataContainersMapLength", dataContainersMapLength)
                .append("namespaceInformation", namespaceInformation)
                .append("dictionary", dictionary)
                .append("structureData", structureData)
                .append("dataContainers", dataContainers)
                .append("dataContainersMap", dataContainersMap)
                .toString();
    }

    public static class SXCBuilder {
        private Integer versionNo;
        private Byte compressMethod;
        private Set<SXCContentAppearance> contentAppearanceInformation;
        private Integer namespaceInformationOffset = 0;
        private Integer namespaceInformationLength = 0;
        private Integer dictionaryOffset = 0;
        private Integer dictionaryLength = 0;
        private Integer structureDataOffset = 0;
        private Integer structureDataLength = 0;
        private Integer dataContainersOffset = 0;
        private Integer dataContainersLength = 0;
        private Integer dataContainersMapOffset = 0;
        private Integer dataContainersMapLength = 0;
        private StructureData structureData;
        private List<String> dataContainers;
        private DataContainersMap dataContainersMap;

        SXCBuilder() {
        }

        public SXCBuilder versionNo(Integer versionNo) {
            this.versionNo = versionNo;
            return this;
        }

        public SXCBuilder compressMethod(Byte compressMethod) {
            this.compressMethod = compressMethod;
            return this;
        }

        public SXCBuilder contentAppearanceInformation(Set<SXCContentAppearance> contentAppearanceInformation) {
            this.contentAppearanceInformation = contentAppearanceInformation;
            return this;
        }

        public SXCBuilder namespaceInformationOffset(Integer namespaceInformationOffset) {
            this.namespaceInformationOffset = namespaceInformationOffset;
            return this;
        }

        public SXCBuilder namespaceInformationLength(Integer namespaceInformationLength) {
            this.namespaceInformationLength = namespaceInformationLength;
            return this;
        }

        public SXCBuilder dictionaryOffset(Integer dictionaryOffset) {
            this.dictionaryOffset = dictionaryOffset;
            return this;
        }

        public SXCBuilder dictionaryLength(Integer dictionaryLength) {
            this.dictionaryLength = dictionaryLength;
            return this;
        }

        public SXCBuilder structureDataOffset(Integer structureDataOffset) {
            this.structureDataOffset = structureDataOffset;
            return this;
        }

        public SXCBuilder structureDataLength(Integer structureDataLength) {
            this.structureDataLength = structureDataLength;
            return this;
        }

        public SXCBuilder dataContainersOffset(Integer dataContainersOffset) {
            this.dataContainersOffset = dataContainersOffset;
            return this;
        }

        public SXCBuilder dataContainersLength(Integer dataContainersLength) {
            this.dataContainersLength = dataContainersLength;
            return this;
        }

        public SXCBuilder dataContainersMapOffset(Integer dataContainersMapOffset) {
            this.dataContainersMapOffset = dataContainersMapOffset;
            return this;
        }

        public SXCBuilder dataContainersMapLength(Integer dataContainersMapLength) {
            this.dataContainersMapLength = dataContainersMapLength;
            return this;
        }

        public SXCBuilder structureData(StructureData structureData) {
            this.structureData = structureData;
            return this;
        }

        public SXCBuilder dataContainers(List<String> dataContainers) {
            this.dataContainers = dataContainers;
            return this;
        }

        public SXCBuilder dataContainersMap(DataContainersMap dataContainersMap) {
            this.dataContainersMap = dataContainersMap;
            return this;
        }

        public SXC build() {
            return new SXC(versionNo, compressMethod, contentAppearanceInformation,
                    namespaceInformationOffset, namespaceInformationLength, dictionaryOffset, dictionaryLength,
                    structureDataOffset, structureDataLength, dataContainersOffset, dataContainersLength,
                    dataContainersMapOffset, dataContainersMapLength,
                    structureData, dataContainers, dataContainersMap);
        }
    }
}
