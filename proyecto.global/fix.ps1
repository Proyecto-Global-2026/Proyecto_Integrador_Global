 = Get-Content -Raw 'postman\ProyectoGlobal.postman_collection.json'
 =  -replace '/api/auth/usuarios', '/api/usuarios'
Set-Content -Path 'postman\ProyectoGlobal.postman_collection.json' -Value  -NoNewline
