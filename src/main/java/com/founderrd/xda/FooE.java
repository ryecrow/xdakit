/**
 * Title:	XDAException
 * Description:	定义com.foundered.xda可能抛出的异常
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

public class FooE extends Exception {
    static final String INVALID_PACK_PATH = "Invalid package path!";
    static final String INVALID_FILE_PATH = "Invalid file path!";
    static final String INEXISTENT_ITEM = "Inexistent item!";
    static final String INVALID_OPERATION = "Invalid operation!";
    static final String INVALID_OPERATION_TYPE = "Invalid operation type!";
    static final String INVALID_NEXT_FIELD_OF_LAST_ENTRY = "Invalid next field of last entry!";
    static final String INVALID_ITEM_CONTENT = "Invalid Item content!";
    static final String XDACOMMONFUNCTION_ERROR = "XDACommonFunction error!";
    static final String NEVER_PARSE_ENTRY = "Never parse entry!";
    static final String NEVER_PARSE_HEADER = "Never parse header!";
    static final String INVALID_NAMETABLE = "Invalid NameTable!";
    static final String INVALID_BITSPARAM = "Invalid BitsParam!";
    static final String INVALID_ITEMLIST = "Invalid ItemList!";
    static final String INVALID_ENTRY_CLASSTYPE = "Invalid Entry's ClassType!";
    static final String INVALID_OPERATION_SEQUENCE = "Invalid Operator Sequence!";
    static final String INVALID_NAMEVALUE = "Invalid Namevalue!";
    static final String CANNOT_EXTRACT_STREAM = "Cannot extract stream!";
    static final String INVALID_RIGHT_INFO = "Invalid right info!";
    static final String INVALID_ENTRYNAMETABLETYPE = "Invalid EntryNameTableType!";
    static final String NO_XDA_FILE = "No XDA file!";
    private static final long serialVersionUID = 1L;
    private final String info;

    public FooE(String theInfo) {
        info = theInfo;
    }

    public String getInfo() {
        return info;
    }
}
