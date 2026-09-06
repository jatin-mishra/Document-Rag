package org.kbase.ragindexer.error;

import org.kbase.ragindexer.constants.ErrorCode;

public enum Error {

    bad_request(AppException.builder().statusCode(400).errorCode(ErrorCode.BAD_REQUEST).message("Bad Request")),
    not_found(AppException.builder().statusCode(404).errorCode(ErrorCode.NOT_FOUND).message("Not found")),
    internal_server_error(AppException.builder().statusCode(500).errorCode(ErrorCode.INTERNAL_SERVER_ERROR).message("Internal server error"));

    private AppException.AppExceptionBuilder builder;

    Error(AppException.AppExceptionBuilder builder) {
        this.builder = builder;
    }

    public AppException.AppExceptionBuilder builder(){
        return builder;
    }

    public AppException build(){
        return builder.build();
    }
}
