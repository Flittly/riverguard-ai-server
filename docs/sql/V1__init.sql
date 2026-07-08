-- RiverGuard AI Platform v1.0 — 初始化数据库
-- PostgreSQL

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    nickname VARCHAR(64),
    phone VARCHAR(20),
    email VARCHAR(128),
    status SMALLINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.status IS '0=禁用 1=启用';
COMMENT ON COLUMN sys_user.deleted IS '0=未删除 1=已删除';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_role IS '角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE (user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_scope (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    manageable_role_id BIGINT NOT NULL,
    UNIQUE (role_id, manageable_role_id)
);

COMMENT ON TABLE sys_role_scope IS '角色管理范围表';

-- 初始化三种角色
INSERT INTO sys_role (id, code, name, description, create_time)
VALUES (1, 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO sys_role (id, code, name, description, create_time)
VALUES (2, 'ADMIN', '管理员', '普通管理员，可管理普通用户', NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO sys_role (id, code, name, description, create_time)
VALUES (3, 'USER', '普通用户', '普通用户权限', NOW())
ON CONFLICT (code) DO NOTHING;

-- 初始化角色管理范围
-- SUPER_ADMIN 可以管理所有角色
INSERT INTO sys_role_scope (id, role_id, manageable_role_id) VALUES (1, 1, 1) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_scope (id, role_id, manageable_role_id) VALUES (2, 1, 2) ON CONFLICT DO NOTHING;
INSERT INTO sys_role_scope (id, role_id, manageable_role_id) VALUES (3, 1, 3) ON CONFLICT DO NOTHING;
-- ADMIN 只能管理普通用户
INSERT INTO sys_role_scope (id, role_id, manageable_role_id) VALUES (4, 2, 3) ON CONFLICT DO NOTHING;

-- 初始化超级管理员用户 (密码: admin123, BCrypt 加密)
INSERT INTO sys_user (id, username, password, nickname, status, create_time, update_time, deleted)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '超级管理员', 1, NOW(), NOW(), 0)
ON CONFLICT (username) DO NOTHING;

INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1) ON CONFLICT DO NOTHING;
