package io.technicrow.xdakit.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * XDA entry NameTable
 */
public class NameTable {

    private Integer nameCount;

    private List<NameMapping> nameMappings;

    public NameTable(Integer nameCount, List<NameMapping> nameMappings) {
        this.nameCount = nameCount;
        this.nameMappings = nameMappings;
    }

    public Integer getNameCount() {
        return this.nameCount;
    }

    public List<NameMapping> getNameMappings() {
        return this.nameMappings;
    }

    public void setNameCount(Integer nameCount) {
        this.nameCount = nameCount;
    }

    public void setNameMappings(List<NameMapping> nameMappings) {
        this.nameMappings = nameMappings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NameTable nameTable = (NameTable) o;

        return new EqualsBuilder().append(nameCount, nameTable.nameCount).append(nameMappings, nameTable.nameMappings).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(nameCount).append(nameMappings).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nameCount", nameCount)
                .append("nameMappings", nameMappings)
                .toString();
    }
}
