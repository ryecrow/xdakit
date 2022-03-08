package io.technicrow.xdakit.model;

import io.technicrow.xdakit.constant.Operator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigInteger;

/**
 * Item in the ItemList
 */
public class Item {

    private Operator operator;

    private Byte reserved;

    private Long itemOffset;

    private BigInteger nameValue;

    public Item(Operator operator, Byte reserved, Long itemOffset, BigInteger nameValue) {
        this.operator = operator;
        this.reserved = reserved;
        this.itemOffset = itemOffset;
        this.nameValue = nameValue;
    }

    public Item() {
    }

    public Operator getOperator() {
        return this.operator;
    }

    public Byte getReserved() {
        return this.reserved;
    }

    public Long getItemOffset() {
        return this.itemOffset;
    }

    public BigInteger getNameValue() {
        return this.nameValue;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public void setReserved(Byte reserved) {
        this.reserved = reserved;
    }

    public void setItemOffset(Long itemOffset) {
        this.itemOffset = itemOffset;
    }

    public void setNameValue(BigInteger nameValue) {
        this.nameValue = nameValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return new EqualsBuilder().append(operator, item.operator).append(reserved, item.reserved).append(itemOffset, item.itemOffset).append(nameValue, item.nameValue).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(operator).append(reserved).append(itemOffset).append(nameValue).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("operator", operator)
                .append("reserved", reserved)
                .append("itemOffset", itemOffset)
                .append("nameValue", nameValue)
                .toString();
    }
}
