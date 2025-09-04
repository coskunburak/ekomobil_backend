CREATE UNIQUE INDEX IF NOT EXISTS uq_roles_name_ci ON roles (lower(name));
