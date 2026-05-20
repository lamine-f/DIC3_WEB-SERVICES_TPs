<?php
declare(strict_types=1);

require __DIR__ . '/../config/bootstrap.php';

use App\ClientFactory;

if (!empty($_SESSION['token'])) {
    try {
        ClientFactory::make()->logout($_SESSION['token']);
    } catch (\Throwable $e) {
        // ignore, on detruit la session de toute facon
    }
}
$_SESSION = [];
session_destroy();
header('Location: login.php');
