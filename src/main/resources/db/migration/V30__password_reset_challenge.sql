CREATE TABLE IF NOT EXISTS password_reset_challenge (
  id            BIGSERIAL PRIMARY KEY,
  email         VARCHAR(255) NOT NULL,
  code_hash     VARCHAR(64)  NOT NULL,
  salt          VARCHAR(24)  NOT NULL,
  expires_at    TIMESTAMPTZ  NOT NULL,
  attempts      INT          NOT NULL DEFAULT 0,
  max_attempts  INT          NOT NULL DEFAULT 5,
  consumed      BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  consumed_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_prc_email_expires
  ON password_reset_challenge (email, expires_at DESC);
