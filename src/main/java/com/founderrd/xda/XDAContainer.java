/**
 * Title:	XDAContainer
 * Description:	实现XDAInterface接口，底层使用了XDADocument和XDADecorator
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

public class XDAContainer implements XDAInterface {
    final XDADecorator dec;
    private final XDADocument doc;

    public XDAContainer() {
        this.doc = new XDADocument();
        this.dec = new XDADecorator();
    }

    // XDA文档操作
    public void create(String filePath, byte bitsParam)
            throws FileNotFoundException, XDAException {
        File xdaFile = new File(filePath);
        doc.create(xdaFile, (byte) 0x01, (byte) 0x00, (byte) 0x00, bitsParam);
    }

    public void create(final String filePath) throws FileNotFoundException,
            XDAException {
        create(filePath, (byte) 0x04);
    }

    public void open(String filePath) throws IOException, XDAException {
        File xdaFile = new File(filePath);
        if (!xdaFile.exists() || xdaFile.isDirectory())
            throw new XDAException(XDAException.INVALID_FILE_PATH);

        doc.open(xdaFile);
    }

    public void close() throws IOException {
        doc.close();
    }

    public void save(boolean nameTableCompress, boolean itemListCompress)
            throws NoSuchAlgorithmException, XDAException, IOException {
        byte entryCompressMark = 0x00;
        if (nameTableCompress)
            entryCompressMark |= COMPRESSNAMETABLE_MASK;
        if (itemListCompress)
            entryCompressMark |= COMPRESSITEMLIST_MASK;

        doc.saveChanges(entryCompressMark);
    }

    public void save() throws NoSuchAlgorithmException, XDAException,
            IOException {
        save(true, true);
    }

    public void saveAs(String newXDAPath, byte bitsParam,
                       boolean nameTableCompress, boolean itemListCompress) throws NoSuchAlgorithmException, XDAException, IOException {
        byte entryCompress = 0;
        if (nameTableCompress)
            entryCompress &= 0x01;
        if (itemListCompress)
            entryCompress &= 0x02;

        File newFile = new File(newXDAPath);
        doc.saveAs(newFile, (byte) 0x01, (byte) 0x00, (byte) 0x00, bitsParam, entryCompress);
    }

    public void saveAs(String newXDAPath) throws NoSuchAlgorithmException, XDAException, IOException {
        saveAs(newXDAPath, (byte) 0x04, true, true);
    }

    // 编辑操作
    public void addFile(String pathInXDA, String targetFilePath, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = dec.decorate(targetFilePath, ecs);

        doc.insertItem(pathInXDA, inputSteam, ecs);
    }

    public void addFile(String pathInXDA, byte[] targetDate, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = dec.decorate(targetDate, ecs);
        doc.insertItem(pathInXDA, inputSteam, ecs);
    }

    public void replaceFile(String pathInXDA, String targetFilePath, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = dec.decorate(targetFilePath, ecs);
        doc.replaceItem(pathInXDA, inputSteam, ecs);
    }

    public void replaceFile(String pathInXDA, byte[] targetDate, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = dec.decorate(targetDate, ecs);
        doc.replaceItem(pathInXDA, inputSteam, ecs);
    }

    public void removeFile(String pathInXDA) throws XDAException {
        doc.deleteItem(pathInXDA);
    }

    public void addDir(String pathInXDA, String dirPath, byte[] ecs)
            throws XDAException {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return;

        String parentPath = pathInXDA.concat("\\");
        doAddDir(parentPath, dir, ecs);
    }

    public void removeDir(String pathInXDA) {
        // 检查pathInXDA
        char ch = pathInXDA.charAt(pathInXDA.length() - 1);
        String pathDir = pathInXDA;
        if ((ch != '\\') && (ch != '/')) {
            pathDir = pathDir.concat("\\");
        }

        Vector<String> allLogicExistedItem = doc.getAllLogicExistedItem();
        for (String oneItemPath : allLogicExistedItem) {
            if (oneItemPath.startsWith(pathDir))
                try {
                    doc.deleteItem(oneItemPath);
                } catch (XDAException e) {
                }
        }
    }

    public void extractFile(String pathInXDA, String targetFilePath)
            throws IOException, XDAException {
        OutputStream targetStream = new FileOutputStream(targetFilePath);
        doc.extractItemStream(pathInXDA, targetStream, dec);
    }

    @Override
    public byte[] extractFile(String pathInXDA) throws IOException,
            XDAException {
        ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
        doc.extractItemStream(pathInXDA, targetStream, dec);
        return targetStream.toByteArray();
    }

    @Override
    public void extractDir(String dir) throws IOException, XDAException {
        Vector<String> allLogicExistedItem = doc.getAllLogicExistedItem();
        for (String pathInXDA : allLogicExistedItem) {
            String targetFilePath = dir.concat(pathInXDA);
            createParentDir(targetFilePath);
            extractFile(pathInXDA, targetFilePath);
        }
    }

    @Override
    public void extractDir(String pathInXDA, String dir) throws XDAException {
        if (XDADocument.PACKPATH_PATTERN.matcher(pathInXDA).matches())
            throw new XDAException(XDAException.INVALID_PACK_PATH);

        char ch = pathInXDA.charAt(pathInXDA.length() - 1);
        if ((ch != '\\') && (ch != '/')) {
            pathInXDA = pathInXDA.concat("\\");
        }
        ch = dir.charAt(dir.length() - 1);
        if ((ch == '\\') || (ch == '/')) {
            dir = dir.substring(0, dir.length() - 2);
        }

        Vector<String> allLogicExistedItem = doc.getAllLogicExistedItem();
        for (String oneItemPath : allLogicExistedItem) {
            if (oneItemPath.startsWith(pathInXDA)) {
                try {
                    String extractFile = dir.concat(oneItemPath);
                    createFile(extractFile);
                    extractFile(oneItemPath, extractFile);
                } catch (IOException | XDAException e) {
                }
            }
        }
    }

    @Override
    public boolean validateFile() {
        return false;
    }

    @Override
    public int getMajorVersion() throws XDAException {
        return doc.getHeader().getMajorVersion();
    }

    @Override
    public int getMinorVersion() throws XDAException {
        return doc.getHeader().getMinorVersion();
    }

    private boolean createParentDir(String path) {
        File file = new File(path);
        File parent = file.getParentFile();
        return parent.mkdirs();
    }

    private void doAddDir(String parentPath, File dir, byte[] ecs)
            throws XDAException {
        File[] children = dir.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                doAddDir(parentPath.concat(child.getName()).concat("\\"),
                        child, ecs);
            } else {
                addFile(parentPath.concat(child.getName()), child
                        .getAbsolutePath(), ecs);
            }
        }
    }

    private boolean createFile(String filePath) throws IOException {
        File file = new File(filePath);
        File folder = file.getParentFile();
        folder.mkdirs();
        return file.createNewFile();
    }
}
