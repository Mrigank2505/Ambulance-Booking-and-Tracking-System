$baseUrl = "http://localhost:8080/api"
$testTimestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   ROLE-BASED REGISTRATION & AUTHENTICATION TEST SUITE   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$passCount = 0
$failCount = 0

function Send-ApiRequest {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [string]$Body = $null,
        [hashtable]$Headers = @{}
    )

    $result = @{
        StatusCode = 0
        Data = $null
        RawContent = ""
        Error = $null
    }

    try {
        $req = [System.Net.HttpWebRequest]::Create($Uri)
        $req.Method = $Method
        $req.ContentType = "application/json"
        $req.Accept = "application/json"

        foreach ($key in $Headers.Keys) {
            $req.Headers.Add($key, $Headers[$key])
        }

        if ($Body -and ($Method -eq "POST" -or $Method -eq "PUT")) {
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($Body)
            $req.ContentLength = $bytes.Length
            $stream = $req.GetRequestStream()
            $stream.Write($bytes, 0, $bytes.Length)
            $stream.Close()
        }

        $res = $req.GetResponse()
        $result.StatusCode = [int]$res.StatusCode
        $reader = New-Object System.IO.StreamReader($res.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Close()
        $res.Close()

        $result.RawContent = $content
        if ($content) {
            $result.Data = $content | ConvertFrom-Json
        }
    } catch [System.Net.WebException] {
        $webEx = $_.Exception
        if ($webEx.Response) {
            $res = [System.Net.HttpWebResponse]$webEx.Response
            $result.StatusCode = [int]$res.StatusCode
            $reader = New-Object System.IO.StreamReader($res.GetResponseStream())
            $content = $reader.ReadToEnd()
            $reader.Close()
            $res.Close()
            $result.RawContent = $content
            if ($content) {
                try {
                    $result.Data = $content | ConvertFrom-Json
                } catch {}
            }
        } else {
            $result.Error = $webEx.Message
        }
    } catch {
        $result.Error = $_.Exception.Message
    }

    return $result
}

function Assert-Test($condition, $testName, $detail = "") {
    if ($condition) {
        Write-Host "  [PASS] $testName" -ForegroundColor Green
        if ($detail) { Write-Host "         $detail" -ForegroundColor Gray }
        $script:passCount++
    } else {
        Write-Host "  [FAIL] $testName" -ForegroundColor Red
        if ($detail) { Write-Host "         $detail" -ForegroundColor DarkYellow }
        $script:failCount++
    }
}

# --- 1. Security Check: Public self-registration as ADMIN without key must be rejected ---
$adminNoKey = @{
    fullName = "Hacker Attempt"
    email = "hacker$testTimestamp@test.com"
    phone = "111$($testTimestamp.ToString().Substring(4))"
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "ADMIN"
} | ConvertTo-Json

$res1 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $adminNoKey
Assert-Test ($res1.StatusCode -eq 400) "Test 1: Admin self-registration without security key rejected (HTTP 400)" "Received StatusCode: $($res1.StatusCode)"

# --- 2. Security Check: Public self-registration as ADMIN with INVALID key must be rejected ---
$adminBadKey = @{
    fullName = "Hacker Attempt 2"
    email = "hacker2_$testTimestamp@test.com"
    phone = "222$($testTimestamp.ToString().Substring(4))"
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "ADMIN"
    adminSecurityKey = "WRONG_KEY_123"
} | ConvertTo-Json

$res2 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $adminBadKey
Assert-Test ($res2.StatusCode -eq 400) "Test 2: Admin self-registration with invalid security key rejected (HTTP 400)" "Received StatusCode: $($res2.StatusCode)"

# --- 3. Authorized Admin Registration with VALID key ---
$adminEmail = "valid_admin_$testTimestamp@test.com"
$adminPhone = "333$($testTimestamp.ToString().Substring(4))"
$adminBody = @{
    fullName = "Authorized Admin"
    email = $adminEmail
    phone = $adminPhone
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "ADMIN"
    adminSecurityKey = "ADMIN_SECURE_KEY_2026"
} | ConvertTo-Json

$res3 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $adminBody
Assert-Test ($res3.StatusCode -eq 201 -and $res3.Data.data.role -eq "ADMIN") "Test 3: Authorized Admin registration succeeds with valid key (HTTP 201, Role: ADMIN)" "Registered Admin ID: $($res3.Data.data.id)"

# --- 4. Driver Registration without License Number must be rejected ---
$driverNoLic = @{
    fullName = "Driver No License"
    email = "driver_nolic_$testTimestamp@test.com"
    phone = "444$($testTimestamp.ToString().Substring(4))"
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "DRIVER"
} | ConvertTo-Json

$res4 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $driverNoLic
Assert-Test ($res4.StatusCode -eq 400) "Test 4: Driver registration without license number rejected (HTTP 400)" "Received StatusCode: $($res4.StatusCode)"

# --- 5. Valid Driver Registration with License Number ---
$driverEmail = "valid_driver_$testTimestamp@test.com"
$driverPhone = "555$($testTimestamp.ToString().Substring(4))"
$driverLicense = "DL-$testTimestamp"
$driverBody = @{
    fullName = "Emergency Pilot Mike"
    email = $driverEmail
    phone = $driverPhone
    licenseNumber = $driverLicense
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "DRIVER"
} | ConvertTo-Json

$res5 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $driverBody
Assert-Test ($res5.StatusCode -eq 201 -and $res5.Data.data.role -eq "DRIVER") "Test 5: Driver registration succeeds with license number (HTTP 201, Role: DRIVER)" "Registered Driver User ID: $($res5.Data.data.id)"

# --- 6. Driver Registration with DUPLICATE License Number must be rejected ---
$driverDupLic = @{
    fullName = "Duplicate Driver"
    email = "driver_dup_$testTimestamp@test.com"
    phone = "666$($testTimestamp.ToString().Substring(4))"
    licenseNumber = $driverLicense
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "DRIVER"
} | ConvertTo-Json

$res6 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $driverDupLic
Assert-Test ($res6.StatusCode -eq 400) "Test 6: Duplicate driver licence number rejected (HTTP 400)" "Received StatusCode: $($res6.StatusCode)"

# --- 7. Valid User Registration ---
$userEmail = "citizen_$testTimestamp@test.com"
$userPhone = "777$($testTimestamp.ToString().Substring(4))"
$userBody = @{
    fullName = "Citizen Jane"
    email = $userEmail
    phone = $userPhone
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "USER"
    address = "44 Park Avenue"
} | ConvertTo-Json

$res7 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $userBody
Assert-Test ($res7.StatusCode -eq 201 -and $res7.Data.data.role -eq "USER") "Test 7: User registration succeeds (HTTP 201, Role: USER)" "Registered User ID: $($res7.Data.data.id)"

# --- 8. Duplicate Email Registration must be rejected ---
$dupUser = @{
    fullName = "Duplicate Jane"
    email = $userEmail
    phone = "888$($testTimestamp.ToString().Substring(4))"
    password = "Password@123"
    confirmPassword = "Password@123"
    role = "USER"
} | ConvertTo-Json

$res8 = Send-ApiRequest -Uri "$baseUrl/auth/register" -Method "POST" -Body $dupUser
Assert-Test ($res8.StatusCode -eq 400) "Test 8: Duplicate email registration rejected (HTTP 400)" "Received StatusCode: $($res8.StatusCode)"

# --- 9. Incorrect Credentials Login must be rejected ---
$badLogin = @{
    email = $userEmail
    password = "WrongPassword999"
} | ConvertTo-Json

$res9 = Send-ApiRequest -Uri "$baseUrl/auth/login" -Method "POST" -Body $badLogin
Assert-Test ($res9.StatusCode -eq 401) "Test 9: Incorrect credentials rejected (HTTP 401 Unauthorized)" "Received StatusCode: $($res9.StatusCode)"

# --- 10. User Login Synchronization & Token Verification ---
$loginUserBody = @{
    email = $userEmail
    password = "Password@123"
} | ConvertTo-Json
$res10 = Send-ApiRequest -Uri "$baseUrl/auth/login" -Method "POST" -Body $loginUserBody
$userToken = $res10.Data.data.token
Assert-Test ($res10.StatusCode -eq 200 -and $res10.Data.data.user.role -eq "USER" -and $userToken) "Test 10: Registered User login succeeds, verifies BCrypt, returns USER role & JWT" "Logged in: $($res10.Data.data.user.fullName)"

# --- 11. Driver Login Synchronization & Profile Retrieval ---
$loginDriverBody = @{
    email = $driverEmail
    password = "Password@123"
} | ConvertTo-Json
$res11 = Send-ApiRequest -Uri "$baseUrl/auth/login" -Method "POST" -Body $loginDriverBody
$driverToken = $res11.Data.data.token
Assert-Test ($res11.StatusCode -eq 200 -and $res11.Data.data.user.role -eq "DRIVER" -and $driverToken) "Test 11: Registered Driver login succeeds, verifies BCrypt, returns DRIVER role & JWT" "Logged in: $($res11.Data.data.user.fullName)"

# --- 12. Admin Login Synchronization ---
$loginAdminBody = @{
    email = $adminEmail
    password = "Password@123"
} | ConvertTo-Json
$res12 = Send-ApiRequest -Uri "$baseUrl/auth/login" -Method "POST" -Body $loginAdminBody
$adminToken = $res12.Data.data.token
Assert-Test ($res12.StatusCode -eq 200 -and $res12.Data.data.user.role -eq "ADMIN" -and $adminToken) "Test 12: Authorized Admin login succeeds, verifies BCrypt, returns ADMIN role & JWT" "Logged in: $($res12.Data.data.user.fullName)"

# --- 13. Security Barrier: Driver cannot access Admin-only features ---
$driverHeaders = @{ Authorization = "Bearer $driverToken" }
$res13 = Send-ApiRequest -Uri "$baseUrl/admin/dashboard" -Method "GET" -Headers $driverHeaders
Assert-Test ($res13.StatusCode -eq 403) "Test 13: Driver blocked from accessing Admin-only API (HTTP 403 Forbidden)" "Received StatusCode: $($res13.StatusCode)"

# --- 14. Security Barrier: User cannot access Driver-only features ---
$userHeaders = @{ Authorization = "Bearer $userToken" }
$res14 = Send-ApiRequest -Uri "$baseUrl/driver/me" -Method "GET" -Headers $userHeaders
Assert-Test ($res14.StatusCode -eq 403) "Test 14: User blocked from accessing Driver-only API (HTTP 403 Forbidden)" "Received StatusCode: $($res14.StatusCode)"

# --- 15. Driver Access to Driver-only features ---
$res15 = Send-ApiRequest -Uri "$baseUrl/driver/me" -Method "GET" -Headers $driverHeaders
Assert-Test ($res15.StatusCode -eq 200 -and $res15.Data.data.licenseNumber -eq $driverLicense) "Test 15: Driver successfully accesses Driver profile with correct license in MySQL" "License in DB: $($res15.Data.data.licenseNumber)"

# --- 16. Admin Access to Admin Dashboard ---
$adminHeaders = @{ Authorization = "Bearer $adminToken" }
$res16 = Send-ApiRequest -Uri "$baseUrl/admin/dashboard" -Method "GET" -Headers $adminHeaders
Assert-Test ($res16.StatusCode -eq 200 -and $res16.Data.data.totalUsers -gt 0) "Test 16: Admin successfully accesses Admin Command Center KPIs" "Total Users in DB: $($res16.Data.data.totalUsers)"

# --- 17. Existing Platform Functionality: Nearby Hospitals (10km filter) ---
$res17 = Send-ApiRequest -Uri "$baseUrl/hospitals/nearby?latitude=12.9716&longitude=77.5946&radiusKm=10" -Method "GET"
Assert-Test ($res17.StatusCode -eq 200 -and $res17.Data.data.Count -gt 0) "Test 17: Existing 10km nearby hospitals functionality operational" "Found $($res17.Data.data.Count) hospitals within 10 km"

# --- 18. Existing Platform Functionality: Ambulance Fleet ---
$res18 = Send-ApiRequest -Uri "$baseUrl/ambulances" -Method "GET"
Assert-Test ($res18.StatusCode -eq 200 -and $res18.Data.data.Count -gt 0) "Test 18: Existing ambulance fleet directory operational" "Found $($res18.Data.data.Count) ambulances"

$summaryColor = "Green"
if ($failCount -gt 0) { $summaryColor = "Red" }
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "TOTAL TESTS: $($passCount + $failCount) | PASSED: $passCount | FAILED: $failCount" -ForegroundColor $summaryColor
Write-Host "==========================================================" -ForegroundColor Cyan
