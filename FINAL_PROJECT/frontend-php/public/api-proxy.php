<?php
declare(strict_types=1);

require __DIR__ . '/../config/bootstrap.php';

use App\Client\BackendException;
use App\ClientFactory;

header('Content-Type: application/json');

if (empty($_SESSION['token'])) {
    http_response_code(401);
    echo json_encode(['error' => 'Non authentifie']);
    exit;
}

$action = $_REQUEST['action'] ?? '';
$token = $_SESSION['token'];

try {
    $client = ClientFactory::make();

    switch ($action) {
        case 'getMessages':
            $roomId = (int) ($_GET['roomId'] ?? 0);
            $sinceId = (int) ($_GET['sinceId'] ?? 0);
            $messages = $client->getMessages($token, $roomId, $sinceId);
            echo json_encode(['messages' => $messages]);
            break;

        case 'sendMessage':
            $roomId = (int) ($_POST['roomId'] ?? 0);
            $content = trim((string) ($_POST['content'] ?? ''));
            if ($content === '') {
                http_response_code(400);
                echo json_encode(['error' => 'Contenu vide']);
                break;
            }
            $id = $client->sendMessage($token, $roomId, $content);
            echo json_encode(['id' => $id]);
            break;

        default:
            http_response_code(400);
            echo json_encode(['error' => 'Action inconnue']);
    }
} catch (BackendException $e) {
    http_response_code(502);
    echo json_encode(['error' => $e->getMessage()]);
}
