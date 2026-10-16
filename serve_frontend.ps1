# Lightweight PowerShell Static File Server
param([int]$Port = 5500)

$candidatePorts = @($Port, 5555, 5556, 3000, 8000)
$listener = $null
$prefix = $null

foreach ($p in $candidatePorts) {
    try {
        $candidatePrefix = "http://localhost:$p/"
        $testListener = New-Object System.Net.HttpListener
        $testListener.Prefixes.Add($candidatePrefix)
        $testListener.Start()
        $listener = $testListener
        $prefix = $candidatePrefix
        $Port = $p
        Write-Host "Frontend server listening at $prefix"
        break
    } catch {
        # Try next candidate port
    }
}

if (-not $listener) {
    Write-Host "Failed to start HTTP listener on candidate ports."
    exit 1
}

$root = "$PSScriptRoot\frontend"
if (-not (Test-Path $root)) {
    $root = "$PSScriptRoot"
}

$mimeTypes = @{
    ".html" = "text/html; charset=utf-8"
    ".css"  = "text/css; charset=utf-8"
    ".js"   = "application/javascript; charset=utf-8"
    ".json" = "application/json; charset=utf-8"
    ".png"  = "image/png"
    ".jpg"  = "image/jpeg"
    ".svg"  = "image/svg+xml"
    ".ico"  = "image/x-icon"
}

while ($listener.IsListening) {
    try {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response

        $rawPath = $request.Url.LocalPath.TrimStart('/')
        if ([string]::IsNullOrWhiteSpace($rawPath) -or $rawPath -eq "") {
            $rawPath = "login.html"
        }

        $filePath = Join-Path $root $rawPath

        if (Test-Path $filePath -PathType Leaf) {
            $ext = [System.IO.Path]::GetExtension($filePath).ToLower()
            $mime = if ($mimeTypes.ContainsKey($ext)) { $mimeTypes[$ext] } else { "application/octet-stream" }
            $response.ContentType = $mime

            $bytes = [System.IO.File]::ReadAllBytes($filePath)
            $response.ContentLength64 = $bytes.Length
            $response.OutputStream.Write($bytes, 0, $bytes.Length)
        } else {
            $response.StatusCode = 404
            $errBytes = [System.Text.Encoding]::UTF8.GetBytes("404 Not Found: $rawPath")
            $response.OutputStream.Write($errBytes, 0, $errBytes.Length)
        }
        $response.Close()
    } catch {
        # Catch and continue
    }
}
