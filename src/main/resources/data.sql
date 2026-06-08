INSERT INTO categories (name, description)
VALUES
    ('World Cup', 'Unique World Cup jerseys'),
    ('Coins', 'Unique coins.'),
    ('Sportswear', 'Gear for playing sports.'),
    ('Cleaning', 'Products for cleaning your house.')
ON CONFLICT DO NOTHING;

INSERT INTO products (name, category_id, description, price, stock_quantity)
SELECT 'Limited edition Modric jersey', c.id, 'Last dance limited edition Jersey', 999.99, 99
FROM categories c WHERE c.name = 'World Cup'
AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.name = 'Limited edition Modric jersey'
);

INSERT INTO products (name, category_id, description, price, stock_quantity)
SELECT 'Goose Coin', c.id, 'Unique Goose Coin', 500.00, 4
FROM categories c WHERE c.name = 'Coins'
AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.name = 'Goose Coin'
);

INSERT INTO products (name, category_id, description, price, stock_quantity)
SELECT 'Running Shoes', c.id, 'Lightweight running shoes for mixed terrain.', 99.99, 18
FROM categories c WHERE c.name = 'Sportswear'
AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.name = 'Running Shoes'
);

INSERT INTO products (name, category_id, description, price, stock_quantity)
SELECT 'Detergent', c.id, 'Best detergent for cleaning clothes.', 5.00, 1000
FROM categories c WHERE c.name = 'Cleaning'
AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.name = 'Detergent'
);

INSERT INTO roles (name)
VALUES ('USER'), ('ADMIN')
ON CONFLICT DO NOTHING;
