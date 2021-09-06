/**
 * Title:	XDAInterface
 * Description:	定义XDA接口。包括以下功能：
 * 1. 新建空XDA文档
 * 2. 打开一个XDA文档
 * 3. 关闭当前XDA文档
 * 4. 保存一个XDA文档
 * 5. 另存为一个新的XDA文件
 * 6. 向包添加一个文件
 * 7. 替换包内文件
 * 8. 移除包内文件
 * 9. 添加指定目录所有子孙文件
 * 10. 移除指定目录下所有内容
 * 11. 提取包内文件
 * 12. 提取包根目录下的所有子孙文件到指定文件夹
 * 13. 提取包内指定路径下的所有子孙文件到指定文件夹
 * 14. 对文件校验和进行检查
 * 15. 获取主版本号
 * 16. 获取次版本号
 *
 *
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.XDAException
import java.io.FileNotFoundException
import java.io.IOException
import java.security.NoSuchAlgorithmException

/**
 * XDA接口定义
 *
 *
 *
 * @version 1.00 11 Nov 2009
 * @author TH.Yang (tianhang.yang@gmail.com)
 */
interface XDAInterface {
    /**
     * 新建空XDA文档。<br></br>
     * 该文档的主版本号和此版本号由实现该接口的类确定。位参数必须是XDA规范规定的数字。
     * <P>
     *
     * @param filePath
     * 新建XDA文档的文件路径
     * @param bitsParam
     * 位参数
     * @throws XDAException
     * @throws FileNotFoundException
    </P> */
    @Throws(FileNotFoundException::class, XDAException::class)
    fun create(filePath: String?, bitsParam: Byte)

    /**
     * 新建空XDA文档。<br></br>
     * 使用默认位参数进行创建，相当于调用了create(0x00)。
     *
     *
     *
     * @param filePath
     * 新建XDA文档的文件路径
     * @throws FileNotFoundException
     * @see .create
     */
    @Throws(FileNotFoundException::class, XDAException::class)
    fun create(filePath: String?)

    /**
     * 打开一个XDA文档
     * <P>
     *
     * @param filePath
     * XDA文档的文件路径
     * @throws XDAException
     * @throws IOException
    </P> */
    @Throws(IOException::class, XDAException::class)
    fun open(filePath: String?)

    /**
     * 关闭当前XDA文档
     *
     *
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun close()

    /**
     * 保存一个XDA文档
     * <P>
     * @param nameTableCompress
     * 如果为true，则对NameTable进行压缩
     * @param itemListCompress
     * 如果为true，则对ItemList进行压缩
     * @throws IOException
     * @throws XDAException
     * @throws NoSuchAlgorithmException
    </P> */
    @Throws(NoSuchAlgorithmException::class, XDAException::class, IOException::class)
    fun save(nameTableCompress: Boolean, itemListCompress: Boolean)

    /**
     * 保存一个XDA文档<br></br>
     * 保存XDA文档,对NameTable和ItemList都进行压缩.<br></br>
     * 相当于调用了save(true, true).
     * <P>
     *
     * @throws XDAException
     * @throws NoSuchAlgorithmException
     * @see .save
    </P> */
    @Throws(NoSuchAlgorithmException::class, XDAException::class, IOException::class)
    fun save()

    /**
     * 另存为一个新的XDA文件。<br></br>
     * 新XDA文件只有一个Entry，为当前XDA文件逻辑上的最新版本。
     *
     *
     *
     * @param newXDAPath
     * 新XDA文件路径
     * @param bitsParam
     * 位参数
     * @param nameTableCompress
     * 如果为true，则对NameTable进行压缩
     * @param itemListCompress
     * 如果为true，则对ItemList进行压缩
     * @throws IOException
     * @throws XDAException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws XDAException
     */
    @Throws(NoSuchAlgorithmException::class, XDAException::class, IOException::class)
    fun saveAs(
        newXDAPath: String?, bitsParam: Byte,
        nameTableCompress: Boolean, itemListCompress: Boolean
    )

    /**
     * 另存为一个新的XDA文件<br></br>
     * 新XDA文件只有一个Entry，为当前XDA的最新的版本。
     * 使用默认的bitsParam，对NameTable和ItemList都进行压缩。
     * 相当于调用了savaAs(newXDAPath, 0x04, true, true)。
     *
     *
     *
     * @param newXDAPath
     * 新XDA文件路径
     * @throws IOException
     * @throws XDAException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws XDAException
     * @see .saveAs
     */
    @Throws(NoSuchAlgorithmException::class, XDAException::class, IOException::class)
    fun saveAs(newXDAPath: String?)

    /**
     * 向包添加一个文件<br></br>
     * 添加的实际目标是文件。<br></br>
     * 在调用save前,如果对targetFilePath文件进行修改。则发生无法预测行为。
     *
     *
     *
     * @param pathInXDA
     * XDA文档内路径;
     * @param targetFilePath
     * 待添加的目标文件路径;
     * @param ecs
     * ECS, 长度必须等于实际可用字节长度.
     * @throws XDAException
     */
    @Throws(XDAException::class)
    fun addFile(
        pathInXDA: String?, targetFilePath: String?,
        ecs: ByteArray?
    )

    /**
     * 向包添加一个文件<br></br>
     * 添加的实际目标是一段内存。<br></br>
     * 在调用save前,如果对targetDate文件进行修改，则发生无法预测行为。
     *
     *
     *
     * @param pathInXDA
     * XDA文档内路径;
     * @param targetDate
     * 待添加的目标内存;
     * @param ecs
     * ECS, 长度必须等于实际可用字节长度。
     * @throws XDAException
     * @throws XDAException
     */
    @Throws(XDAException::class)
    fun addFile(
        pathInXDA: String?, targetDate: ByteArray?,
        ecs: ByteArray?
    )

    /**
     * 替换包内文件<br></br>
     * 替换的实际目标是文件.<br></br>
     * 在调用save前,如果对targetFilePath文件进行修改,则发生无法预测行为
     * <P>
     *
     * @param pathInXDA
     * XDA文档内路径;
     * @param targetFilePath
     * 待替换的目标文件路径;
     * @param ecs
     * ECS, 长度必须等于实际可用字节长度.
     * @throws XDAException
    </P> */
    @Throws(XDAException::class)
    fun replaceFile(
        pathInXDA: String?,
        targetFilePath: String?, ecs: ByteArray?
    )

    /**
     * 替换包内文件<br></br>
     * 替换的实际目标是一段内存.<br></br>
     * 在调用save前,如果对targetFilePath文件进行修改,则发生无法预测行为
     * <P>
     *
     * @param pathInXDA
     * XDA文档内路径;
     * @param targetDate
     * 待替换的目标内存;
     * @param ecs
     * ECS, 长度必须等于实际可用字节长度.
     * @throws XDAException
     * @throws XDAException
    </P> */
    @Throws(XDAException::class)
    fun replaceFile(
        pathInXDA: String?, targetDate: ByteArray?,
        ecs: ByteArray?
    )

    /**
     * 移除包内文件
     * <P>
     * @param pathInXDA
     * XDA文档内路径;
     * @throws XDAException
    </P> */
    @Throws(XDAException::class)
    fun removeFile(pathInXDA: String?)

    /**
     * 添加指定目录所有子孙文件
     * <P>
     * @param pathInXDA
     * 挂接的XDA文档路径,""表示在从根开始;
     * @param dirPath
     * 指定目录;
     * @param ecs
     * ECS, 长度必须等于实际可用字节长度.
     * @throws XDAException
    </P> */
    @Throws(XDAException::class)
    fun addDir(pathInXDA: String?, dirPath: String?, ecs: ByteArray?)

    /**
     * 移除指定目录下所有内容
     * <P>
     *
     * @param pathInXDA
     * 待删除的目录路径;
     * @throws XDAException
    </P> */
    fun removeDir(pathInXDA: String?)

    /**
     * 提取包内文件
     * <P>
     * 提取到文件中.
    </P> *
     *
     *
     * @param pathInXDA
     * XDA文档内路径;
     * @param targetFilePath
     * 提取的文件路径;
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     */
    @Throws(FileNotFoundException::class, IOException::class, XDAException::class)
    fun extractFile(pathInXDA: String?, targetFilePath: String?)

    /**
     * 提取包内文件<br></br>
     * 提取出的文件由一段内存接受。
     *
     *
     *
     * @param pathInXDA
     * XDA文档内路径
     * @return 装载提取文件内容的内存
     * @throws XDAException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     */
    @Throws(IOException::class, XDAException::class)
    fun extractFile(pathInXDA: String?): ByteArray?

    /**
     * 提取包根目录下的所有子孙文件到指定文件夹
     * 相当于调用了extractDir("", dir);
     * <P>
     *
     * @param dir
     * 指定文件夹
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     * @see .extractDir
    </P> */
    @Throws(IOException::class, XDAException::class)
    fun extractDir(dir: String?)

    /**
     * 提取包内指定路径下的所有子孙文件到指定文件夹
     *
     *
     *
     * @param pathInXDA
     * 包内指定路径,""相当于从跟开始
     * @param dir
     * 指定文件夹
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     */
    @Throws(IOException::class, XDAException::class)
    fun extractDir(pathInXDA: String?, dir: String?)

    /**
     * 对文件校验和进行检查
     *
     *
     *
     * @return 校验和是否和符合包文件
     */
    fun validateFile(): Boolean

    /**
     * 获取主版本号
     *
     *
     *
     * @return 主版本号
     * @throws XDAException
     */
    val majorVersion: Int

    /**
     * 获取次版本号
     *
     *
     *
     * @return 次版本号
     * @throws XDAException
     */
    val minorVersion: Int

    companion object {
        /**
         * NameTable压缩掩码
         */
        const val COMPRESSNAMETABLE_MASK = 0x01.toByte()

        /**
         * ItemList压缩掩码
         */
        const val COMPRESSITEMLIST_MASK = 0x02.toByte()
    }
}