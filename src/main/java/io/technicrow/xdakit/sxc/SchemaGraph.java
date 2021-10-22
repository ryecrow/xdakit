package io.technicrow.xdakit.sxc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Binary Schema Graph
 */
@Data
@AllArgsConstructor
public class SchemaGraph {

    private byte[] fileHeader;

    private byte[] checkInformation;

    private Integer schemaNodeCount;

    private List<SchemaNode> schemaNodes;

    private List<Integer> elementTable;
 }
