package io.technicrow.xdakit.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigInteger;

/**
 * NameMapping entity in the {@link NameTable}
 */
public class NameMapping {

    private BigInteger nameValue;

    private String path;

    public NameMapping(BigInteger nameValue, String path) {
        this.nameValue = nameValue;
        this.path = path;
    }

    public BigInteger getNameValue() {
        return this.nameValue;
    }

    public String getPath() {
        return this.path;
    }

    public void setNameValue(BigInteger nameValue) {
        this.nameValue = nameValue;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NameMapping that = (NameMapping) o;

        return new EqualsBuilder().append(nameValue, that.nameValue).append(path, that.path).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(nameValue).append(path).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nameValue", nameValue)
                .append("path", path)
                .toString();
    }
}
