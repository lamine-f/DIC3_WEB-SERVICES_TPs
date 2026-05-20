const pool = require('../db');

async function insert(roomId, userId, username, content) {
  const { rows } = await pool.query(
    `INSERT INTO messages (room_id, user_id, username, content)
     VALUES ($1, $2, $3, $4)
     RETURNING id`,
    [roomId, userId, username, content]
  );
  return Number(rows[0].id);
}

async function listSince(roomId, sinceId) {
  const { rows } = await pool.query(
    `SELECT id, room_id, user_id, username, content, created_at
     FROM messages
     WHERE room_id = $1 AND id > $2
     ORDER BY id`,
    [roomId, sinceId]
  );
  return rows.map(r => ({
    id: Number(r.id),
    roomId: r.room_id,
    userId: r.user_id,
    username: r.username,
    content: r.content,
    createdAt: r.created_at.toISOString(),
  }));
}

module.exports = { insert, listSince };
