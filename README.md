Fixing API

A Fixing API é o backend oficial do Fixing, um aplicativo inspirado no modelo da Uber, porém voltado para serviços gerais — conectando clientes a prestadores de forma rápida, segura e eficiente.

🚀 Tecnologias Utilizadas

Kotlin
Spring Boot
Spring Security (JWT)
PostgreSQL
JPA / Hibernate

🔐 Recursos de Segurança

Autenticação JWT
Revogação de tokens através de tokenVersion
Sistema de banimento temporário e permanente
Hash de senhas usando BCrypt
Verificação automática de expiração de banimentos

📌 Principais Funcionalidades

Registro e login de usuários
Gerenciamento de perfis (cliente e prestador)
Dashboard administrativo
Banir / desbanir usuários
Controle de tentativas de login
Gerenciamento de planos, cargos e permissões

🛠️ Configuração

Crie um arquivo .env com:
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MS=

▶️ Como Rodar o Projeto
./gradlew bootRun

📄 Licença
Projeto proprietário — uso restrito. Entre em contato para permissões.
