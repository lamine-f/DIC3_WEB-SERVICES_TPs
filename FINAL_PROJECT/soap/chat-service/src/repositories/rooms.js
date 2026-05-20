const pool = require('../db');

async function listAll() {
  const { rows } = await pool.query(
    'SELECT id, name, created_by FROM rooms ORDER BY id'
  );
  return rows.map(r => ({ id: r.id, name: r.name, createdBy: r.created_by }));
}

async function create(name, createdBy) {
  const { rows } = await pool.query(
    'INSERT INTO rooms (name, created_by) VALUES ($1, $2) RETURNING id, name, created_by',
    [name, createdBy]
  );
  const r = rows[0];
  return { id: r.id, name: r.name, createdBy: r.created_by };
}

async function findByName(name) {
  const { rows } = await pool.query(
    'SELECT id, name, created_by FROM rooms WHERE name = $1',
    [name]
  );
  if (rows.length === 0) return null;
  const r = rows[0];
  return { id: r.id, name: r.name, createdBy: r.created_by };
}

async function exists(id) {
  const { rows } = await pool.query('SELECT 1 FROM rooms WHERE id = $1', [id]);
  return rows.length > 0;
}

module.exports = { listAll, create, findByName, exists };
