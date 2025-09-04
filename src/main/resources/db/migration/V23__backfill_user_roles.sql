WITH user_role AS (
  SELECT id FROM roles WHERE name='USER'
)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, (SELECT id FROM user_role)
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id AND ur.role_id = (SELECT id FROM user_role)
WHERE ur.user_id IS NULL;