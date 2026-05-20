from flask import Flask

from .routes.rooms import bp as rooms_bp
from .routes.messages import bp as messages_bp


def create_app() -> Flask:
    app = Flask(__name__)

    @app.errorhandler(404)
    def not_found(_e):
        from .http.response import render
        return render({"error": "Not Found"}, status=404)

    app.register_blueprint(rooms_bp)
    app.register_blueprint(messages_bp)

    return app
