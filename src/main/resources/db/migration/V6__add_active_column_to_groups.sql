-- Adiciona a coluna active na tabela groups
ALTER TABLE groups ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

