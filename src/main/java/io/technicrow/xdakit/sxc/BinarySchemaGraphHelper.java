package io.technicrow.xdakit.sxc;

import io.technicrow.xdakit.Utils;
import io.technicrow.xdakit.XDAException;
import io.technicrow.xdakit.constant.DataType;
import io.technicrow.xdakit.constant.SchemaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class of SXC Binary Schema Graph
 */
public final class BinarySchemaGraphHelper {

    private BinarySchemaGraphHelper() {
        throw new AssertionError("No instance of BinarySchemaGraphHelper for you!");
    }

    private static final int FILE_HEADER_LENGTH = 22;
    private static final int CHECK_INFORMATION_LENGTH = 16;

    public static SchemaGraph parseBSG(InputStream bsgFile) throws IOException, XDAException {
        if (bsgFile == null) {
            return null;
        }
        try (InputStream bsg = bsgFile) {
            byte[] header = new byte[FILE_HEADER_LENGTH];
            if (bsg.read(header) != FILE_HEADER_LENGTH) {
                throw new XDAException("Cannot read BSG header");
            }
            byte[] checkInformation = new byte[CHECK_INFORMATION_LENGTH];
            if (bsg.read(checkInformation) != CHECK_INFORMATION_LENGTH) {
                throw new XDAException("Cannot read BSG checkInformation");
            }
            int nodeCount = Utils.readInt(bsg);
            int count = 0;
            List<SchemaNode> schemaNodes = new LinkedList<>();
            while (count < nodeCount) {
                schemaNodes.add(readNode(bsg));
                count++;
            }
            List<Integer> elements = new LinkedList<>();
            while (true) {
                byte[] b = new byte[4];
                if (bsg.read(b) != 4) {
                    break;
                }
                int element = ((b[3] << 24) & 0xff000000) + ((b[2] << 16) & 0x00ff0000)
                        + ((b[1] << 8) & 0x0000ff00) + (b[0] & 0x000000ff);
                elements.add(element);
            }
            return new SchemaGraph(header, checkInformation, nodeCount, schemaNodes, elements);
        }
    }

    private static SchemaNode readNode(InputStream bsg) throws IOException, XDAException {
        SchemaNode.SchemaNodeBuilder builder = new SchemaNode.SchemaNodeBuilder();
        int type = bsg.read();
        SchemaType schemaType = SchemaType.ofType((byte) type);
        if (schemaType == null) {
            throw new XDAException("Invalid schema type: " + type);
        }
        builder.schemaNodeType(schemaType);
        if (SchemaType.ATTRIBUTE.equals(schemaType) || SchemaType.ELEMENT.equals(schemaType)) {
            int nameLength = Utils.readInt(bsg);
            byte[] nameBuf = new byte[nameLength * 2];
            for (int i = 0; i < nameLength; i++) {
                nameBuf[i * 2] = (byte) bsg.read();
                nameBuf[i * 2 + 1] = (byte) bsg.read();
            }
            builder.schemaNodeNameLength(nameLength);
            builder.schemaNodeName(new String(nameBuf, StandardCharsets.UTF_16LE));
            type = bsg.read();
            DataType dataType = DataType.ofType((byte) type);
            if (dataType == null) {
                throw new XDAException("Invalid data type: " + type);
            }
            builder.dataType(dataType);
        }
        if (!SchemaType.ATTRIBUTE.equals(schemaType)) {
            builder.minOccurs(Utils.readInt(bsg));
            builder.maxOccurs(Utils.readInt(bsg));
        } else {
            builder.attributeRequired(bsg.read() != 0);
        }
        if (SchemaType.ELEMENT.equals(schemaType)) {
            builder.mixed(bsg.read() != 0);
            int attributeCount = Utils.readInt(bsg);
            builder.attributeCount(attributeCount);
            if (attributeCount > 0) {
                List<Integer> attributes = new LinkedList<>();
                for (int i = 0; i < attributeCount; i++) {
                    attributes.add(Utils.readInt(bsg));
                }
                builder.attributes(attributes);
            }
        }
        if (!SchemaType.ATTRIBUTE.equals(schemaType)) {
            int childrenCount = Utils.readInt(bsg);
            builder.childrenCount(childrenCount);
            if (childrenCount > 0) {
                List<Integer> children = new LinkedList<>();
                for (int i = 0; i < childrenCount; i++) {
                    children.add(Utils.readInt(bsg));
                }
                builder.children(children);
            }
        }
        return builder.build();
    }
}
