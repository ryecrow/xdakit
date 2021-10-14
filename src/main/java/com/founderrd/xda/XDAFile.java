package com.founderrd.xda;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

/**
 * Implementation of the {@link XDA} interface by the underlying {@link XDADocument} and
 * {@link XDADecorator} objects
 *
 * @author —ÓÃÏ∫Ω(tianhang.yang @ gmail.com)
 * @version 1.0
 */
public class XDAFile implements XDA {

    private static final byte NAME_TABLE_MASK = (byte) 0x01;

    /**
     * ItemList—πÀı—⁄¬Î
     */
    private static final byte ITEM_LIST_MASK = (byte) 0x02;

    private final XDADecorator decorator = new XDADecorator();
    private final XDADocument document;

    public XDAFile() {
        this.document = new XDADocument();
    }

    public XDAFile(String filePath) throws FileNotFoundException, XDAException {
        this(filePath, (byte) 0x04);
    }

    public XDAFile(String filePath, byte bitsParam) throws FileNotFoundException, XDAException {
        if (!Files.exists(Paths.get(filePath))) {
            throw new FileNotFoundException(filePath);
        }
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        this.document = new XDADocument(raf, (byte) 0x01, (byte) 0x00, (byte) 0x00, bitsParam);
    }

    private XDAFile(XDADocument document) {
        this.document = document;
    }

    /* --- Factorial Methods --*/

    /**
     * Create a new XDA file with version code of 1.0
     *
     * @param filePath  Path of the created XDA file
     * @param bitsParam bit parameters
     * @throws XDAException
     * @throws FileNotFoundException
     */
    public static XDAFile create(String filePath, byte bitsParam) throws IOException, XDAException {
        File file = new File(filePath);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        return new XDAFile(new XDADocument(raf, (byte) 0x01, (byte) 0x00, (byte) 0x00, bitsParam));
    }

    /**
     * {@inheritDoc}
     */
    public static XDAFile create(final String filePath) throws IOException {
        return create(filePath, (byte) 0x04);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String filePath) throws IOException, XDAException {
        File xdaFile = new File(filePath);
        if (!xdaFile.exists() || xdaFile.isDirectory())
            throw new XDAException(ExceptionMessage.INVALID_FILE_PATH);

        document.open(xdaFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        document.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(boolean nameTableCompress, boolean itemListCompress)
            throws NoSuchAlgorithmException, XDAException, IOException {
        byte entryCompressMark = 0x00;
        if (nameTableCompress)
            entryCompressMark |= NAME_TABLE_MASK;
        if (itemListCompress)
            entryCompressMark |= ITEM_LIST_MASK;

        document.saveChanges(entryCompressMark);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() throws NoSuchAlgorithmException, XDAException,
            IOException {
        save(true, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAs(String newXDAPath, byte bitsParam,
                       boolean nameTableCompress, boolean itemListCompress) throws NoSuchAlgorithmException, XDAException, IOException {
        byte entryCompress = 0;
        if (nameTableCompress)
            entryCompress &= 0x01;
        if (itemListCompress)
            entryCompress &= 0x02;

        File newFile = new File(newXDAPath);
        document.saveAs(newFile, (byte) 0x01, (byte) 0x00, (byte) 0x00, bitsParam, entryCompress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAs(String newXDAPath) throws NoSuchAlgorithmException, XDAException, IOException {
        saveAs(newXDAPath, (byte) 0x04, true, true);
    }

    /* --- EDIT ---*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFile(String pathInXDA, String targetFilePath, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = decorator.decorate(targetFilePath, ecs);

        document.insertItem(pathInXDA, inputSteam, ecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFile(String pathInXDA, byte[] targetDate, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = decorator.decorate(targetDate, ecs);
        document.insertItem(pathInXDA, inputSteam, ecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceFile(String pathInXDA, String targetFilePath, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = decorator.decorate(targetFilePath, ecs);
        document.replaceItem(pathInXDA, inputSteam, ecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceFile(String pathInXDA, byte[] targetDate, byte[] ecs)
            throws XDAException {
        XDAInputStream inputSteam = decorator.decorate(targetDate, ecs);
        document.replaceItem(pathInXDA, inputSteam, ecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFile(String pathInXDA) throws XDAException {
        document.deleteItem(pathInXDA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDir(String pathInXDA, String dirPath, byte[] ecs)
            throws XDAException {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return;

        String parentPath = pathInXDA.concat("\\");
        doAddDir(parentPath, dir, ecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDir(String pathInXDA) {
        // ºÏ≤ÈpathInXDA
        char ch = pathInXDA.charAt(pathInXDA.length() - 1);
        String pathDir = pathInXDA;
        if ((ch != '\\') && (ch != '/')) {
            pathDir = pathDir.concat("\\");
        }

        Vector<String> allLogicExistedItem = document.getAllLogicExistedItem();
        for (String oneItemPath : allLogicExistedItem) {
            if (oneItemPath.startsWith(pathDir))
                try {
                    document.deleteItem(oneItemPath);
                } catch (XDAException e) {
                }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extractFile(String pathInXDA, String targetFilePath)
            throws IOException, XDAException {
        OutputStream targetStream = new FileOutputStream(targetFilePath);
        document.extractItemStream(pathInXDA, targetStream, decorator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] extractFile(String pathInXDA) throws IOException,
            XDAException {
        ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
        document.extractItemStream(pathInXDA, targetStream, decorator);
        return targetStream.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extractDir(String dir) throws IOException, XDAException {
        Vector<String> allLogicExistedItem = document.getAllLogicExistedItem();
        for (String pathInXDA : allLogicExistedItem) {
            String targetFilePath = dir.concat(pathInXDA);
            createParentDir(targetFilePath);
            extractFile(pathInXDA, targetFilePath);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extractDir(String pathInXDA, String dir) throws XDAException {
        if (StringUtils.isBlank(pathInXDA) || XDADocument.PACKPATH_PATTERN.matcher(pathInXDA).matches()) {
            throw new XDAException("Invalid path: " + pathInXDA);
        }

        char ch = pathInXDA.charAt(pathInXDA.length() - 1);
        if ((ch != '\\') && (ch != '/')) {
            pathInXDA = pathInXDA.concat("\\");
        }
        ch = dir.charAt(dir.length() - 1);
        if ((ch == '\\') || (ch == '/')) {
            dir = dir.substring(0, dir.length() - 2);
        }

        Vector<String> allLogicExistedItem = document.getAllLogicExistedItem();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMajorVersion() {
        return document.getHeader().getMajorVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinorVersion() {
        return document.getHeader().getMinorVersion();
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
