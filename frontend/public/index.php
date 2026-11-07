<?php
declare(strict_types=1);

set_exception_handler(static function (Throwable $exception): void {
    error_log('SOA frontend proxy exception: ' . $exception);
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    $message = ($_GET['debug'] ?? '') === '1'
        ? get_class($exception) . ': ' . $exception->getMessage()
        : 'PHP proxy exception; check the PHP error log.';
    echo json_encode(['message' => $message], JSON_UNESCAPED_UNICODE);
});

register_shutdown_function(static function (): void {
    $error = error_get_last();
    if ($error !== null && in_array($error['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR], true)) {
        error_log('SOA frontend proxy fatal: ' . $error['message'] . ' in ' . $error['file'] . ':' . $error['line']);
    }
});

function sendJsonError(int $status, string $message): void
{
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['message' => $message], JSON_UNESCAPED_UNICODE);
    exit;
}

function proxyWithStreams(string $url, string $method, array $headers, string $caFile): void
{
    $allowUrlFopen = strtolower((string) ini_get('allow_url_fopen'));
    if ($allowUrlFopen === '' || $allowUrlFopen === '0' || $allowUrlFopen === 'off' || $allowUrlFopen === 'false') {
        sendJsonError(500, 'На PHP-сервере отключены HTTPS streams (allow_url_fopen).');
    }

    $ssl = [
        'verify_peer' => true,
        'verify_peer_name' => true,
        'allow_self_signed' => false,
    ];
    if ($caFile !== '') {
        if (!is_file($caFile) || !is_readable($caFile)) {
            sendJsonError(500, 'CA-файл ' . $caFile . ' недоступен для чтения.');
        }
        $ssl['cafile'] = $caFile;
    }

    $httpOptions = [
        'method' => $method,
        'header' => implode("\r\n", $headers),
        'timeout' => 10,
        'ignore_errors' => true,
        'follow_location' => 0,
    ];
    if (in_array($method, ['POST', 'PATCH'], true)) {
        $httpOptions['content'] = file_get_contents('php://input') ?: '';
    }

    $options = [
        'http' => $httpOptions,
        'ssl' => $ssl,
    ];

    $responseBody = @file_get_contents($url, false, stream_context_create($options));
    $responseHeaders = $http_response_header ?? [];
    if ($responseBody === false) {
        $lastError = error_get_last();
        sendJsonError(502, 'Не удалось обратиться к WildFly на Helios: '
            . ($lastError['message'] ?? 'ошибка HTTPS-соединения'));
    }

    $status = 502;
    foreach ($responseHeaders as $responseHeader) {
        if (preg_match('~^HTTP/\S+\s+(\d{3})~', $responseHeader, $matches)) {
            $status = (int) $matches[1];
        } elseif (stripos($responseHeader, 'Content-Type:') === 0) {
            header('Content-Type: ' . trim(substr($responseHeader, strlen('Content-Type:'))));
        }
    }

    http_response_code($status);
    echo $responseBody;
    exit;
}

function proxyRequest(string $url, string $forwardQuery, string $caFile): void
{
    $method = strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');
    $methodOverride = strtoupper((string) ($_GET['_method'] ?? ''));
    if ($method === 'POST' && in_array($methodOverride, ['PATCH', 'DELETE'], true)) {
        $method = $methodOverride;
    }

    if (!in_array($method, ['GET', 'POST', 'PATCH', 'DELETE'], true)) {
        header('Allow: GET, POST, PATCH, DELETE');
        sendJsonError(405, 'Метод не поддерживается.');
    }

    if ($forwardQuery !== '') {
        $url .= '?' . $forwardQuery;
    }

    $headers = ['Accept: ' . ($_SERVER['HTTP_ACCEPT'] ?? 'application/json')];
    $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
    if ($contentType !== '') {
        $headers[] = 'Content-Type: ' . $contentType;
    }
    proxyWithStreams($url, $method, $headers, $caFile);
}

$proxyService = $_GET['proxy'] ?? null;
if ($proxyService !== null) {
    $configPath = __DIR__ . '/proxy-config.php';
    if (!is_file($configPath)) {
        sendJsonError(500, 'Не найден proxy-config.php. Скопируй proxy-config.php.example и настрой его.');
    }
    define('FRONT_PROXY_BOOTSTRAP', true);
    $proxyConfig = require $configPath;

    $apiPath = $_GET['path'] ?? '';
    if (!is_string($apiPath) || $apiPath === '' || $apiPath[0] !== '/') {
        sendJsonError(400, 'Некорректный API path.');
    }

    $forwardQueryParts = [];
    foreach (explode('&', $_SERVER['QUERY_STRING'] ?? '') as $queryPart) {
        $key = urldecode(explode('=', $queryPart, 2)[0]);
        if ($key !== 'proxy' && $key !== 'path' && $key !== 'debug' && $key !== '_method' && $queryPart !== '') {
            $forwardQueryParts[] = $queryPart;
        }
    }
    $forwardQuery = implode('&', $forwardQueryParts);
    $firstServiceUrl = getenv('FIRST_SERVICE_URL') ?: $proxyConfig['first_service_url'];
    $secondServiceUrl = getenv('SECOND_SERVICE_URL') ?: $proxyConfig['second_service_url'];

    $isCityCollection = $apiPath === '/cities';
    $isCityById = preg_match('~^/cities/[1-9][0-9]*$~D', $apiPath) === 1;
    $isCitySearch = preg_match('~^/cities/(name|area|standard-of-living)/[^/]+$~D', $apiPath) === 1;
    if ($isCityCollection || $isCityById || $isCitySearch) {
        $citySuffix = substr($apiPath, strlen('/cities'));
        proxyRequest(rtrim($firstServiceUrl, '/') . $citySuffix, $forwardQuery, $proxyConfig['first_service_ca_file']);
    }
    if ($apiPath === '/route/calculate/to-largest' || $apiPath === '/route/calculate/to-newest') {
        proxyRequest(rtrim($secondServiceUrl, '/') . $apiPath, $forwardQuery, $proxyConfig['second_service_ca_file']);
    }

    sendJsonError(404, 'API endpoint не найден.');
}

header('Content-Type: text/html; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
readfile(__DIR__ . '/index.html');
