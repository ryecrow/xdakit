package io.technicrow.xdakit;

public interface XDA extends AutoCloseable {

    boolean validate();

    int getMajorVersion();

    int getMinorVersion();
}
