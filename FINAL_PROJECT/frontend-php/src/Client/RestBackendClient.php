<?php
declare(strict_types=1);

namespace App\Client;

/**
 * Adapter REST du port ClientInterface (Phase 2).
 * Implementation cURL : Bearer + Accept: application/json.
 */
final class RestBackendClient implements ClientInterface
{
    public function __construct(
        private readonly string $authBase,
        private readonly string $chatBase,
    ) {}

    public function register(string $username, string $password): string
    {
        $r = $this->json('POST', $this->authBase . '/register', null, [
            'username' => $username, 'password' => $password,
        ]);
        return (string) ($r['token'] ?? '');
    }

    public function login(string $username, string $password): string
    {
        $r = $this->json('POST', $this->authBase . '/login', null, [
            'username' => $username, 'password' => $password,
        ]);
        return (string) ($r['token'] ?? '');
    }

    public function logout(string $token): bool
    {
        [$status] = $this->request('POST', $this->authBase . '/logout', $token);
        return $status >= 200 && $status < 300;
    }

    public function validateToken(string $token): array
    {
        $r = $this->json('GET', $this->authBase . '/validate', $token);
        return [
            'userId' => (int) ($r['userId'] ?? 0),
            'username' => (string) ($r['username'] ?? ''),
        ];
    }

    public function listRooms(string $token): array
    {
        $r = $this->json('GET', $this->chatBase . '/rooms', $token);
        $rooms = $r['rooms'] ?? $r;
        return array_map(static fn($x) => [
            'id' => (int) $x['id'],
            'name' => (string) $x['name'],
            'createdBy' => (int) ($x['createdBy'] ?? 0),
        ], is_array($rooms) ? $rooms : []);
    }

    public function createRoom(string $token, string $name): array
    {
        $r = $this->json('POST', $this->chatBase . '/rooms', $token, ['name' => $name]);
        return [
            'id' => (int) ($r['id'] ?? 0),
            'name' => (string) ($r['name'] ?? ''),
            'createdBy' => (int) ($r['createdBy'] ?? 0),
        ];
    }

    public function sendMessage(string $token, int $roomId, string $content): int
    {
        $r = $this->json('POST', $this->chatBase . "/rooms/{$roomId}/messages", $token, [
            'content' => $content,
        ]);
        return (int) ($r['id'] ?? 0);
    }

    public function getMessages(string $token, int $roomId, int $sinceId): array
    {
        $r = $this->json('GET', $this->chatBase . "/rooms/{$roomId}/messages?sinceId={$sinceId}", $token);
        $list = $r['messages'] ?? $r;
        return array_map(static fn($m) => [
            'id' => (int) $m['id'],
            'roomId' => (int) ($m['roomId'] ?? $roomId),
            'userId' => (int) ($m['userId'] ?? 0),
            'username' => (string) ($m['username'] ?? ''),
            'content' => (string) ($m['content'] ?? ''),
            'createdAt' => (string) ($m['createdAt'] ?? ''),
        ], is_array($list) ? $list : []);
    }

    /**
     * @return array{0:int,1:string} [status, body]
     */
    private function request(string $method, string $url, ?string $token, ?array $body = null): array
    {
        $ch = curl_init($url);
        $headers = ['Accept: application/json'];
        if ($token) {
            $headers[] = 'Authorization: Bearer ' . $token;
        }
        $opts = [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_CUSTOMREQUEST => $method,
            CURLOPT_TIMEOUT => 10,
            CURLOPT_HTTPHEADER => $headers,
        ];
        if ($body !== null) {
            $headers[] = 'Content-Type: application/json';
            $opts[CURLOPT_HTTPHEADER] = $headers;
            $opts[CURLOPT_POSTFIELDS] = json_encode($body, JSON_UNESCAPED_UNICODE);
        }
        curl_setopt_array($ch, $opts);
        $resp = curl_exec($ch);
        if ($resp === false) {
            $err = curl_error($ch);
            curl_close($ch);
            throw new BackendException("Echec HTTP {$method} {$url}: {$err}");
        }
        $status = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);
        return [$status, (string) $resp];
    }

    private function json(string $method, string $url, ?string $token, ?array $body = null): array
    {
        [$status, $resp] = $this->request($method, $url, $token, $body);
        if ($status >= 400) {
            $msg = $resp !== '' ? $resp : "HTTP {$status}";
            throw new BackendException("Erreur {$status} sur {$method} {$url}: {$msg}");
        }
        if ($resp === '') {
            return [];
        }
        $data = json_decode($resp, true);
        if (!is_array($data)) {
            throw new BackendException("Reponse JSON invalide pour {$method} {$url}");
        }
        return $data;
    }
}
