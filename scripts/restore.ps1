<#
.SYNOPSIS
  Khôi phục DB và ảnh từ một bản do scripts\backup.ps1 tạo ra.
.DESCRIPTION
  GHI ĐÈ toàn bộ dữ liệu DB và thư mục ảnh hiện tại. Script hỏi xác nhận trừ khi có -Force.
  Các bước: dừng frontend + backend, khôi phục ảnh, khôi phục DB (pg_restore --clean), chạy lại stack.
.PARAMETER From
  Thư mục bản sao lưu, ví dụ backups\20261124-103000 (có db.dump và media.tgz).
.PARAMETER Force
  Bỏ qua câu hỏi xác nhận.
.EXAMPLE
  .\scripts\restore.ps1 -From backups\20261124-103000
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$From,
    [switch]$Force
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

$dir = (Resolve-Path $From).Path
$dump = Join-Path $dir 'db.dump'
if (-not (Test-Path $dump) -or -not (Test-Path (Join-Path $dir 'media.tgz'))) {
    throw "Thiếu db.dump hoặc media.tgz trong $dir"
}

if (-not $Force) {
    Write-Host "Sẽ GHI ĐÈ toàn bộ dữ liệu DB và ảnh hiện tại bằng bản $dir."
    if ((Read-Host 'Gõ YES để tiếp tục') -cne 'YES') { Write-Host 'Đã hủy.'; return }
}

Write-Host "[1/4] Dừng frontend và backend..."
Invoke-Native { docker compose stop frontend backend } 'dừng dịch vụ'
Invoke-Native { docker compose up -d db } 'bật db'

Write-Host "[2/4] Khôi phục thư mục ảnh..."
Invoke-Native { docker compose run --rm --no-deps -T -v "${dir}:/backup:ro" --entrypoint sh backend -c 'find /data/media -mindepth 1 -delete && tar xzf /backup/media.tgz -C /data/media' } 'khôi phục ảnh'

Write-Host "[3/4] Khôi phục cơ sở dữ liệu..."
Invoke-Native { docker compose cp $dump db:/tmp/restore.dump } 'chép db.dump vào container'
# --clean --if-exists xóa rồi tạo lại đối tượng; --no-owner tránh lỗi khi tên user khác máy gốc.
Invoke-Native { docker compose exec -T db sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists --no-owner /tmp/restore.dump' } 'pg_restore'
Invoke-Native { docker compose exec -T db rm -f /tmp/restore.dump } 'dọn tệp tạm'

Write-Host "[4/4] Chạy lại stack..."
Invoke-Native { docker compose up -d } 'khởi động lại'
Write-Host "Xong. Kiểm tra: http://localhost:5173/api/health/db"
