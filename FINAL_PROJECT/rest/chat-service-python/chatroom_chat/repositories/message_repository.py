from ..db.connection import connection


def insert(room_id: int, user_id: int, username: str, content: str) -> int:
    with connection() as conn, conn.cursor() as cur:
        cur.execute(
            "INSERT INTO messages (room_id, user_id, username, content) "
            "VALUES (%s, %s, %s, %s) RETURNING id",
            (room_id, user_id, username, content),
        )
        return int(cur.fetchone()[0])


def list_since(room_id: int, since_id: int) -> list[dict]:
    with connection() as conn, conn.cursor() as cur:
        cur.execute(
            "SELECT id, room_id, user_id, username, content, created_at "
            "FROM messages WHERE room_id = %s AND id > %s ORDER BY id",
            (room_id, since_id),
        )
        return [
            {
                "id": int(r[0]),
                "roomId": r[1],
                "userId": r[2],
                "username": r[3],
                "content": r[4],
                "createdAt": r[5].isoformat() if r[5] else "",
            }
            for r in cur.fetchall()
        ]
