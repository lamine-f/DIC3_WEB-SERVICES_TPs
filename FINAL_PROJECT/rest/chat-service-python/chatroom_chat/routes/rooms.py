from flask import Blueprint, g, request

from ..http.response import render
from ..middleware.auth_middleware import require_auth
from ..services import chat_service
from ..services.chat_service import ChatError


bp = Blueprint("rooms", __name__)


@bp.get("/rooms")
@require_auth
def list_rooms():
    return render({"rooms": chat_service.list_rooms()})


@bp.post("/rooms")
@require_auth
def create_room():
    body = _parse_body()
    name = (body.get("name") or "").strip()
    try:
        room = chat_service.create_room(name, g.user["userId"])
    except ChatError as e:
        return render({"error": {"code": e.code, "message": str(e)}}, status=e.status)
    return render(room, status=201)


def _parse_body() -> dict:
    if request.is_json:
        return request.get_json(silent=True) or {}
    # XML support pour le body
    ctype = (request.content_type or "").lower()
    if "xml" in ctype and request.data:
        import xmltodict
        try:
            parsed = xmltodict.parse(request.data.decode("utf-8"))
        except Exception:
            return {}
        # racine attendue : <root><name>...</name></root> ou similaire
        if isinstance(parsed, dict) and len(parsed) == 1:
            return next(iter(parsed.values())) or {}
        return parsed
    return request.form.to_dict()
