package io.technicrow.xdakit.sxc;

import lombok.Data;

import java.util.List;

/**
 * Binary Schema Graph
 */
@Data
public class SchemaGraph {

    private byte[] fileHeader;

    private byte[] checkInformation;

    private Integer schemaNodeCount;

    private List<SchemaNode> schemaNodes;

    private List<Integer> elementTable;
 }
