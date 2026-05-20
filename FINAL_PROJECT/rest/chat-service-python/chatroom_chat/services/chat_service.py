from ..repositories import room_repository, message_repository


class ChatError(Exception):
    def __init__(self, status: int, code: str, message: str):
        super().__init__(message)
        self.status = status
        self.code = code


def list_rooms() -> list[dict]:
    return room_repository.list_all()


def create_room(name: str, created_by: int) -> dict:
    if not name or not name.strip():
        raise ChatError(400, "INVALID_INPUT", "nom du salon requis")
    if room_repository.find_by_name(name):
        raise ChatError(409, "ROOM_EXISTS", "salon deja existant")
    return room_repository.create(name.strip(), created_by)


def send_message(room_id: int, user_id: int, username: str, content: str) -> int:
    if not room_repository.exists(room_id):
        raise ChatError(404, "ROOM_NOT_FOUND", "salon introuvable")
    if not content or not content.strip():
        raise ChatError(400, "INVALID_INPUT", "contenu vide")
    return message_repository.insert(room_id, user_id, username, content)


def get_messages(room_id: int, since_id: int) -> list[dict]:
    return message_repository.list_since(room_id, since_id)
