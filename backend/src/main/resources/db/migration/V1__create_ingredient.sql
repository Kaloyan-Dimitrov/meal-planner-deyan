CREATE TABLE IF NOT EXISTS ingredient (
                                          id             BIGSERIAL PRIMARY KEY,
                                          name           VARCHAR(255) NOT NULL,
    category       VARCHAR(255) NOT NULL,
    kcal_per_100g  NUMERIC(10,2) NOT NULL,
    UNIQUE (name, category)
    );
