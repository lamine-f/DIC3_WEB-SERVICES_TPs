from flask import Blueprint, g, request

from ..http.response import render
from ..middleware.auth_middleware import require_auth
from ..services import chat_service
from ..services.chat_service import ChatError
from .rooms import _parse_body


bp = Blueprint("messages", __name__)


@bp.get("/rooms/<int:room_id>/messages")
@require_auth
def get_messages(room_id: int):
    since_id = int(request.args.get("sinceId", 0))
    msgs = chat_service.get_messages(room_id, since_id)
    return render({"messages": msgs})


@bp.post("/rooms/<int:room_id>/messages")
@require_auth
def send_message(room_id: int):
    body = _parse_body()
    content = (body.get("content") or "").strip()
    try:
        msg_id = chat_service.send_message(
            room_id, g.user["userId"], g.user["username"], content
        )
    except ChatError as e:
        return render({"error": {"code": e.code, "message": str(e)}}, status=e.status)
    return render({"id": msg_id}, status=201)
