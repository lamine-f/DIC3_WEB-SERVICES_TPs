"""Content negotiation pour le ChatService REST.

Lit l'en-tete Accept et serialise en JSON (defaut) ou XML.
"""
import xmltodict
from flask import Response, jsonify, request


_TYPES = ["application/json", "application/xml"]


def _best_match() -> str:
    return request.accept_mimetypes.best_match(_TYPES, default="application/json") or "application/json"


def render(payload: dict, status: int = 200) -> Response:
    if _best_match() == "application/xml":
        body = xmltodict.unparse({"response": payload}, pretty=False)
        return Response(body, status=status, mimetype="application/xml")
    resp = jsonify(payload)
    resp.status_code = status
    return resp
