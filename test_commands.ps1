# Test script to verify bot responses
$token = "8538430573:AAEwnoP95nZXUYE8sqUJrAvJU5oE0Pn3zc4"
$chatId = "412345678"  # Replace with your Telegram user ID

$commands = @(
    "/start",
    "/help",
    "/ping",
    "/echo hello",
    "/ttt",
    "/board",
    "/stop"
)

Write-Host "=" * 60
Write-Host "Testing Telegram Bot Commands"
Write-Host "=" * 60

foreach ($cmd in $commands) {
    Write-Host ""
    Write-Host "Sending: $cmd"
    
    try {
        $response = Invoke-WebRequest -Uri "https://api.telegram.org/bot$token/sendMessage" `
            -Method Post `
            -Body @{
                chat_id = $chatId
                text = $cmd
            } `
            -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Sent successfully (HTTP 200)"
        } else {
            Write-Host "⚠️  Response: $($response.StatusCode)"
        }
    } catch {
        Write-Host "❌ Error: $_"
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "=" * 60
Write-Host "Check bot responses in Telegram chat"
Write-Host "=" * 60
