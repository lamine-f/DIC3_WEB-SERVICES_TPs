import os


class Config:
    DB_HOST = os.environ.get("DB_HOST", "localhost")
    DB_PORT = int(os.environ.get("DB_PORT", "5435"))
    DB_NAME = os.environ.get("DB_NAME", "chatroom_chat_rest")
    DB_USER = os.environ.get("DB_USER", "postgres")
    DB_PASSWORD = os.environ.get("DB_PASSWORD", "postgres")

    SERVICE_PORT = int(os.environ.get("SERVICE_PORT", "8082"))

    AUTH_BASE_URL = os.environ.get("AUTH_BASE_URL", "http://localhost:8081")
