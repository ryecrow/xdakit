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

    @Builder.Default
    private Integer namespaceInformationOffset = 0;

    @Builder.Default
    private Integer namespaceInformationLength = 0;

    @Builder.Default
    private Integer dictionaryOffset = 0;

    @Builder.Default
    private Integer dictionaryLength = 0;

    @Builder.Default
    private Integer structureDataOffset = 0;

    @Builder.Default
    private Integer structureDataLength = 0;

    @Builder.Default
    private Integer dataContainersOffset = 0;

    @Builder.Default
    private Integer dataContainersLength = 0;

    @Builder.Default
    private Integer dataContainersMapOffset = 0;

    @Builder.Default
    private Integer dataContainersMapLength = 0;

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

    private Object dataContainers;

    private DataContainersMap dataContainersMap;
}
