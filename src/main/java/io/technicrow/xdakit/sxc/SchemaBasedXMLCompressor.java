package io.technicrow.xdakit.sxc;

import io.technicrow.xdakit.Utils;
import io.technicrow.xdakit.XDAException;
import io.technicrow.xdakit.constant.SXCContentAppearance;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

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

    public SchemaBasedXMLCompressor(InputStream source) throws IOException, XDAException {
        this.source = source;
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
        if (contentAppearanceInfo.contains(SXCContentAppearance.NAMESPACE_INFORMATION) ) {
            builder.namespaceInformationOffset(Utils.readInt(source));
            builder.namespaceInformationLength(Utils.readInt(source));
        }
        if (contentAppearanceInfo.contains(SXCContentAppearance.DICTIONARY) ) {
            builder.dictionaryOffset(Utils.readInt(source));
            builder.dictionaryLength(Utils.readInt(source));
        }
        if (contentAppearanceInfo.contains(SXCContentAppearance.STRUCTURED_DATA) ) {
            builder.structureDataOffset(Utils.readInt(source));
            builder.structureDataLength(Utils.readInt(source));
        }
        if (contentAppearanceInfo.contains(SXCContentAppearance.DATA_CONTAINERS_AND_MAP) ) {
            builder.dataContainersOffset(Utils.readInt(source));
            builder.dataContainersLength(Utils.readInt(source));
            builder.dataContainersMapOffset(Utils.readInt(source));
            builder.dataContainersMapLength(Utils.readInt(source));
        }
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

    @Override
    public void close() throws IOException {
        source.close();
    }
}
