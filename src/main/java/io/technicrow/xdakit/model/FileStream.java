package io.technicrow.xdakit.model;

import jakarta.activation.DataSource;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The XDA BitStream entity
 */
@Data
public class FileStream implements DataSource {

    private Byte checksum;

    private Long length;

    private byte[] ecs;

    private String name;

    private String contentType;

    public InputStream getFileData() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getFileData();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }
}
