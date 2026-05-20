<?php
declare(strict_types=1);

require __DIR__ . '/../config/bootstrap.php';

use App\Client\BackendException;
use App\ClientFactory;

$error = null;
$username = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = trim((string) ($_POST['username'] ?? ''));
    $password = (string) ($_POST['password'] ?? '');
    if ($username === '' || $password === '') {
        $error = 'Veuillez saisir un nom d\'utilisateur et un mot de passe.';
    } else {
        try {
            $client = ClientFactory::make();
            $token = $client->login($username, $password);
            if ($token === '') {
                $error = 'Identifiants invalides.';
            } else {
                $_SESSION['token'] = $token;
                $_SESSION['username'] = $username;
                header('Location: chat.php');
                exit;
            }
        } catch (BackendException $e) {
            $error = $e->getMessage();
        }
    }
}
?>
<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <title>Chatroom — Connexion</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body>
  <main class="auth">
    <h1>Connexion</h1>
    <p class="mode">Backend actif : <strong><?= htmlspecialchars(BACKEND_MODE) ?></strong></p>
    <?php if ($error): ?>
      <div class="error"><?= htmlspecialchars($error) ?></div>
    <?php endif; ?>
    <form method="post">
      <label>Nom d'utilisateur
        <input type="text" name="username" value="<?= htmlspecialchars($username) ?>" required autofocus>
      </label>
      <label>Mot de passe
        <input type="password" name="password" required minlength="4">
      </label>
      <button type="submit">Se connecter</button>
    </form>
    <p>Pas de compte ? <a href="register.php">Créer un compte</a></p>
  </main>
</body>
</html>
