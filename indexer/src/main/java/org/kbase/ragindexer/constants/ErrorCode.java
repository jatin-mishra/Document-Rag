package org.kbase.ragindexer.constants;

public enum ErrorCode {
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);
    private final int code;
    ErrorCode(int code) {
        this.code = code;
    }
}
