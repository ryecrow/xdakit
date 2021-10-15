package io.technicrow.xdakit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * Java presentation of an XDA package
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XDAPackage {

    @Builder.Default
    private XDAHeader header = new XDAHeader();

    @Builder.Default
    private List<XDAEntry> entries = new LinkedList<>();
}
