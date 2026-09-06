from enum import Enum


class IndexingStatus(str, Enum):
    NOT_STARTED = "NOT_STARTED"
    TRIGGERED = "TRIGGERED"
    TRIGGER_FAILED = "TRIGGER_FAILED"

class UploadStatus(str, Enum):
    INITIATED = "INITIATED"
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"
