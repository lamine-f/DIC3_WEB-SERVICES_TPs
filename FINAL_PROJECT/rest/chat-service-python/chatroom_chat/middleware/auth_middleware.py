from functools import wraps
from flask import g, request

from ..clients.auth_client import validate_token, AuthError
from ..http.response import render


def require_auth(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        auth_header = request.headers.get("Authorization", "")
        token = auth_header[7:].strip() if auth_header.lower().startswith("bearer ") else auth_header.strip()
        try:
            info = validate_token(token)
        except AuthError as e:
            return render({"error": {"code": e.code, "message": str(e)}}, status=e.status)
        g.user = info
        return fn(*args, **kwargs)

    return wrapper
