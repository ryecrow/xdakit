package io.technicrow.xdakit.sxc;

import io.technicrow.xdakit.InputStreamDecorator;
import io.technicrow.xdakit.Utils;
import io.technicrow.xdakit.XDAException;
import io.technicrow.xdakit.constant.SXCContentAppearance;
import org.apache.commons.io.input.NullInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * SXC base class
 */
public class SchemaBasedXMLCompressor implements AutoCloseable {

    private static final byte[] SXC_HEADER = {'?', '?', 'S', 'X', 'C'};
    private static final int CHECK_INFORMATION_LENGTH = 16;
    private static final byte NAMESPACE_MASK = 0x08;
    private static final byte DICTIONARY_MASK = 0x04;
    private static final byte STRUCTURE_DATA_MASK = 0x02;
    private static final byte DATA_CONTAINER_MASK = 0x01;

    private final InputStream source;
    private final SchemaGraph bsg;

    public SchemaBasedXMLCompressor(InputStream source, SchemaGraph bsg) throws IOException, XDAException {
        this.source = source;
        this.bsg = bsg;
        validateHeader();
    }

    private void validateHeader() throws IOException, XDAException {
        byte[] fh = new byte[SXC_HEADER.length];
        if (((source.read(fh) != SXC_HEADER.length) || !Arrays.equals(fh, SXC_HEADER))) {
            throw new XDAException("Incorrect SXC header");
        }
    }

    public void readSXC() throws IOException, XDAException {
        SXC.SXCBuilder builder = new SXC.SXCBuilder();
        builder.versionNo(Utils.readInt(source));
        byte[] checkInformation = new byte[CHECK_INFORMATION_LENGTH];
        if (source.read(checkInformation) != CHECK_INFORMATION_LENGTH) {
            throw new XDAException("Failed to read SXC checkInformation");
        }
        builder.compressMethod((byte) source.read());
        byte appearanceInfo = (byte) source.read();
        Set<SXCContentAppearance> contentAppearanceInfo = listAppearance(appearanceInfo);
        builder.contentAppearanceInformation(contentAppearanceInfo);
        if (contentAppearanceInfo.contains(SXCContentAppearance.NAMESPACE_INFORMATION)) {
            builder.namespaceInformationOffset(Utils.readInt(source));
            builder.namespaceInformationLength(Utils.readInt(source));
        }
        if (contentAppearanceInfo.contains(SXCContentAppearance.DICTIONARY)) {
            builder.dictionaryOffset(Utils.readInt(source));
            builder.dictionaryLength(Utils.readInt(source));
        }
        if (contentAppearanceInfo.contains(SXCContentAppearance.STRUCTURED_DATA)) {
            builder.structureDataOffset(Utils.readInt(source));
            builder.structureDataLength(Utils.readInt(source));
        }
        if (contentAppearanceInfo.contains(SXCContentAppearance.DATA_CONTAINERS_AND_MAP)) {
            builder.dataContainersOffset(Utils.readInt(source));
            builder.dataContainersLength(Utils.readInt(source));
            builder.dataContainersMapOffset(Utils.readInt(source));
            builder.dataContainersMapLength(Utils.readInt(source));
        }
        SXC sxc = builder.build();
        readNamespaceInformation(sxc);
        readDictionary(sxc);
        readStructuredDataInformation(sxc);
        readDataContainers(sxc);
        readDataContainersMap(sxc);
        System.out.println(sxc);
    }

    private Set<SXCContentAppearance> listAppearance(byte appearanceInfo) {
        Set<SXCContentAppearance> appearances = EnumSet.noneOf(SXCContentAppearance.class);
        if (appearanceInfo != 0) {
            if ((appearanceInfo & NAMESPACE_MASK) != 0) {
                appearances.add(SXCContentAppearance.NAMESPACE_INFORMATION);
            }
            if ((appearanceInfo & DICTIONARY_MASK) != 0) {
                appearances.add(SXCContentAppearance.DICTIONARY);
            }
            if ((appearanceInfo & STRUCTURE_DATA_MASK) != 0) {
                appearances.add(SXCContentAppearance.STRUCTURED_DATA);
            }
            if ((appearanceInfo & DATA_CONTAINER_MASK) != 0) {
                appearances.add(SXCContentAppearance.DATA_CONTAINERS_AND_MAP);
            }
        }
        return appearances;
    }

    private void readNamespaceInformation(SXC sxc) throws IOException, XDAException {
        int offset = sxc.getNamespaceInformationOffset();
        if (offset == 0) {
            return;
        }
        try (InputStream in = readData((sxc.getCompressMethod() == 1), sxc.getNamespaceInformationLength(), source)) {

        }
    }

    private void readDictionary(SXC sxc) throws IOException, XDAException {
        int offset = sxc.getDictionaryOffset();
        if (offset == 0) {
            return;
        }
        try (InputStream in = readData((sxc.getCompressMethod() == 1), sxc.getDictionaryLength(), source)) {

        }
    }

    private void readStructuredDataInformation(SXC sxc) throws IOException, XDAException {
        int offset = sxc.getStructureDataOffset();
        if (offset == 0) {
            return;
        }
        try (InputStream in = readData((sxc.getCompressMethod() == 1), sxc.getStructureDataLength(), source)) {

        }
    }

    private void readDataContainers(SXC sxc) throws IOException, XDAException {
        int offset = sxc.getDataContainersOffset();
        if (offset == 0) {
            return;
        }
        try (InputStream in = readData((sxc.getCompressMethod() == 1), sxc.getDataContainersLength(), source)) {

        }
    }

    private void readDataContainersMap(SXC sxc) throws IOException, XDAException {
        int offset = sxc.getDataContainersMapOffset();
        if (offset == 0) {
            return;
        }
        DataContainersMap.DataContainersMapBuilder builder = DataContainersMap.builder();
        boolean encoded = (source.read() == 1);
        builder.encoded(encoded);
        boolean compressed = (source.read() == 1);
        builder.compressed(compressed);
        try (InputStream in = readData(compressed, (sxc.getDataContainersMapLength() - 2), source)) {
            int uncombinedCount = Utils.readInt(in);
            builder.uncombinedDataContainersCount(uncombinedCount);
            int combinedCount = Utils.readInt(in);
            builder.combinedDataContainersCount(combinedCount);
            if (uncombinedCount > 0) {
                List<DataContainerEntry> uncombined = new LinkedList<>();
                for (int i = 0; i < uncombinedCount; i++) {
                    uncombined.add(new DataContainerEntry(Utils.readInt(in), Utils.readInt(in)));
                }
                builder.uncombinedDataContainersInformation(uncombined);
            }
            if (combinedCount > 0) {
                List<CombinedDataContainerEntry> combined = new LinkedList<>();
                for (int i = 0; i < combinedCount; i++) {
                    combined.add(readCombinedEntry(in));
                }
                builder.combinedDataContainersInformation(combined);
            }
        }
        DataContainersMap map = builder.build();
        sxc.setDataContainersMap(map);
    }

    private CombinedDataContainerEntry readCombinedEntry(InputStream mapData) throws IOException {
        int count =  Utils.readInt(mapData);
        int length = Utils.readInt(mapData);
        List<DataContainerEntry> entries = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            entries.add(new DataContainerEntry(Utils.readInt(mapData), Utils.readInt(mapData)));
        }
        return new CombinedDataContainerEntry(count, length, entries);
    }

    private InputStream readData(boolean compressed, int length, InputStream source) throws IOException, XDAException {
        if (length == 0) {
            return new NullInputStream();
        }
        byte[] sd = new byte[length];
        if (source.read(sd) != length) {
            throw new XDAException("Failed to read SXC data");
        }
        return new InputStreamDecorator(new ByteArrayInputStream(sd), compressed);
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}
