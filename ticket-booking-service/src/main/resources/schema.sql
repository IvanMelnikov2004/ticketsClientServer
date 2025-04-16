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

