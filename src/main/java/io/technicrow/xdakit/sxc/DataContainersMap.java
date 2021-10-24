package io.technicrow.xdakit.sxc;

import lombok.Data;

import java.util.List;

@Data
public class DataContainersMap {

    private boolean encoded;

    private boolean compressed;

    private Integer uncombinedDataContainersCount;

    private Integer combinedDataContainersCount;

    private List<DataContainerEntry> uncombinedDataContainersInformation;

    private List<CombinedDataContainerEntry> combinedDataContainersInformation;
}
