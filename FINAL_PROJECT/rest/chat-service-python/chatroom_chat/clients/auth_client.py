import requests

from config import Config


class AuthError(Exception):
    def __init__(self, status: int, code: str, message: str):
        super().__init__(message)
        self.status = status
        self.code = code


def validate_token(token: str) -> dict:
    """Appelle GET /validate sur AuthService Java JAX-RS.

    Retourne {"userId": int, "username": str} ou leve AuthError.
    """
    if not token:
        raise AuthError(401, "MISSING_TOKEN", "token absent")
    url = f"{Config.AUTH_BASE_URL}/validate"
    try:
        resp = requests.get(
            url,
            headers={
                "Authorization": f"Bearer {token}",
                "Accept": "application/json",
            },
            timeout=5,
        )
    except requests.RequestException as e:
        raise AuthError(502, "AUTH_UNREACHABLE", f"auth service injoignable: {e}") from e

    if resp.status_code == 401:
        raise AuthError(401, "INVALID_TOKEN", "token invalide ou expire")
    if resp.status_code >= 400:
        raise AuthError(502, "AUTH_ERROR", f"auth service: HTTP {resp.status_code}")

    data = resp.json()
    return {
        "userId": int(data.get("userId", 0)),
        "username": str(data.get("username", "")),
    }
