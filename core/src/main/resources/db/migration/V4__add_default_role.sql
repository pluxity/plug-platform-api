-- ADMIN 역할 추가 (이미 'ADMIN' 이름의 역할이 존재하면 아무것도 하지 않음)
INSERT INTO roles (name, description, created_at, updated_at)
VALUES ('ADMIN', 'System Administrator role created by Flyway migration', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- USER 역할 추가 (이미 'USER' 이름의 역할이 존재하면 아무것도 하지 않음)
INSERT INTO roles (name, description, created_at, updated_at)
VALUES ('USER', 'Default User role created by Flyway migration', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;