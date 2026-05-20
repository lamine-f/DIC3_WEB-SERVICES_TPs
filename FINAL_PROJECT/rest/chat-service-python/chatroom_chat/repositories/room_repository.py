from ..db.connection import connection


def list_all() -> list[dict]:
    with connection() as conn, conn.cursor() as cur:
        cur.execute("SELECT id, name, created_by FROM rooms ORDER BY id")
        return [
            {"id": r[0], "name": r[1], "createdBy": r[2]}
            for r in cur.fetchall()
        ]


def create(name: str, created_by: int) -> dict:
    with connection() as conn, conn.cursor() as cur:
        cur.execute(
            "INSERT INTO rooms (name, created_by) VALUES (%s, %s) RETURNING id, name, created_by",
            (name, created_by),
        )
        r = cur.fetchone()
        return {"id": r[0], "name": r[1], "createdBy": r[2]}


def find_by_name(name: str) -> dict | None:
    with connection() as conn, conn.cursor() as cur:
        cur.execute(
            "SELECT id, name, created_by FROM rooms WHERE name = %s",
            (name,),
        )
        r = cur.fetchone()
        if r is None:
            return None
        return {"id": r[0], "name": r[1], "createdBy": r[2]}


def exists(room_id: int) -> bool:
    with connection() as conn, conn.cursor() as cur:
        cur.execute("SELECT 1 FROM rooms WHERE id = %s", (room_id,))
        return cur.fetchone() is not None
