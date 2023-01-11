DROP TABLE IF EXISTS flight_route;
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS air_plane;
DROP TABLE IF EXISTS schedule;

CREATE TABLE station (id SERIAL PRIMARY KEY, iata_code VARCHAR(3) NOT NULL UNIQUE, full_name VARCHAR(255));

CREATE TABLE flight_route (id SERIAL PRIMARY KEY, from_station INT NOT NULL, to_station INT NOT NULL,
    flight_id INT, UNIQUE(from_station, to_station));

CREATE TABLE air_plane (id SERIAL PRIMARY KEY,
    name VARCHAR(7) NOT NULL,
    work_in_progress BOOLEAN DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 1,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

CREATE TABLE schedule (id SERIAL PRIMARY KEY, id_route INT NOT NULL, id_plane INT NOT NULL);

insert into station(iata_code, full_name) values ('CDG', 'Paris Charles de Gaulle'),
('ORY', 'Paris Orly'),('NCE', 'Nice Cote d Azur');

insert into air_plane(name) values ('AF001'),('AF002'),('AF003');