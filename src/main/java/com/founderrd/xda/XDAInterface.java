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
 * <p>
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * XDA接口定义
 * <p>
 *
 * @author TH.Yang (tianhang.yang@gmail.com)
 * @version 1.00 11 Nov 2009
 */
public interface XDAInterface {

    /**
     * NameTable压缩掩码
     */
    byte NAME_TABLE_MASK = (byte) 0x01;

    /**
     * ItemList压缩掩码
     */
    byte COMPRESSITEMLIST_MASK = (byte) 0x02;

    /**
     * 新建空XDA文档。<br>
     * 该文档的主版本号和此版本号由实现该接口的类确定。位参数必须是XDA规范规定的数字。
     * <p>
     *
     * @param filePath  新建XDA文档的文件路径
     * @param bitsParam 位参数
     * @throws FooE
     * @throws FileNotFoundException
     */
    void create(final String filePath, byte bitsParam)
            throws FileNotFoundException, FooE;

    /**
     * 新建空XDA文档。<br>
     * 使用默认位参数进行创建，相当于调用了create(0x00)。
     * <p>
     *
     * @param filePath 新建XDA文档的文件路径
     * @throws FileNotFoundException
     * @see #create(String, byte)
     */
    void create(final String filePath) throws FileNotFoundException,
            FooE;

    /**
     * 打开一个XDA文档
     * <p>
     *
     * @param filePath XDA文档的文件路径
     * @throws FooE
     * @throws IOException
     */
    void open(final String filePath) throws IOException, FooE;

    /**
     * 关闭当前XDA文档
     * <p>
     *
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * 保存一个XDA文档
     * <p>
     *
     * @param nameTableCompress 如果为true，则对NameTable进行压缩
     * @param itemListCompress  如果为true，则对ItemList进行压缩
     * @throws IOException
     * @throws FooE
     * @throws NoSuchAlgorithmException
     */
    void save(boolean nameTableCompress, boolean itemListCompress)
            throws NoSuchAlgorithmException, FooE, IOException;

    /**
     * 保存一个XDA文档<br>
     * 保存XDA文档,对NameTable和ItemList都进行压缩.<br>
     * 相当于调用了save(true, true).
     * <p>
     *
     * @throws FooE
     * @throws NoSuchAlgorithmException
     * @see #save(boolean, boolean)
     */
    void save() throws NoSuchAlgorithmException, FooE,
            IOException;

    /**
     * 另存为一个新的XDA文件。<br>
     * 新XDA文件只有一个Entry，为当前XDA文件逻辑上的最新版本。
     * <p>
     *
     * @param newXDAPath        新XDA文件路径
     * @param bitsParam         位参数
     * @param nameTableCompress 如果为true，则对NameTable进行压缩
     * @param itemListCompress  如果为true，则对ItemList进行压缩
     * @throws IOException
     * @throws FooE
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FooE
     */
    void saveAs(String newXDAPath, byte bitsParam,
                boolean nameTableCompress, boolean itemListCompress)
            throws NoSuchAlgorithmException, FooE, IOException;

    /**
     * 另存为一个新的XDA文件<br>
     * 新XDA文件只有一个Entry，为当前XDA的最新的版本。
     * 使用默认的bitsParam，对NameTable和ItemList都进行压缩。
     * 相当于调用了savaAs(newXDAPath, 0x04, true, true)。
     * <p>
     *
     * @param newXDAPath 新XDA文件路径
     * @throws IOException
     * @throws FooE
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FooE
     * @see #saveAs(String, byte, boolean, boolean)
     */
    void saveAs(String newXDAPath) throws NoSuchAlgorithmException,
            FooE, IOException;

    /**
     * 向包添加一个文件<br>
     * 添加的实际目标是文件。<br>
     * 在调用save前,如果对targetFilePath文件进行修改。则发生无法预测行为。
     * <p>
     *
     * @param pathInXDA      XDA文档内路径;
     * @param targetFilePath 待添加的目标文件路径;
     * @param ecs            ECS, 长度必须等于实际可用字节长度.
     * @throws FooE
     */
    void addFile(final String pathInXDA, final String targetFilePath,
                 byte[] ecs) throws FooE;

    /**
     * 向包添加一个文件<br>
     * 添加的实际目标是一段内存。<br>
     * 在调用save前,如果对targetDate文件进行修改，则发生无法预测行为。
     * <p>
     *
     * @param pathInXDA  XDA文档内路径;
     * @param targetDate 待添加的目标内存;
     * @param ecs        ECS, 长度必须等于实际可用字节长度。
     * @throws FooE
     * @throws FooE
     */
    void addFile(final String pathInXDA, final byte[] targetDate,
                 byte[] ecs) throws FooE;

    /**
     * 替换包内文件<br>
     * 替换的实际目标是文件.<br>
     * 在调用save前,如果对targetFilePath文件进行修改,则发生无法预测行为
     * <p>
     *
     * @param pathInXDA      XDA文档内路径;
     * @param targetFilePath 待替换的目标文件路径;
     * @param ecs            ECS, 长度必须等于实际可用字节长度.
     * @throws FooE
     */
    void replaceFile(final String pathInXDA,
                     final String targetFilePath, byte[] ecs) throws FooE;

    /**
     * 替换包内文件<br>
     * 替换的实际目标是一段内存.<br>
     * 在调用save前,如果对targetFilePath文件进行修改,则发生无法预测行为
     * <p>
     *
     * @param pathInXDA  XDA文档内路径;
     * @param targetDate 待替换的目标内存;
     * @param ecs        ECS, 长度必须等于实际可用字节长度.
     * @throws FooE
     * @throws FooE
     */
    void replaceFile(final String pathInXDA, final byte[] targetDate,
                     byte[] ecs) throws FooE;

    /**
     * 移除包内文件
     * <p>
     *
     * @param pathInXDA XDA文档内路径;
     * @throws FooE
     */
    void removeFile(final String pathInXDA) throws FooE;

    /**
     * 添加指定目录所有子孙文件
     * <p>
     *
     * @param pathInXDA 挂接的XDA文档路径,""表示在从根开始;
     * @param dirPath   指定目录;
     * @param ecs       ECS, 长度必须等于实际可用字节长度.
     * @throws FooE
     */
    void addDir(String pathInXDA, String dirPath, byte[] ecs)
            throws FooE;

    /**
     * 移除指定目录下所有内容
     * <p>
     *
     * @param pathInXDA 待删除的目录路径;
     * @throws FooE
     */
    void removeDir(String pathInXDA);

    /**
     * 提取包内文件
     * <p>
     * 提取到文件中.
     * <p>
     *
     * @param pathInXDA      XDA文档内路径;
     * @param targetFilePath 提取的文件路径;
     * @throws FileNotFoundException
     * @throws FooE
     * @throws IOException
     */
    void extractFile(final String pathInXDA, final String targetFilePath)
            throws FileNotFoundException, IOException, FooE;

    /**
     * 提取包内文件<br>
     * 提取出的文件由一段内存接受。
     * <p>
     *
     * @param pathInXDA XDA文档内路径
     * @return 装载提取文件内容的内存
     * @throws FooE
     * @throws IOException
     * @throws FileNotFoundException
     * @throws FooE
     * @throws IOException
     */
    byte[] extractFile(final String pathInXDA) throws IOException,
            FooE;

    /**
     * 提取包根目录下的所有子孙文件到指定文件夹
     * 相当于调用了extractDir("", dir);
     * <p>
     *
     * @param dir 指定文件夹
     * @throws FileNotFoundException
     * @throws FooE
     * @throws IOException
     * @see #extractDir(String, String)
     */
    void extractDir(String dir) throws IOException, FooE;

    /**
     * 提取包内指定路径下的所有子孙文件到指定文件夹
     * <p>
     *
     * @param pathInXDA 包内指定路径,""相当于从跟开始
     * @param dir       指定文件夹
     * @throws FileNotFoundException
     * @throws FooE
     * @throws IOException
     */
    void extractDir(String pathInXDA, String dir) throws IOException,
            FooE;

    /**
     * 对文件校验和进行检查
     * <p>
     *
     * @return 校验和是否和符合包文件
     */
    boolean validateFile();

    /**
     * 获取主版本号
     * <p>
     *
     * @return 主版本号
     * @throws FooE
     */
    int getMajorVersion() throws FooE;

    /**
     * 获取次版本号
     * <p>
     *
     * @return 次版本号
     * @throws FooE
     */
    int getMinorVersion() throws FooE;

}
