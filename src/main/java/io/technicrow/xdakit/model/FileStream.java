package io.technicrow.xdakit.model;

import io.technicrow.xdakit.Utils;
import jakarta.activation.DataSource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link jakarta.activation.DataSource} representation of XDA FileStream
 */
public class FileStream implements DataSource {

    private String path;

    private Byte checkSum;

    private Long length;

    private byte[] ecs;

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
        return Utils.getContentTypeByExtension(FilenameUtils.getExtension(path));
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(path);
    }

    public FileStream(String path, Byte checkSum, Long length, byte[] ecs, InputStream data) {
        this.path = path;
        this.checkSum = checkSum;
        this.length = length;
        this.ecs = ecs;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Byte getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(Byte checkSum) {
        this.checkSum = checkSum;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public byte[] getEcs() {
        return ecs;
    }

    public void setEcs(byte[] ecs) {
        this.ecs = ecs;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FileStream that = (FileStream) o;

        return new EqualsBuilder().append(path, that.path).append(checkSum, that.checkSum).append(length, that.length).append(ecs, that.ecs).append(data, that.data).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(path).append(checkSum).append(length).append(ecs).append(data).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("path", path)
                .append("checkSum", checkSum)
                .append("length", length)
                .toString();
    }
}
