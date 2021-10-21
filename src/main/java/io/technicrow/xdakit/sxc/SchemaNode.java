package io.technicrow.xdakit.sxc;

import io.technicrow.xdakit.constant.DataType;
import io.technicrow.xdakit.constant.SchemaType;
import lombok.Data;

import java.util.List;

/**
 * Schema Node Information
 */
@Data
public class SchemaNode {

    private SchemaType schemaNodeType;

    private Integer schemaNodeNameLength;

    private String schemaNodeName;

    private DataType dataType;

    private Integer minOccurs;

    private Integer maxOccurs;

    private boolean attributeRequired;

    private boolean mixed;

    private Integer attributeCount;

    private List<Integer> attributes;

    private Integer childrenCount;

    private List<Integer> children;
}