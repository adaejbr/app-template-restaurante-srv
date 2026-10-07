param(
    [string]$Email = 'joao@exemplo.com',
    [int]$ValidadeSegundos = 3600
)
$ErrorActionPreference = 'Stop'
# Ferramenta local para testes. Nao e um endpoint de login.
if (-not $env:JWT_SECRET) { throw 'Defina JWT_SECRET em Base64 antes de executar.' }
$chave = [Convert]::FromBase64String($env:JWT_SECRET)
if ($chave.Length -lt 32) { throw 'JWT_SECRET precisa ter ao menos 32 bytes.' }
function ConvertTo-Base64Url([byte[]]$Bytes) {
    return [Convert]::ToBase64String($Bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}
$agora = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$cabecalho = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes('{"alg":"HS256","typ":"JWT"}'))
$claims = @{ sub=$Email; iat=($agora - 7200); exp=($agora + $ValidadeSegundos) } | ConvertTo-Json -Compress
$payload = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($claims))
$conteudo = "$cabecalho.$payload"
$hmac = [Security.Cryptography.HMACSHA256]::new($chave)
try {
    $assinatura = ConvertTo-Base64Url ($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($conteudo)))
    "$conteudo.$assinatura"
} finally { $hmac.Dispose() }
