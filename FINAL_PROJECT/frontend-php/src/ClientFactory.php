<?php
declare(strict_types=1);

namespace App;

use App\Client\ClientInterface;
use App\Client\RestBackendClient;
use App\Client\SoapBackendClient;

final class ClientFactory
{
    public static function make(): ClientInterface
    {
        return match (BACKEND_MODE) {
            'soap' => new SoapBackendClient(SOAP_AUTH_WSDL, SOAP_CHAT_WSDL),
            'rest' => new RestBackendClient(REST_AUTH_BASE, REST_CHAT_BASE),
            default => throw new \RuntimeException('BACKEND_MODE invalide : ' . BACKEND_MODE),
        };
    }
}
