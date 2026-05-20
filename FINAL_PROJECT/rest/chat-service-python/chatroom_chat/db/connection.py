import psycopg
from contextlib import contextmanager

from config import Config


def _dsn() -> str:
    return (
        f"host={Config.DB_HOST} port={Config.DB_PORT} "
        f"dbname={Config.DB_NAME} user={Config.DB_USER} password={Config.DB_PASSWORD}"
    )


@contextmanager
def connection():
    conn = psycopg.connect(_dsn())
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
