-- Таблица ролей
CREATE TABLE IF NOT EXISTS roles (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE CHECK (name IN ('admin', 'user'))
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    balance INT NOT NULL,
    role_id INT NOT NULL REFERENCES roles(id)
);

-- Таблица токенов обновления
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Таблица типов транспорта
CREATE TABLE IF NOT EXISTS transport_types (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);


-- Таблица маршрутов
CREATE TABLE IF NOT EXISTS routes (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    departure_city VARCHAR(100) NOT NULL,
    arrival_city VARCHAR(100) NOT NULL,
    UNIQUE(departure_city, arrival_city)
);





CREATE TABLE IF NOT EXISTS tickets (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transport_type_id INT NOT NULL REFERENCES transport_types(id),
    route_id INT NOT NULL REFERENCES routes(id),
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time TIMESTAMP WITH TIME ZONE NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    available_tickets INT NOT NULL CHECK (available_tickets >= 0)  -- Количество доступных билетов
);


CREATE INDEX IF NOT EXISTS idx_transport_route_departure
ON tickets(transport_type_id, route_id, departure_time);

CREATE INDEX IF NOT EXISTS idx_route_departure
ON tickets(route_id, departure_time);


-- Таблица бронирований
CREATE TABLE IF NOT EXISTS bookings (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ticket_id INT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,  -- Связь с билетом
    booking_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'confirmed', 'canceled')),
    ticket_quantity INT NOT NULL CHECK (ticket_quantity > 0)  -- Количество забронированных билетов
);

-- Индексы для bookings
CREATE INDEX IF NOT EXISTS idx_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_status ON bookings(status);





-- Таблица пополнений баланса
CREATE TABLE IF NOT EXISTS deposits (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'completed', 'failed')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для deposits
CREATE INDEX IF NOT EXISTS idx_user_deposit ON deposits(user_id);
CREATE INDEX IF NOT EXISTS idx_status_deposit ON deposits(status);

-- Вставка ролей
INSERT INTO roles (name)
VALUES ('admin')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name)
VALUES ('user')
ON CONFLICT (name) DO NOTHING;

INSERT INTO transport_types (name)
VALUES ('bus')
ON CONFLICT (name) DO NOTHING;

INSERT INTO transport_types (name)
VALUES ('avia')
ON CONFLICT (name) DO NOTHING;

INSERT INTO transport_types (name)
VALUES ('train')
ON CONFLICT (name) DO NOTHING;




-- Вставка маршрутов (5 маршрутов)
INSERT INTO routes (departure_city, arrival_city) VALUES ('Москва', 'Санкт-Петербург')
ON CONFLICT (departure_city, arrival_city) DO NOTHING;

INSERT INTO routes (departure_city, arrival_city) VALUES ('Казань', 'Екатеринбург')
ON CONFLICT (departure_city, arrival_city) DO NOTHING;

INSERT INTO routes (departure_city, arrival_city) VALUES ('Новосибирск', 'Омск')
ON CONFLICT (departure_city, arrival_city) DO NOTHING;

INSERT INTO routes (departure_city, arrival_city) VALUES ('Самара', 'Волгоград')
ON CONFLICT (departure_city, arrival_city) DO NOTHING;

INSERT INTO routes (departure_city, arrival_city) VALUES ('Краснодар', 'Сочи')
ON CONFLICT (departure_city, arrival_city) DO NOTHING;

-----------------------------------
-- Вставка билетов (50 билетов)
-----------------------------------

-- Маршрут 1: Москва -> Санкт-Петербург (10 билетов)
INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T10:00:00+03:00',
  '2026-01-01T12:00:00+03:00',
  150.00,
  100
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T11:00:00+03:00',
  '2026-01-01T13:00:00+03:00',
  120.00,
  80
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T12:00:00+03:00',
  '2026-01-01T14:00:00+03:00',
  90.00,
  50
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T13:00:00+03:00',
  '2026-01-01T15:00:00+03:00',
  160.00,
  60
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T14:00:00+03:00',
  '2026-01-01T16:00:00+03:00',
  130.00,
  70
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T15:00:00+03:00',
  '2026-01-01T17:00:00+03:00',
  100.00,
  40
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T16:00:00+03:00',
  '2026-01-01T18:00:00+03:00',
  155.00,
  55
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T17:00:00+03:00',
  '2026-01-01T19:00:00+03:00',
  125.00,
  65
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T18:00:00+03:00',
  '2026-01-01T20:00:00+03:00',
  95.00,
  45
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Москва' AND arrival_city = 'Санкт-Петербург'),
  '2026-01-01T19:00:00+03:00',
  '2026-01-01T21:00:00+03:00',
  165.00,
  75
);

-- Маршрут 2: Казань -> Екатеринбург (10 билетов)
INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T09:00:00+03:00',
  '2026-01-02T11:00:00+03:00',
  110.00,
  60
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T10:00:00+03:00',
  '2026-01-02T12:00:00+03:00',
  80.00,
  40
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T11:00:00+03:00',
  '2026-01-02T13:00:00+03:00',
  140.00,
  90
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T12:00:00+03:00',
  '2026-01-02T14:00:00+03:00',
  115.00,
  70
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T13:00:00+03:00',
  '2026-01-02T15:00:00+03:00',
  85.00,
  50
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T14:00:00+03:00',
  '2026-01-02T16:00:00+03:00',
  145.00,
  80
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T15:00:00+03:00',
  '2026-01-02T17:00:00+03:00',
  120.00,
  65
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T16:00:00+03:00',
  '2026-01-02T18:00:00+03:00',
  90.00,
  55
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T17:00:00+03:00',
  '2026-01-02T19:00:00+03:00',
  150.00,
  75
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Казань' AND arrival_city = 'Екатеринбург'),
  '2026-01-02T18:00:00+03:00',
  '2026-01-02T20:00:00+03:00',
  130.00,
  85
);

-- Маршрут 3: Новосибирск -> Омск (10 билетов)
INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T08:00:00+03:00',
  '2026-01-03T10:00:00+03:00',
  75.00,
  40
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T09:00:00+03:00',
  '2026-01-03T11:00:00+03:00',
  135.00,
  90
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T10:00:00+03:00',
  '2026-01-03T12:00:00+03:00',
  105.00,
  60
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T11:00:00+03:00',
  '2026-01-03T13:00:00+03:00',
  80.00,
  50
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T12:00:00+03:00',
  '2026-01-03T14:00:00+03:00',
  140.00,
  70
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T13:00:00+03:00',
  '2026-01-03T15:00:00+03:00',
  110.00,
  60
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T14:00:00+03:00',
  '2026-01-03T16:00:00+03:00',
  85.00,
  45
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T15:00:00+03:00',
  '2026-01-03T17:00:00+03:00',
  145.00,
  75
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T16:00:00+03:00',
  '2026-01-03T18:00:00+03:00',
  115.00,
  65
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Новосибирск' AND arrival_city = 'Омск'),
  '2026-01-03T17:00:00+03:00',
  '2026-01-03T19:00:00+03:00',
  90.00,
  50
);

-- Маршрут 4: Самара -> Волгоград (10 билетов)
INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T07:00:00+03:00',
  '2026-01-04T09:00:00+03:00',
  155.00,
  80
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T08:00:00+03:00',
  '2026-01-04T10:00:00+03:00',
  125.00,
  70
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T09:00:00+03:00',
  '2026-01-04T11:00:00+03:00',
  95.00,
  60
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T10:00:00+03:00',
  '2026-01-04T12:00:00+03:00',
  165.00,
  85
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T11:00:00+03:00',
  '2026-01-04T13:00:00+03:00',
  135.00,
  75
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T12:00:00+03:00',
  '2026-01-04T14:00:00+03:00',
  105.00,
  65
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T13:00:00+03:00',
  '2026-01-04T15:00:00+03:00',
  170.00,
  90
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T14:00:00+03:00',
  '2026-01-04T16:00:00+03:00',
  140.00,
  80
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T15:00:00+03:00',
  '2026-01-04T17:00:00+03:00',
  110.00,
  70
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Самара' AND arrival_city = 'Волгоград'),
  '2026-01-04T16:00:00+03:00',
  '2026-01-04T18:00:00+03:00',
  175.00,
  95
);

-- Маршрут 5: Краснодар -> Сочи (10 билетов)
INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T06:00:00+03:00',
  '2026-01-05T08:00:00+03:00',
  130.00,
  75
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T07:00:00+03:00',
  '2026-01-05T09:00:00+03:00',
  100.00,
  60
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T08:00:00+03:00',
  '2026-01-05T10:00:00+03:00',
  160.00,
  85
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T09:00:00+03:00',
  '2026-01-05T11:00:00+03:00',
  140.00,
  70
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T10:00:00+03:00',
  '2026-01-05T12:00:00+03:00',
  110.00,
  65
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T11:00:00+03:00',
  '2026-01-05T13:00:00+03:00',
  170.00,
  80
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T12:00:00+03:00',
  '2026-01-05T14:00:00+03:00',
  150.00,
  75
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'bus'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T13:00:00+03:00',
  '2026-01-05T15:00:00+03:00',
  120.00,
  70
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'avia'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T14:00:00+03:00',
  '2026-01-05T16:00:00+03:00',
  180.00,
  85
);

INSERT INTO tickets (transport_type_id, route_id, departure_time, arrival_time, price, available_tickets)
VALUES (
  (SELECT id FROM transport_types WHERE name = 'train'),
  (SELECT id FROM routes WHERE departure_city = 'Краснодар' AND arrival_city = 'Сочи'),
  '2026-01-05T15:00:00+03:00',
  '2026-01-05T17:00:00+03:00',
  160.00,
  80
);