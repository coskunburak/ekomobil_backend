-- 1) Kolonu ekle (önce nullable ekliyoruz ki update atabilelim)
ALTER TABLE users
ADD COLUMN IF NOT EXISTS email VARCHAR(255);


ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);
