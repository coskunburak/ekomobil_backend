DO $$
DECLARE
  v_uid BIGINT;
  v_admin_role BIGINT;
BEGIN
  SELECT id INTO v_uid FROM users WHERE username='admin';
  IF v_uid IS NULL THEN
    INSERT INTO users(username, password, enabled)
    VALUES ('admin', '$2a$10$REPLACE_ME_WITH_BCRYPT', TRUE)
    RETURNING id INTO v_uid;
  END IF;

  SELECT id INTO v_admin_role FROM roles WHERE name='ADMIN';

  BEGIN
    INSERT INTO user_roles(user_id, role_id) VALUES (v_uid, v_admin_role);
  EXCEPTION WHEN unique_violation THEN
    -- zaten ekli
    NULL;
  END;
END $$;
