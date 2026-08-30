


class Settings:
    app_name: str 
    app_version: str 

    def __init__(self):
        self.app_name = "retrieval-service"
        self.app_version = "v1.0.0"



def get_settings():
    return Settings()