package com.founderrd.xda.bean;

import java.math.BigInteger;

public class ItemData {

    private Byte operator;
    private Long itemOffset;
    private BigInteger nameValueInItemList;

    public ItemData(Byte operator, Long itemOffset, BigInteger nameValueInItemList) {
        this.operator = operator;
        this.itemOffset = itemOffset;
        this.nameValueInItemList = nameValueInItemList;
    }

    public Byte getOperator() {
        return operator;
    }

    public void setOperator(Byte operator) {
        this.operator = operator;
    }

    public Long getItemOffset() {
        return itemOffset;
    }

    public void setItemOffset(Long itemOffset) {
        this.itemOffset = itemOffset;
    }

    public BigInteger getNameValueInItemList() {
        return nameValueInItemList;
    }

    public void setNameValueInItemList(BigInteger nameValueInItemList) {
        this.nameValueInItemList = nameValueInItemList;
    }
}
