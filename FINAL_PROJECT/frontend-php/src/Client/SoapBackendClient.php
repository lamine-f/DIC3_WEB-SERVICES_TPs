<?php
declare(strict_types=1);

namespace App\Client;

use SoapClient;
use SoapFault;

/**
 * Adapter SOAP du port ClientInterface (Phase 1).
 * Wrap deux SoapClient (un par service) et normalise les retours.
 */
final class SoapBackendClient implements ClientInterface
{
    private SoapClient $auth;
    private SoapClient $chat;

    public function __construct(string $authWsdl, string $chatWsdl)
    {
        $opts = [
            'trace' => 1,
            'exceptions' => true,
            'cache_wsdl' => WSDL_CACHE_NONE,
            'connection_timeout' => 5,
        ];
        try {
            $this->auth = new SoapClient($authWsdl, $opts);
            $this->chat = new SoapClient($chatWsdl, $opts);
        } catch (SoapFault $e) {
            throw new BackendException('Impossible de charger les WSDL SOAP : ' . $e->getMessage());
        }
    }

    public function register(string $username, string $password): string
    {
        return $this->callAuth('register', ['username' => $username, 'password' => $password]);
    }

    public function login(string $username, string $password): string
    {
        return $this->callAuth('login', ['username' => $username, 'password' => $password]);
    }

    public function logout(string $token): bool
    {
        try {
            $r = $this->auth->logout(['token' => $token]);
            return (bool) ($r->return ?? $r);
        } catch (SoapFault $e) {
            return false;
        }
    }

    public function validateToken(string $token): array
    {
        try {
            $r = $this->auth->validateToken(['token' => $token]);
            $info = $r->return ?? $r;
            return [
                'userId' => (int) ($info->userId ?? 0),
                'username' => (string) ($info->username ?? ''),
            ];
        } catch (SoapFault $e) {
            throw new BackendException('Token invalide : ' . $e->getMessage());
        }
    }

    public function listRooms(string $token): array
    {
        try {
            $r = $this->chat->listRooms(['token' => $token]);
            return self::normalizeRooms($r->room ?? []);
        } catch (SoapFault $e) {
            throw new BackendException('listRooms: ' . $e->getMessage());
        }
    }

    public function createRoom(string $token, string $name): array
    {
        try {
            $r = $this->chat->createRoom(['token' => $token, 'name' => $name]);
            $room = $r->room ?? $r;
            return [
                'id' => (int) $room->id,
                'name' => (string) $room->name,
                'createdBy' => (int) ($room->createdBy ?? 0),
            ];
        } catch (SoapFault $e) {
            throw new BackendException('createRoom: ' . $e->getMessage());
        }
    }

    public function sendMessage(string $token, int $roomId, string $content): int
    {
        try {
            $r = $this->chat->sendMessage([
                'token' => $token,
                'roomId' => $roomId,
                'content' => $content,
            ]);
            return (int) ($r->messageId ?? 0);
        } catch (SoapFault $e) {
            throw new BackendException('sendMessage: ' . $e->getMessage());
        }
    }

    public function getMessages(string $token, int $roomId, int $sinceId): array
    {
        try {
            $r = $this->chat->getMessages([
                'token' => $token,
                'roomId' => $roomId,
                'sinceId' => $sinceId,
            ]);
            return self::normalizeMessages($r->message ?? []);
        } catch (SoapFault $e) {
            throw new BackendException('getMessages: ' . $e->getMessage());
        }
    }

    private function callAuth(string $method, array $args): string
    {
        try {
            $r = $this->auth->{$method}($args);
            return (string) ($r->return ?? $r);
        } catch (SoapFault $e) {
            throw new BackendException("{$method}: " . $e->getMessage());
        }
    }

    private static function normalizeRooms(mixed $raw): array
    {
        $items = self::toArray($raw);
        return array_map(static fn($r) => [
            'id' => (int) $r->id,
            'name' => (string) $r->name,
            'createdBy' => (int) ($r->createdBy ?? 0),
        ], $items);
    }

    private static function normalizeMessages(mixed $raw): array
    {
        $items = self::toArray($raw);
        return array_map(static fn($m) => [
            'id' => (int) $m->id,
            'roomId' => (int) $m->roomId,
            'userId' => (int) $m->userId,
            'username' => (string) $m->username,
            'content' => (string) $m->content,
            'createdAt' => (string) ($m->createdAt ?? ''),
        ], $items);
    }

    private static function toArray(mixed $x): array
    {
        if ($x === null) {
            return [];
        }
        if (is_array($x)) {
            return $x;
        }
        return [$x];
    }
}
