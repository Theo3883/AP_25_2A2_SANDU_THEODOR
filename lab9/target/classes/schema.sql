DROP TABLE IF EXISTS sister_cities;
DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS countries;
DROP TABLE IF EXISTS continents;

CREATE TABLE continents
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE countries
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    code         VARCHAR(10),
    continent_id INTEGER REFERENCES continents (id) ON DELETE SET NULL
);

CREATE TABLE cities
(
    id         SERIAL PRIMARY KEY,
    country_id INTEGER REFERENCES countries (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    is_capital BOOLEAN,
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION
);

CREATE TABLE sister_cities (
                               id SERIAL PRIMARY KEY,
                               city1_id INTEGER REFERENCES cities(id) ON DELETE CASCADE,
                               city2_id INTEGER REFERENCES cities(id) ON DELETE CASCADE,
                               CONSTRAINT unique_sister_pair UNIQUE (city1_id, city2_id),
                               CONSTRAINT no_self_relationship CHECK (city1_id <> city2_id)
);

CREATE INDEX IF NOT EXISTS idx_sister_cities_city1_id ON sister_cities(city1_id);
CREATE INDEX IF NOT EXISTS idx_sister_cities_city2_id ON sister_cities(city2_id);