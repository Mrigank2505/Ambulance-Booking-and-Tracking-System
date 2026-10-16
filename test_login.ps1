param(
    [string]$identifier = "admin",
    [string]$password = "Password@123"
)

$payload = @{
    email = $identifier
    password = $password
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $payload

    Write-Output "SUCCESS: Logged in as $($response.data.user.fullName) (Role: $($response.data.user.role))"
    Write-Output "Token: $($response.data.token.Substring(0, 20))..."
} catch {
    Write-Output "FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Output "Response: $($reader.ReadToEnd())"
    }
}
