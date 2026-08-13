class UploadError(Exception):
    """Base class for upload service errors."""


class UploadTooLargeError(UploadError):
    """Raised when an uploaded file exceeds the configured size cap."""


class UploadServiceError(UploadError):
    """Raised when the upload service fails due to a downstream (dao) failure."""
