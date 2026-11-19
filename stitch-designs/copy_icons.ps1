$ErrorActionPreference = "Stop"
$old = "C:\Users\KH\AndroidStudioProjects\GraduationSystem\app\src\main\res"
$new = "c:\Users\KH\Desktop\graduation-system\android-app\app\src\main\res"

# Copy all mipmap density folders (launcher icons) from the working project.
Get-ChildItem $old -Directory | Where-Object { $_.Name -like "mipmap*" } | ForEach-Object {
    $destDir = Join-Path $new $_.Name
    if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
    Copy-Item (Join-Path $_.FullName "*") $destDir -Recurse -Force
    Write-Output ("copied " + $_.Name)
}

# Copy launcher-related drawables (foreground/background referenced by adaptive icon).
Get-ChildItem (Join-Path $old "drawable") -Filter *launcher* -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item $_.FullName (Join-Path $new ("drawable\" + $_.Name)) -Force
    Write-Output ("copied drawable " + $_.Name)
}

Write-Output "DONE"
