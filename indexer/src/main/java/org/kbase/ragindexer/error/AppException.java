package org.kbase.ragindexer.error;

import lombok.Builder;
import lombok.Getter;
import org.kbase.ragindexer.constants.ErrorCode;

import java.util.Map;

@Getter
@Builder
public class AppException extends RuntimeException {
    private Integer statusCode;
    private ErrorCode errorCode;
    private String message;
    private Map<String, Object> details;
    private String howToFix;
}
