CREATE TABLE cards (
  id               BIGSERIAL PRIMARY KEY,
  user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  card_number      VARCHAR(32) NOT NULL UNIQUE,
  alias            VARCHAR(64),
  balance_cents    BIGINT NOT NULL DEFAULT 0,
  currency         VARCHAR(3) NOT NULL DEFAULT 'TRY',
  status           VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  version          BIGINT NOT NULL DEFAULT 0,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cards_user ON cards(user_id);

CREATE TABLE card_transactions (
  id             BIGSERIAL PRIMARY KEY,
  card_id        BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
  type           VARCHAR(16) NOT NULL,
  amount_cents   BIGINT NOT NULL,
  balance_after  BIGINT NOT NULL,
  note           TEXT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_card_tx_card ON card_transactions(card_id);
