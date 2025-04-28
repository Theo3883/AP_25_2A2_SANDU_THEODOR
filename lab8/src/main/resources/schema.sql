DROP TABLE IF EXISTS countries;
DROP TABLE IF EXISTS continents;
DROP TABLE IF EXISTS cities;

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
    continent_id INTEGER REFERENCES continents (id)
);

CREATE TABLE cities
(
    id         SERIAL PRIMARY KEY,
    country_id INTEGER REFERENCES countries (id),
    name       VARCHAR(100) NOT NULL,
    is_capital BOOLEAN,
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION
);