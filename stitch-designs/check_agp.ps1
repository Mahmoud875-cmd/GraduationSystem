$ErrorActionPreference = 'SilentlyContinue'
Write-Output '=== cached AGP versions ==='
Get-ChildItem 'C:\Users\KH\.gradle\caches\modules-2\files-2.1\com.android.tools.build\gradle' | Select-Object Name
Write-Output '=== old GraduationSystem project files ==='
$old = 'C:\Users\KH\AndroidStudioProjects\GraduationSystem'
if (Test-Path $old) {
  Get-ChildItem $old -Recurse -Include '*.gradle','*.gradle.kts','gradle-wrapper.properties','*.xml' | Select-Object FullName
} else {
  Write-Output "NOT FOUND: $old"
}
Write-Output '=== wrapper properties of old project ==='
$wp = Join-Path $old 'gradle\wrapper\gradle-wrapper.properties'
if (Test-Path $wp) { Get-Content $wp }
