# XKM Build Telegram Helper v2.0
# Real-time build progress with CPU/RAM monitoring

param(
    [string]$Action,
    [string]$BotToken,
    [string]$ChatId,
    [string]$MessageId = "",
    [string]$Title = "",
    [string]$Stage = "",
    [string]$Status = "",
    [string]$Detail = "",
    [int]$Progress = 0,
    [string]$ErrorMsg = "",
    [string]$Time = "",
    [string]$BuildType = "Debug",
    [string]$ResponseFile = ""
)

$BaseUrl = "https://botapi.arasea.dpdns.org/bot$BotToken"

function Create-ProgressBar($percent) {
    $filled = [math]::Floor($percent / 5)
    $empty = 20 - $filled
    return ('#' * $filled) + ('-' * $empty)
}

function Get-SystemStats {
    try {
        $cpu = (Get-Counter '\Processor(_Total)\% Processor Time' -ErrorAction SilentlyContinue).CounterSamples[0].CookedValue
        $cpuPercent = [math]::Round($cpu, 0)
        
        $os = Get-CimInstance Win32_OperatingSystem -ErrorAction SilentlyContinue
        $totalMem = [math]::Round($os.TotalVisibleMemorySize / 1MB, 1)
        $freeMem = [math]::Round($os.FreePhysicalMemory / 1MB, 1)
        $usedMem = $totalMem - $freeMem
        $ramPercent = [math]::Round(($usedMem / $totalMem) * 100, 0)
        
        return @{
            CPU      = $cpuPercent
            RAM      = $ramPercent
            UsedRAM  = $usedMem
            TotalRAM = $totalMem
        }
    }
    catch {
        return @{ CPU = 0; RAM = 0; UsedRAM = 0; TotalRAM = 0 }
    }
}

function Create-StatusBar($percent, $label) {
    $filled = [math]::Floor($percent / 10)
    $empty = 10 - $filled
    $bar = ('#' * $filled) + ('-' * $empty)
    return "$label [$bar] $percent%"
}

switch ($Action) {
    "start" {
        $bar = Create-ProgressBar $Progress
        $stats = Get-SystemStats
        
        $msg = "[BUILD] $Title`n"
        $msg += "`n====================`n"
        $msg += "Stage: $Stage`n"
        $msg += "[$bar] $Progress%`n"
        $msg += "Status: Starting...`n"
        $msg += "====================`n"
        $msg += "CPU: $($stats.CPU)% | RAM: $($stats.RAM)%`n"
        $msg += "Time: $Time"
        
        $body = @{
            chat_id              = $ChatId
            text                 = $msg
            disable_notification = $true
        } | ConvertTo-Json -Compress
        
        try {
            $resp = Invoke-RestMethod -Uri "$BaseUrl/sendMessage" -Method Post -Body $body -ContentType "application/json; charset=utf-8"
            if ($ResponseFile) {
                $resp.result.message_id | Out-File -FilePath $ResponseFile -Encoding UTF8 -NoNewline
            }
        }
        catch {
            Write-Host "Failed to send: $_"
        }
    }
    
    "update" {
        $bar = Create-ProgressBar $Progress
        $stats = Get-SystemStats
        
        $cpuBar = Create-StatusBar $stats.CPU "CPU"
        $ramBar = Create-StatusBar $stats.RAM "RAM"
        
        if ($Detail -and $Detail.Length -gt 50) {
            $Detail = $Detail.Substring(0, 47) + "..."
        }
        
        $detailLine = ""
        if ($Detail) { 
            $detailLine = "`n>> $Detail" 
        }
        
        $msg = "[BUILD] XKM $BuildType`n"
        $msg += "`n====================`n"
        $msg += "Stage: $Stage`n"
        $msg += "[$bar] $Progress%`n"
        $msg += "Status: $Status$detailLine`n"
        $msg += "====================`n"
        $msg += "$cpuBar`n"
        $msg += "$ramBar ($($stats.UsedRAM)/$($stats.TotalRAM) GB)`n"
        $msg += "====================`n"
        $msg += "Time: $Time"
        
        $body = @{
            chat_id    = $ChatId
            message_id = [int]$MessageId
            text       = $msg
        } | ConvertTo-Json -Compress
        
        try {
            Invoke-RestMethod -Uri "$BaseUrl/editMessageText" -Method Post -Body $body -ContentType "application/json; charset=utf-8" | Out-Null
        }
        catch {}
    }
    
    "success" {
        $stats = Get-SystemStats
        
        $msg = "[SUCCESS] XKM $BuildType Build Complete!`n"
        $msg += "`n====================`n"
        $msg += "[####################] 100%`n"
        $msg += "====================`n"
        $msg += "`nAPK ready!`n"
        $msg += "CPU: $($stats.CPU)% | RAM: $($stats.RAM)%`n"
        $msg += "Finished: $Time"
        
        $body = @{
            chat_id    = $ChatId
            message_id = [int]$MessageId
            text       = $msg
        } | ConvertTo-Json -Compress
        
        try {
            Invoke-RestMethod -Uri "$BaseUrl/editMessageText" -Method Post -Body $body -ContentType "application/json; charset=utf-8" | Out-Null
        }
        catch {}
    }
    
    "failed" {
        $msg = "[FAILED] XKM $BuildType Build`n"
        $msg += "`n====================`n"
        $msg += "Error: $ErrorMsg`n"
        $msg += "====================`n"
        $msg += "Time: $Time"
        
        $body = @{
            chat_id    = $ChatId
            message_id = [int]$MessageId
            text       = $msg
        } | ConvertTo-Json -Compress
        
        try {
            Invoke-RestMethod -Uri "$BaseUrl/editMessageText" -Method Post -Body $body -ContentType "application/json; charset=utf-8" | Out-Null
        }
        catch {}
    }
}
