package io.technicrow.xdakit;

/**
 * XDA Exception
 */
public class XDAException extends Exception {

    public XDAException(String message) {
        super(message);
    }

    public XDAException(String message, Throwable cause) {
        super(message, cause);
    }
}
