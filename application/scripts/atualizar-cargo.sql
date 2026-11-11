-- Execute uma unica vez se a tabela foi criada pela versao anterior, sem cargo.
-- Nao execute em bancos novos: banco-local.sql ja inclui a coluna.
USE restaurante;
ALTER TABLE usuarios ADD COLUMN cargo VARCHAR(30) NOT NULL DEFAULT 'CLIENTE';
