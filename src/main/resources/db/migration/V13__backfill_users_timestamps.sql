UPDATE users SET created_at = COALESCE(created_at, NOW()),
                 updated_at = COALESCE(updated_at, NOW());