# PowerShell 脚本：设置 UTF-8 编码并启动应用
# 使用方法：在 PowerShell 中运行 .\start-utf8.ps1

# 设置 PowerShell 输出编码为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

# 设置环境变量
$env:JAVA_OPTS = "-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN"

Write-Host "已设置 UTF-8 编码，正在启动应用..." -ForegroundColor Green

# 启动应用
mvn spring-boot:run




