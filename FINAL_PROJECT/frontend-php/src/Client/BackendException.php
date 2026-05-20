<?php
declare(strict_types=1);

namespace App\Client;

class BackendException extends \RuntimeException
{
    public function __construct(string $message, public readonly ?string $errorCode = null)
    {
        parent::__construct($message);
    }
}
