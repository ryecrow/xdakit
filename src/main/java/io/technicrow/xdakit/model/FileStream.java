package io.technicrow.xdakit.model;

import io.technicrow.xdakit.Utils;
import jakarta.activation.DataSource;
import lombok.*;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link jakarta.activation.DataSource} representation of XDA FileStream
 */
@ToString(exclude = "data")
@EqualsAndHashCode
@AllArgsConstructor
public class FileStream implements DataSource {

    @Setter
    private String name;

    @Getter
    @Setter
    private Byte checkSum;

    @Getter
    @Setter
    private Long length;

    @Getter
    @Setter
    private byte[] ecs;

    @Setter
    private InputStream data;

    @Override
    public InputStream getInputStream() {
        return data;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Operation not supported");
    }

    @Override
    public String getContentType() {
        return Utils.getContentTypeByExtension(FilenameUtils.getExtension(name));
    }

    @Override
    public String getName() {
        return name;
    }
}
