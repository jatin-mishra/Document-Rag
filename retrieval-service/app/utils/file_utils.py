import re
import uuid

_UNSAFE_FILENAME_CHARS = re.compile(r"[^A-Za-z0-9._-]+")


def build_object_key(filename: str) -> str:
    safe_name = _UNSAFE_FILENAME_CHARS.sub("_", filename.strip()) or "file"
    return f"uploads/{uuid.uuid4()}/{safe_name}"
