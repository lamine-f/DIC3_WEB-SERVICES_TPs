<?php
declare(strict_types=1);

require __DIR__ . '/../config/bootstrap.php';

use App\Client\BackendException;
use App\ClientFactory;

if (empty($_SESSION['token'])) {
    header('Location: login.php');
    exit;
}

$client = ClientFactory::make();
$error = null;
$rooms = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'createRoom') {
    $name = trim((string) ($_POST['name'] ?? ''));
    if ($name !== '') {
        try {
            $client->createRoom($_SESSION['token'], $name);
        } catch (BackendException $e) {
            $error = $e->getMessage();
        }
    }
}

try {
    $rooms = $client->listRooms($_SESSION['token']);
} catch (BackendException $e) {
    $error = $e->getMessage();
}

$currentRoomId = isset($_GET['room']) ? (int) $_GET['room'] : ($rooms[0]['id'] ?? 0);
$currentRoom = null;
foreach ($rooms as $r) {
    if ($r['id'] === $currentRoomId) {
        $currentRoom = $r;
        break;
    }
}
?>
<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <title>Chatroom</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body class="chat-page">
  <header class="topbar">
    <h1>Chatroom</h1>
    <div class="user-info">
      <span>Connecté : <strong><?= htmlspecialchars($_SESSION['username']) ?></strong></span>
      <span class="mode-pill">Backend : <?= htmlspecialchars(BACKEND_MODE) ?></span>
      <a href="logout.php">Déconnexion</a>
    </div>
  </header>

  <?php if ($error): ?>
    <div class="error"><?= htmlspecialchars($error) ?></div>
  <?php endif; ?>

  <div class="chat-layout">
    <aside class="rooms">
      <h2>Salons</h2>
      <ul>
        <?php foreach ($rooms as $r): ?>
          <li class="<?= $r['id'] === $currentRoomId ? 'active' : '' ?>">
            <a href="?room=<?= $r['id'] ?>"><?= htmlspecialchars($r['name']) ?></a>
          </li>
        <?php endforeach; ?>
      </ul>
      <form method="post" class="new-room">
        <input type="hidden" name="action" value="createRoom">
        <input type="text" name="name" placeholder="Nouveau salon" required>
        <button type="submit">Créer</button>
      </form>
    </aside>

    <section class="messages-pane">
      <?php if ($currentRoom): ?>
        <h2><?= htmlspecialchars($currentRoom['name']) ?></h2>
        <div id="messages" data-room-id="<?= $currentRoom['id'] ?>"></div>
        <form id="send-form">
          <input type="text" id="content" placeholder="Votre message..." autocomplete="off" required>
          <button type="submit">Envoyer</button>
        </form>
      <?php else: ?>
        <p>Sélectionnez ou créez un salon.</p>
      <?php endif; ?>
    </section>
  </div>

  <?php if ($currentRoom): ?>
    <script src="assets/js/chat.js"></script>
  <?php endif; ?>
</body>
</html>
