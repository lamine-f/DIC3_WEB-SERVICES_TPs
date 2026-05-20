const auth = require('./authClient');
const rooms = require('./repositories/rooms');
const messages = require('./repositories/messages');

function fault(code, message) {
  const err = new Error(message);
  err.Fault = {
    Code: { Value: 'soap:Sender', Subcode: { value: code } },
    Reason: { Text: message },
    statusCode: 500,
  };
  return err;
}

async function authenticate(token) {
  try {
    return await auth.validateToken(token);
  } catch (e) {
    throw fault('UNAUTHENTICATED', 'token invalide ou service auth indisponible: ' + e.message);
  }
}

async function createRoom(args) {
  const who = await authenticate(args.token);
  if (!args.name || args.name.trim() === '') {
    throw fault('INVALID_INPUT', 'nom du salon requis');
  }
  const existing = await rooms.findByName(args.name);
  if (existing) throw fault('ROOM_EXISTS', 'salon deja existant');
  const room = await rooms.create(args.name, who.userId);
  return { room };
}

async function listRooms(args) {
  await authenticate(args.token);
  const list = await rooms.listAll();
  return { room: list };
}

async function sendMessage(args) {
  const who = await authenticate(args.token);
  if (!await rooms.exists(args.roomId)) {
    throw fault('ROOM_NOT_FOUND', 'salon introuvable');
  }
  if (!args.content || args.content.trim() === '') {
    throw fault('INVALID_INPUT', 'contenu vide');
  }
  const id = await messages.insert(args.roomId, who.userId, who.username, args.content);
  return { messageId: id };
}

async function getMessages(args) {
  await authenticate(args.token);
  const since = Number(args.sinceId || 0);
  const list = await messages.listSince(Number(args.roomId), since);
  return { message: list };
}

module.exports = {
  ChatService: {
    ChatServicePort: {
      createRoom,
      listRooms,
      sendMessage,
      getMessages,
    },
  },
};
