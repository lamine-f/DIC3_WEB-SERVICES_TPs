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
    $passwordConfirm = (string) ($_POST['password_confirm'] ?? '');
    if ($username === '' || strlen($password) < 4) {
        $error = 'Nom requis et mot de passe d\'au moins 4 caractères.';
    } elseif ($password !== $passwordConfirm) {
        $error = 'Les mots de passe ne correspondent pas.';
    } else {
        try {
            $client = ClientFactory::make();
            $token = $client->register($username, $password);
            if ($token === '') {
                $error = 'Impossible de créer le compte.';
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
  <title>Chatroom — Inscription</title>
  <link rel="stylesheet" href="assets/css/style.css">
</head>
<body>
  <main class="auth">
    <h1>Inscription</h1>
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
      <label>Confirmer le mot de passe
        <input type="password" name="password_confirm" required minlength="4">
      </label>
      <button type="submit">Créer un compte</button>
    </form>
    <p>Déjà inscrit ? <a href="login.php">Se connecter</a></p>
  </main>
</body>
</html>
