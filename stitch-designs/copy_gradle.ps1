$ErrorActionPreference = 'Stop'
$src = 'C:\Users\KH\AndroidStudioProjects\GraduationSystem'
$dst = 'C:\Users\KH\Desktop\graduation-system\android-app'

New-Item -ItemType Directory -Force -Path $dst | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $dst 'gradle\wrapper') | Out-Null

Copy-Item (Join-Path $src 'settings.gradle.kts') $dst -Force
Copy-Item (Join-Path $src 'build.gradle.kts') $dst -Force
Copy-Item (Join-Path $src 'gradle.properties') $dst -Force
Copy-Item (Join-Path $src 'gradle\libs.versions.toml') (Join-Path $dst 'gradle') -Force
Copy-Item (Join-Path $src 'gradle\wrapper\gradle-wrapper.properties') (Join-Path $dst 'gradle\wrapper') -Force
if (Test-Path (Join-Path $src 'gradle\wrapper\gradle-wrapper.jar')) {
  Copy-Item (Join-Path $src 'gradle\wrapper\gradle-wrapper.jar') (Join-Path $dst 'gradle\wrapper') -Force
}
Get-ChildItem $src -Filter 'gradlew*' | ForEach-Object { Copy-Item $_.FullName $dst -Force }

Write-Output '=== android-app tree ==='
Get-ChildItem $dst -Recurse | Select-Object FullName
