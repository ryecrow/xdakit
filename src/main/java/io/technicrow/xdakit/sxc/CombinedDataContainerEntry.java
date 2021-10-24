package io.technicrow.xdakit.sxc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CombinedDataContainerEntry {

    private Integer combinedDataContainerCount;

    private Integer combinedDataContainerLength;

    private List<DataContainerEntry> dataContainersInformation;
}
