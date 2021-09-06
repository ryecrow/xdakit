/**
 * Title:	XDAException
 * Description:	定义com.foundered.xda可能抛出的异常
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

class XDAException(val info: String) : Exception() {

    companion object {
        const val INVALID_PACK_PATH = "Invalid package path!"
        const val INVALID_FILE_PATH = "Invalid file path!"
        const val INEXISTENT_ITEM = "Inexistent item!"
        const val INVALID_OPERATION = "Invalid operation!"
        const val INVALID_OPERATION_TYPE = "Invalid operation type!"
        const val INVALID_NEXT_FIELD_OF_LAST_ENTRY = "Invalid next field of last entry!"
        const val INVALID_ITEM_CONTENT = "Invalid Item content!"
        const val XDACOMMONFUNCTION_ERROR = "XDACommonFunction error!"
        const val NEVER_PARSE_ENTRY = "Never parse entry!"
        const val NEVER_PARSE_HEADER = "Never parse header!"
        const val INVALID_NAMETABLE = "Invalid NameTable!"
        const val INVALID_BITSPARAM = "Invalid BitsParam!"
        const val INVALID_ITEMLIST = "Invalid ItemList!"
        const val INVALID_ENTRY_CLASSTYPE = "Invalid Entry's ClassType!"
        const val INVALID_OPERATION_SEQUENCE = "Invalid Operator Sequence!"
        const val INVALID_NAMEVALUE = "Invalid Namevalue!"
        const val CANNOT_EXTRACT_STREAM = "Cannot extract stream!"
        const val INVALID_RIGHT_INFO = "Invalid right info!"
        const val INVALID_ENTRYNAMETABLETYPE = "Invalid EntryNameTableType!"
        const val NO_XDA_FILE = "No XDA file!"
        private const val serialVersionUID = 1L
    }
}