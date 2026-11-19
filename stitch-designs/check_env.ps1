$ErrorActionPreference = 'SilentlyContinue'
Write-Output '=== Android Studio install dir ==='
Get-ChildItem 'C:\Program Files\Android\Android Studio' | Select-Object Name
Write-Output '=== Android SDK (LOCALAPPDATA) ==='
$sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
Write-Output "SDK path: $sdk"
Get-ChildItem $sdk | Select-Object Name
Write-Output '=== Installed platforms ==='
Get-ChildItem (Join-Path $sdk 'platforms') | Select-Object Name
Write-Output '=== Build-tools ==='
Get-ChildItem (Join-Path $sdk 'build-tools') | Select-Object Name
Write-Output '=== JAVA_HOME ==='
Write-Output $env:JAVA_HOME
Write-Output '=== ANDROID_HOME ==='
Write-Output $env:ANDROID_HOME
Write-Output '=== java version ==='
java -version 2>&1
