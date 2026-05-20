<?php
declare(strict_types=1);

require __DIR__ . '/../config/bootstrap.php';

if (!empty($_SESSION['token'])) {
    header('Location: chat.php');
    exit;
}
header('Location: login.php');
