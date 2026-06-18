@echo off
setlocal
cd /d "%~dp0.."

echo === MentorIA - verificando servicos ===

powershell -NoProfile -Command ^
  "$pg = Test-NetConnection localhost -Port 5432 -WarningAction SilentlyContinue; ^
   $api = Test-NetConnection localhost -Port 8080 -WarningAction SilentlyContinue; ^
   if (-not $pg.TcpTestSucceeded) { Write-Host '[ERRO] PostgreSQL nao esta na porta 5432.' -ForegroundColor Red; Write-Host '       Instale PostgreSQL ou rode: docker compose up -d postgres' -ForegroundColor Yellow; exit 1 }; ^
   if (-not $api.TcpTestSucceeded) { Write-Host '[AVISO] Backend nao esta na porta 8080.' -ForegroundColor Yellow; Write-Host '        Inicie pelo IntelliJ ou: cd backend ^&^& mvnw.cmd spring-boot:run' -ForegroundColor Yellow } else { Write-Host '[OK] Backend ativo em http://localhost:8080/api' -ForegroundColor Green }; ^
   Write-Host '[OK] PostgreSQL ativo na porta 5432' -ForegroundColor Green"

if errorlevel 1 exit /b 1

echo.
echo === Frontend (reinicie se alterou nuxt.config.ts) ===
echo cd frontend
echo pnpm dev
echo.
echo Conta de teste: teste@gmail.com / teste123
