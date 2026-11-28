<#
.SYNOPSIS
  Sao lưu DB (pg_dump) và thư mục ảnh upload của stack Docker Compose.
.DESCRIPTION
  Cần dịch vụ `db` đang chạy. Kết quả nằm ở <OutDir>\<yyyyMMdd-HHmmss>\ gồm:
    db.dump    - pg_dump định dạng custom (khôi phục bằng scripts\restore.ps1)
    media.tgz  - toàn bộ thư mục ảnh (volume media_data)
  Thư mục backups\ bị git bỏ qua vì chứa dữ liệu thật.
.PARAMETER OutDir
  Thư mục gốc chứa các bản sao lưu (mặc định: backups ở thư mục dự án).
.PARAMETER Keep
  Giữ lại tối đa N bản gần nhất, xóa bản cũ hơn (mặc định 14; 0 = không xóa).
.EXAMPLE
  .\scripts\backup.ps1
#>
[CmdletBinding()]
param(
    [string]$OutDir = (Join-Path (Split-Path $PSScriptRoot -Parent) 'backups'),
    [int]$Keep = 14
)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

function Invoke-Native {
    # Docker ghi tiến trình ra stderr; Windows PowerShell 5.1 coi đó là lỗi khi ErrorActionPreference=Stop.
    # Nên nhận stderr như văn bản thường và chỉ dựa vào mã thoát để biết lệnh có thất bại không.
    param([scriptblock]$Command, [string]$What)
    $previous = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { & $Command 2>&1 | ForEach-Object { Write-Host "$_" } }
    finally { $ErrorActionPreference = $previous }
    if ($LASTEXITCODE -ne 0) { throw "Thất bại: $What (mã $LASTEXITCODE)" }
}

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$dir = Join-Path $OutDir $stamp
New-Item -ItemType Directory -Force -Path $dir | Out-Null

try {
    Write-Host "[1/2] Sao lưu cơ sở dữ liệu..."
    Invoke-Native { docker compose exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f /tmp/vcraft.dump' } 'pg_dump'
    Invoke-Native { docker compose cp db:/tmp/vcraft.dump (Join-Path $dir 'db.dump') } 'chép db.dump'
    Invoke-Native { docker compose exec -T db rm -f /tmp/vcraft.dump } 'dọn tệp tạm'

    Write-Host "[2/2] Sao lưu thư mục ảnh..."
    # Chạy container tạm dùng chung volume media_data của dịch vụ backend, nên không cần backend đang chạy.
    Invoke-Native { docker compose run --rm --no-deps -T -v "${dir}:/backup" --entrypoint sh backend -c 'tar czf /backup/media.tgz -C /data/media .' } 'nén thư mục ảnh'
} catch {
    Remove-Item -Recurse -Force $dir -ErrorAction SilentlyContinue
    throw
}

$dump = Get-Item (Join-Path $dir 'db.dump')
$media = Get-Item (Join-Path $dir 'media.tgz')
if ($dump.Length -eq 0 -or $media.Length -eq 0) {
    throw "Bản sao lưu rỗng ở $dir, hãy kiểm tra lại."
}

if ($Keep -gt 0) {
    Get-ChildItem $OutDir -Directory | Where-Object { $_.Name -match '^\d{8}-\d{6}$' } |
        Sort-Object Name -Descending | Select-Object -Skip $Keep |
        ForEach-Object { Remove-Item -Recurse -Force $_.FullName; Write-Host "Đã xóa bản cũ $($_.Name)" }
}

Write-Host ("Xong: {0}  (db {1:N0} KB, ảnh {2:N0} KB)" -f $dir, ($dump.Length / 1KB), ($media.Length / 1KB))
