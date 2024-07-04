-- create own schema --
CREATE SCHEMA gvggroup
    AUTHORIZATION postgres;

COMMENT ON SCHEMA gvggroup
    IS 'test assessment ';

create table gvggroup.products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INT NOT NULL,
    created TIMESTAMP NOT NULL,
    modified TIMESTAMP
);

INSERT INTO gvggroup.products (id, name, price, quantity, created, modified) VALUES
     (1, 'Apple iPhone 13', 799.00, 50, '2024-01-01 10:00:00', NULL),
     (2, 'Samsung Galaxy S21', 699.00, 40, '2024-01-02 11:00:00', NULL),
     (3, 'Google Pixel 6', 599.00, 30, '2024-01-03 12:00:00', NULL),
     (4, 'OnePlus 9', 729.00, 25, '2024-01-04 13:00:00', NULL),
     (5, 'Sony WH-1000XM4', 348.00, 100, '2024-01-05 14:00:00', NULL),
     (6, 'Apple MacBook Pro', 1999.00, 15, '2024-01-06 15:00:00', NULL),
     (7, 'Dell XPS 13', 999.00, 20, '2024-01-07 16:00:00', NULL),
     (8, 'HP Spectre x360', 1099.00, 10, '2024-01-08 17:00:00', NULL),
     (9, 'Microsoft Surface Laptop 4', 1299.00, 18, '2024-01-09 18:00:00', NULL),
     (10, 'Samsung QLED TV', 1499.00, 8, '2024-01-10 19:00:00', NULL),
     (11, 'LG OLED TV', 1799.00, 7, '2024-01-11 20:00:00', NULL),
     (12, 'Bose SoundLink Revolve', 199.00, 45, '2024-01-12 21:00:00', NULL),
     (13, 'Sonos One', 199.00, 50, '2024-01-13 22:00:00', NULL),
     (14, 'Amazon Echo Dot', 49.99, 60, '2024-01-14 23:00:00', NULL),
     (15, 'Google Nest Hub', 89.99, 55, '2024-01-15 09:00:00', NULL),
     (16, 'Apple iPad Pro', 799.00, 22, '2024-01-16 10:00:00', NULL),
     (17, 'Samsung Galaxy Tab S7', 649.99, 25, '2024-01-17 11:00:00', NULL),
     (18, 'Microsoft Surface Pro 7', 749.99, 18, '2024-01-18 12:00:00', NULL),
     (19, 'Kindle Paperwhite', 129.99, 40, '2024-01-19 13:00:00', NULL),
     (20, 'Sony PlayStation 5', 499.99, 30, '2024-01-20 14:00:00', NULL);