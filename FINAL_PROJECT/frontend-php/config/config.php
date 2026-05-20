<?php
declare(strict_types=1);

define('BACKEND_MODE', getenv('BACKEND_MODE') ?: 'soap');

define('SOAP_AUTH_WSDL', getenv('SOAP_AUTH_WSDL') ?: 'http://localhost:9001/auth?wsdl');
define('SOAP_CHAT_WSDL', getenv('SOAP_CHAT_WSDL') ?: 'http://localhost:9002/chat?wsdl');

define('REST_AUTH_BASE', getenv('REST_AUTH_BASE') ?: 'http://localhost:8081');
define('REST_CHAT_BASE', getenv('REST_CHAT_BASE') ?: 'http://localhost:8082');
