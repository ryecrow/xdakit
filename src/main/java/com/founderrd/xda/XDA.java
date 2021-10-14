package com.founderrd.xda;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * The XDA Interface
 *
 * @author TH.Yang (tianhang.yang@gmail.com)
 * @version 1.00 11 Nov 2009
 */
public interface XDA extends AutoCloseable {


    /**
     * �½���XDA�ĵ���<br>
     * ʹ��Ĭ��λ�������д������൱�ڵ�����create(0x00)��
     * <p>
     *
     * @param filePath �½�XDA�ĵ����ļ�·��
     * @throws FileNotFoundException
     * @see #create(String, byte)
     */

    /**
     * ����һ��XDA�ĵ�
     * <p>
     *
     * @param nameTableCompress ���Ϊtrue�����NameTable����ѹ��
     * @param itemListCompress  ���Ϊtrue�����ItemList����ѹ��
     * @throws IOException
     * @throws XDAException
     * @throws NoSuchAlgorithmException
     */
    void save(boolean nameTableCompress, boolean itemListCompress) throws NoSuchAlgorithmException, XDAException, IOException;

    /**
     * ����һ��XDA�ĵ�<br>
     * ����XDA�ĵ�,��NameTable��ItemList������ѹ��.<br>
     * �൱�ڵ�����save(true, true).
     * <p>
     *
     * @throws XDAException
     * @throws NoSuchAlgorithmException
     * @see #save(boolean, boolean)
     */
    void save() throws NoSuchAlgorithmException, XDAException, IOException;

    /**
     * ���Ϊһ���µ�XDA�ļ���<br>
     * ��XDA�ļ�ֻ��һ��Entry��Ϊ��ǰXDA�ļ��߼��ϵ����°汾��
     * <p>
     *
     * @param newXDAPath        ��XDA�ļ�·��
     * @param bitsParam         λ����
     * @param nameTableCompress ���Ϊtrue�����NameTable����ѹ��
     * @param itemListCompress  ���Ϊtrue�����ItemList����ѹ��
     * @throws IOException
     * @throws XDAException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws XDAException
     */
    void saveAs(String newXDAPath, byte bitsParam,
                boolean nameTableCompress, boolean itemListCompress)
            throws NoSuchAlgorithmException, XDAException, IOException;

    /**
     * ���Ϊһ���µ�XDA�ļ�<br>
     * ��XDA�ļ�ֻ��һ��Entry��Ϊ��ǰXDA�����µİ汾��
     * ʹ��Ĭ�ϵ�bitsParam����NameTable��ItemList������ѹ����
     * �൱�ڵ�����savaAs(newXDAPath, 0x04, true, true)��
     * <p>
     *
     * @param newXDAPath ��XDA�ļ�·��
     * @throws IOException
     * @throws XDAException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws XDAException
     * @see #saveAs(String, byte, boolean, boolean)
     */
    void saveAs(String newXDAPath) throws NoSuchAlgorithmException,
            XDAException, IOException;

    /**
     * ������һ���ļ�<br>
     * ��ӵ�ʵ��Ŀ�����ļ���<br>
     * �ڵ���saveǰ,�����targetFilePath�ļ������޸ġ������޷�Ԥ����Ϊ��
     * <p>
     *
     * @param pathInXDA      XDA�ĵ���·��;
     * @param targetFilePath ����ӵ�Ŀ���ļ�·��;
     * @param ecs            ECS, ���ȱ������ʵ�ʿ����ֽڳ���.
     * @throws XDAException
     */
    void addFile(final String pathInXDA, final String targetFilePath,
                 byte[] ecs) throws XDAException;

    /**
     * ������һ���ļ�<br>
     * ��ӵ�ʵ��Ŀ����һ���ڴ档<br>
     * �ڵ���saveǰ,�����targetDate�ļ������޸ģ������޷�Ԥ����Ϊ��
     * <p>
     *
     * @param pathInXDA  XDA�ĵ���·��;
     * @param targetDate ����ӵ�Ŀ���ڴ�;
     * @param ecs        ECS, ���ȱ������ʵ�ʿ����ֽڳ��ȡ�
     * @throws XDAException
     * @throws XDAException
     */
    void addFile(final String pathInXDA, final byte[] targetDate,
                 byte[] ecs) throws XDAException;

    /**
     * �滻�����ļ�<br>
     * �滻��ʵ��Ŀ�����ļ�.<br>
     * �ڵ���saveǰ,�����targetFilePath�ļ������޸�,�����޷�Ԥ����Ϊ
     * <p>
     *
     * @param pathInXDA      XDA�ĵ���·��;
     * @param targetFilePath ���滻��Ŀ���ļ�·��;
     * @param ecs            ECS, ���ȱ������ʵ�ʿ����ֽڳ���.
     * @throws XDAException
     */
    void replaceFile(final String pathInXDA,
                     final String targetFilePath, byte[] ecs) throws XDAException;

    /**
     * �滻�����ļ�<br>
     * �滻��ʵ��Ŀ����һ���ڴ�.<br>
     * �ڵ���saveǰ,�����targetFilePath�ļ������޸�,�����޷�Ԥ����Ϊ
     * <p>
     *
     * @param pathInXDA  XDA�ĵ���·��;
     * @param targetDate ���滻��Ŀ���ڴ�;
     * @param ecs        ECS, ���ȱ������ʵ�ʿ����ֽڳ���.
     * @throws XDAException
     * @throws XDAException
     */
    void replaceFile(final String pathInXDA, final byte[] targetDate,
                     byte[] ecs) throws XDAException;

    /**
     * �Ƴ������ļ�
     * <p>
     *
     * @param pathInXDA XDA�ĵ���·��;
     * @throws XDAException
     */
    void removeFile(final String pathInXDA) throws XDAException;

    /**
     * ���ָ��Ŀ¼���������ļ�
     * <p>
     *
     * @param pathInXDA �ҽӵ�XDA�ĵ�·��,""��ʾ�ڴӸ���ʼ;
     * @param dirPath   ָ��Ŀ¼;
     * @param ecs       ECS, ���ȱ������ʵ�ʿ����ֽڳ���.
     * @throws XDAException
     */
    void addDir(String pathInXDA, String dirPath, byte[] ecs)
            throws XDAException;

    /**
     * �Ƴ�ָ��Ŀ¼����������
     * <p>
     *
     * @param pathInXDA ��ɾ����Ŀ¼·��;
     * @throws XDAException
     */
    void removeDir(String pathInXDA);

    /**
     * ��ȡ�����ļ�
     * <p>
     * ��ȡ���ļ���.
     * <p>
     *
     * @param pathInXDA      XDA�ĵ���·��;
     * @param targetFilePath ��ȡ���ļ�·��;
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     */
    void extractFile(final String pathInXDA, final String targetFilePath)
            throws FileNotFoundException, IOException, XDAException;

    /**
     * ��ȡ�����ļ�<br>
     * ��ȡ�����ļ���һ���ڴ���ܡ�
     * <p>
     *
     * @param pathInXDA XDA�ĵ���·��
     * @return װ����ȡ�ļ����ݵ��ڴ�
     * @throws XDAException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     */
    byte[] extractFile(final String pathInXDA) throws IOException,
            XDAException;

    /**
     * ��ȡ����Ŀ¼�µ����������ļ���ָ���ļ���
     * �൱�ڵ�����extractDir("", dir);
     * <p>
     *
     * @param dir ָ���ļ���
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     * @see #extractDir(String, String)
     */
    void extractDir(String dir) throws IOException, XDAException;

    /**
     * ��ȡ����ָ��·���µ����������ļ���ָ���ļ���
     * <p>
     *
     * @param pathInXDA ����ָ��·��,""�൱�ڴӸ���ʼ
     * @param dir       ָ���ļ���
     * @throws FileNotFoundException
     * @throws XDAException
     * @throws IOException
     */
    void extractDir(String pathInXDA, String dir) throws IOException,
            XDAException;

    /**
     * ���ļ�У��ͽ��м��
     * <p>
     *
     * @return У����Ƿ�ͷ��ϰ��ļ�
     */
    boolean validate();

    /**
     * ��ȡ���汾��
     * <p>
     *
     * @return ���汾��
     * @throws XDAException
     */
    int getMajorVersion();

    /**
     * ��ȡ�ΰ汾��
     * <p>
     *
     * @return �ΰ汾��
     * @throws XDAException
     */
    int getMinorVersion();
}
