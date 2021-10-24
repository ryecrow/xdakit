package io.technicrow.xdakit.sxc;

import io.technicrow.xdakit.constant.SXCContentAppearance;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * SXC Data Structure
 */
@Data
@Builder
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

    private Object DataContainers;

    private DataContainersMap dataContainersMap;
}
