$port = new-Object System.IO.Ports.SerialPort COM3,115200,None,8,one
$port.Open()
$startTime = Get-Date
$endTime = $startTime.AddSeconds(95)
Write-Host "Starting high-speed capture from COM3 (115,200 bps) for 90 seconds..."
while ((Get-Date) -lt $endTime) {
    if ($port.BytesToRead -gt 0) {
        $line = $port.ReadLine()
        if ($line -like '$*') {
            $line.Trim() | Out-File -Append -FilePath "src/main/resources/simulation/vfan_115k_90s.nmea" -Encoding ascii
        }
    }
    Start-Sleep -Milliseconds 10
}
$port.Close()
Write-Host "Capture complete."
