package org.kbase.ragindexer.handlers.validators;

import org.kbase.ragindexer.error.AppException;

public interface IValidator<T> {
    void validate(T request) throws AppException;
}
