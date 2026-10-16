$body = '{"email":"driver.mike@ambulance.com","password":"Password@123"}'
$res = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Body $body -ContentType 'application/json'
Write-Host "Auth success:" $res.success
$token = $res.data.token
$headers = @{ Authorization = "Bearer $token" }

Write-Host "--- Test /api/driver/me ---"
$me = Invoke-RestMethod -Uri 'http://localhost:8080/api/driver/me' -Headers $headers
Write-Host "Driver FullName:" $me.data.fullName
Write-Host "License Number:" $me.data.licenseNumber
Write-Host "Ambulance Number:" $me.data.assignedAmbulanceNumber
Write-Host "Availability Status:" $me.data.availabilityStatus

Write-Host "--- Test /api/driver/bookings/pending ---"
$pending = Invoke-RestMethod -Uri 'http://localhost:8080/api/driver/bookings/pending' -Headers $headers
Write-Host "Pending count:" $pending.data.Count

Write-Host "--- Test /api/driver/bookings/active ---"
$active = Invoke-RestMethod -Uri 'http://localhost:8080/api/driver/bookings/active' -Headers $headers
Write-Host "Active count:" $active.data.Count

Write-Host "--- Test /api/bookings ---"
$bookings = Invoke-RestMethod -Uri 'http://localhost:8080/api/bookings' -Headers $headers
Write-Host "My bookings count:" $bookings.data.Count
