-- V3__default_rules_schema.sql

-- ADMIN 역할 추가
MERGE INTO roles (name, description, created_at, updated_at, created_by, updated_by)
KEY(name) -- 'name' 컬럼을 기준으로 병합 (존재하면 업데이트, 없으면 삽입)
VALUES ('ADMIN', 'System Administrator role', NOW(), NOW(), 'system', 'system');

-- USER 역할 추가
MERGE INTO roles (name, description, created_at, updated_at)
KEY(name)
VALUES ('USER', 'Default User role', NOW(), NOW(), 'system', 'system');