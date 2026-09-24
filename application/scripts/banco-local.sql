-- Execute manualmente em um MySQL local de desenvolvimento.
CREATE DATABASE IF NOT EXISTS restaurante CHARACTER SET utf8mb4;
USE restaurante;
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);
INSERT INTO usuarios (nome, email)
SELECT 'Joao', 'joao@exemplo.com'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'joao@exemplo.com');
