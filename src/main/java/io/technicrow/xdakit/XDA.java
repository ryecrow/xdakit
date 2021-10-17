package io.technicrow.xdakit;

import io.technicrow.xdakit.model.FileStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public interface XDA extends AutoCloseable {

    boolean validate();

    @Nonnull
    List<String> listAllFiles();

    @Nullable
    FileStream getFile(@Nonnull String path) throws IOException, XDAException;

    int getMajorVersion();

    int getMinorVersion();
}
