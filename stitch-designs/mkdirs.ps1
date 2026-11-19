$ErrorActionPreference = 'Stop'
$p = 'C:\Users\KH\Desktop\graduation-system\android-app\app\src\main'
$dirs = @(
  'java\com\example\graduationsystem\data\api',
  'java\com\example\graduationsystem\data\model',
  'java\com\example\graduationsystem\data\repository',
  'java\com\example\graduationsystem\ui\auth',
  'java\com\example\graduationsystem\ui\student',
  'java\com\example\graduationsystem\ui\professor',
  'java\com\example\graduationsystem\ui\admin',
  'java\com\example\graduationsystem\ui\common',
  'java\com\example\graduationsystem\util',
  'res\layout',
  'res\values',
  'res\drawable',
  'res\font',
  'res\xml',
  'res\mipmap-anydpi-v26',
  'res\menu'
)
foreach ($d in $dirs) {
  New-Item -ItemType Directory -Force -Path (Join-Path $p $d) | Out-Null
}
Write-Output 'created'
Get-ChildItem $p -Recurse -Directory | Select-Object FullName
