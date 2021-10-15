package io.technicrow.xdakit.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * XDA entry NameTable
 */
@Data
@AllArgsConstructor
public class NameTable {

    private Integer nameCount;

    private List<NameMapping> nameMappings;
}
