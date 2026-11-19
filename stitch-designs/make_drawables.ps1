$ErrorActionPreference = 'Stop'
$d = 'C:\Users\KH\Desktop\graduation-system\android-app\app\src\main\res\drawable'

# status chip backgrounds (pill radius 999)
function Chip($name, $color) {
@"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="$color" />
    <corners android:radius="999dp" />
</shape>
"@ | Set-Content -Path (Join-Path $d $name) -Encoding UTF8
}

Chip 'chip_pending.xml' '@color/status_pending_bg'
Chip 'chip_approved.xml' '@color/status_approved_bg'
Chip 'chip_in_progress.xml' '@color/status_in_progress_bg'
Chip 'chip_rejected.xml' '@color/status_rejected_bg'
Chip 'chip_neutral.xml' '@color/surface_container_highest'
Chip 'chip_primary_fixed.xml' '@color/primary_fixed'

# circular icon background
@"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="@color/primary_container" />
</shape>
"@ | Set-Content -Path (Join-Path $d 'bg_circle_primary_container.xml') -Encoding UTF8

# rounded linear progress track + primary progress drawable
@"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="@color/surface_container_highest" />
    <corners android:radius="999dp" />
</shape>
"@ | Set-Content -Path (Join-Path $d 'progress_track.xml') -Encoding UTF8

@"
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android">
    <shape android:shape="rectangle">
        <solid android:color="@color/primary" />
        <corners android:radius="999dp" />
    </shape>
</clip>
"@ | Set-Content -Path (Join-Path $d 'progress_fill.xml') -Encoding UTF8

Write-Output 'drawables created'
Get-ChildItem $d | Select-Object Name
