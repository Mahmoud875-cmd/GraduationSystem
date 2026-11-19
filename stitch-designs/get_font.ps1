$ErrorActionPreference = 'Stop'
$out = 'C:\Users\KH\Desktop\graduation-system\android-app\app\src\main\res\font'
New-Item -ItemType Directory -Force -Path $out | Out-Null
# Vazirmatn static TTFs from the official CDN (jsDelivr mirrors the GitHub release files).
$map = @{
  'vazirmatn_regular.ttf'   = 'https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/fonts/ttf/Vazirmatn-Regular.ttf';
  'vazirmatn_medium.ttf'    = 'https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/fonts/ttf/Vazirmatn-Medium.ttf';
  'vazirmatn_semibold.ttf'  = 'https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/fonts/ttf/Vazirmatn-SemiBold.ttf';
  'vazirmatn_bold.ttf'      = 'https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/fonts/ttf/Vazirmatn-Bold.ttf'
}
foreach ($k in $map.Keys) {
  try {
    Invoke-WebRequest -Uri $map[$k] -OutFile (Join-Path $out $k) -UseBasicParsing
    Write-Output "OK $k"
  } catch {
    Write-Output "FAIL $k : $($_.Exception.Message)"
  }
}
Get-ChildItem $out | Select-Object Name,Length
