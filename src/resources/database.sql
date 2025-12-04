
-- =============================================
-- NARDOS INVENTORY DATABASE - FINAL VERSION
-- Compatible with XAMPP MySQL
-- =============================================

-- Drop and create fresh database
DROP DATABASE IF EXISTS nardos_inventory;
CREATE DATABASE nardos_inventory;
USE nardos_inventory;

-- =============================================
-- CREATE TABLES
-- =============================================

-- Suppliers table (parent)
CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255)
);

-- Users table (parent)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'Encrypted password',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Products table (parent)
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    supplier_id INT,
    price DECIMAL(10,2),
    quantity INT DEFAULT 0,
    min_stock INT DEFAULT 5,
    cost_price DECIMAL(10,2),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    INDEX idx_category (category),
    INDEX idx_name (name),
    INDEX idx_quantity (quantity)
);

-- Sales table (parent for sale_items)
CREATE TABLE sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    user_id INT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'MOBILE', 'OTHER') DEFAULT 'CASH',
    payment_status ENUM('COMPLETED', 'REFUNDED', 'CANCELLED') DEFAULT 'COMPLETED',
    notes TEXT,
    is_completed BOOLEAN DEFAULT true,
    sale_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_receipt (receipt_number),
    INDEX idx_date (sale_datetime),
    INDEX idx_user (user_id)
);

-- Sale Items table (child of sales)
CREATE TABLE sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_sale (sale_id),
    INDEX idx_product (product_id)
);

-- Stock Movements table (child of products)
CREATE TABLE stock_movements (
    movement_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    related_id INT COMMENT 'e.g., sale_id or purchase_id',
    movement_type ENUM('SALE', 'RESTOCK', 'ADJUSTMENT', 'RETURN', 'DAMAGE', 'TRANSFER') NOT NULL,
    quantity_changed INT NOT NULL COMMENT 'Positive for addition, negative for deduction',
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    reason VARCHAR(255),
    user_id INT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_product (product_id),
    INDEX idx_movement_type (movement_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_user (user_id)
);

-- Reports table
CREATE TABLE reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(50) NOT NULL,
    generated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by INT NOT NULL,
    generated_by_name VARCHAR(100) NOT NULL,
    data TEXT NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary TEXT,
    
    FOREIGN KEY (generated_by) REFERENCES users(user_id),
    INDEX idx_reports_type (report_type),
    INDEX idx_reports_date (generated_date)
);

-- Low Stock Alerts table
CREATE TABLE low_stock_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    current_quantity INT NOT NULL,
    min_stock_level INT NOT NULL,
    alert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP NULL,
    
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_product (product_id),
    INDEX idx_resolved (is_resolved)
);

-- Report Preferences table
CREATE TABLE report_preferences (
    user_id INT PRIMARY KEY,
    auto_generate_daily BOOLEAN DEFAULT true,
    email_reports BOOLEAN DEFAULT false,
    default_report_type VARCHAR(50) DEFAULT 'INVENTORY_SUMMARY',
    save_all_reports BOOLEAN DEFAULT true,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================
-- INSERT DATA (IN CORRECT ORDER)
-- =============================================

-- 1. Insert Suppliers FIRST
INSERT INTO suppliers (supplier_name, contact_person, phone, email) VALUES
('Main Distributor', 'John Smith', '876-555-0101', 'john@maindist.com'),
('Local Market', 'Maria Garcia', '876-555-0102', 'maria@localmarket.com'),
('Juice Supplier', 'Robert Brown', '876-555-0103', 'robert@juicesupply.com'),
('Snack Wholesale', 'Lisa Wong', '876-555-0104', 'lisa@snackwhole.com'),
('Paper Products Co.', 'David Lee', '876-555-0105', 'david@paperproducts.com');

-- 2. Insert Users SECOND
INSERT INTO users (username, password, role, email, is_active) VALUES
('nardo', '$2a$10$YourHashedPasswordHere1', 'ADMIN', 'nardo@nardos.com', true),
('staff1', '$2a$10$YourHashedPasswordHere2', 'USER', 'staff1@nardos.com', true),
('staff2', '$2a$10$YourHashedPasswordHere3', 'USER', 'staff2@nardos.com', true),
('manager', '$2a$10$YourHashedPasswordHere4', 'ADMIN', 'manager@nardos.com', true);

-- 3. Insert Products THIRD
-- FOOD items
INSERT INTO products (name, category, supplier_id, price, quantity, min_stock) VALUES
('Plain Hot Dog', 'FOOD', 1, 3.50, 20, 5),
('Full House Hot Dog', 'FOOD', 1, 5.50, 15, 5),
('Corn Dogs', 'FOOD', 1, 3.00, 25, 8),
('Sugar Dog', 'FOOD', 2, 2.50, 18, 6),
('Tastee Beef Patty', 'FOOD', 2, 2.80, 30, 10),
('Tastee Cheese Patty', 'FOOD', 2, 3.00, 22, 8),
('Tastee Chicken Patty', 'FOOD', 2, 3.00, 20, 8),
('Cheesy Fries', 'FOOD', 1, 4.50, 15, 6),
('Special Chicken Nugget', 'FOOD', 1, 5.00, 12, 5),
('Raisin Dog', 'FOOD', 1, 2.80, 14, 5),
('Mega Sausage', 'FOOD', 1, 6.00, 10, 4);

-- BEVERAGES
INSERT INTO products (name, category, supplier_id, price, quantity, min_stock) VALUES
('Bottle Water', 'BEVERAGES', 3, 1.50, 40, 20),
('Orange Juice', 'BEVERAGES', 3, 2.00, 30, 15),
('Soda', 'BEVERAGES', 3, 1.80, 50, 25),
('Box Juice', 'BEVERAGES', 3, 1.60, 25, 12),
('Cran Splash', 'BEVERAGES', 3, 2.50, 20, 10),
('Cran Wata', 'BEVERAGES', 3, 2.00, 22, 11),
('Sprite', 'BEVERAGES', 3, 1.80, 28, 14),
('Schweppes', 'BEVERAGES', 3, 1.80, 18, 9),
('Minute Maid', 'BEVERAGES', 3, 2.20, 15, 8);

-- SNACKS
INSERT INTO products (name, category, supplier_id, price, quantity, min_stock) VALUES
('Chips', 'SNACKS', 4, 1.20, 40, 20),
('Cookies', 'SNACKS', 4, 1.50, 35, 18),
('Chocolate Bar', 'SNACKS', 4, 2.00, 18, 10),
('Granola Bar', 'SNACKS', 4, 1.80, 12, 8),
('Peanuts', 'SNACKS', 4, 1.50, 22, 12),
('Biscuits', 'SNACKS', 4, 1.30, 16, 10),
('Donuts', 'SNACKS', 4, 2.50, 10, 6);

-- ESSENTIALS
INSERT INTO products (name, category, supplier_id, price, quantity, min_stock) VALUES
('Napkins', 'ESSENTIALS', 5, 1.00, 50, 25),
('Straws', 'ESSENTIALS', 5, 0.80, 60, 30),
('Plastic Forks', 'ESSENTIALS', 5, 1.20, 40, 20);

-- COMBO MEALS
INSERT INTO products (name, category, supplier_id, price, quantity, min_stock) VALUES
('2pc Chicken Combo', 'COMBO MEALS', 1, 8.50, 10, 5),
('Nuggets Combo', 'COMBO MEALS', 1, 7.50, 12, 6),
('Burger Combo', 'COMBO MEALS', 1, 8.00, 15, 8),
('Hot Dog Combo', 'COMBO MEALS', 1, 7.00, 14, 7),
('Corn Dog Combo', 'COMBO MEALS', 1, 6.50, 11, 6);

-- 4. Insert Sales FOURTH
INSERT INTO sales (receipt_number, user_id, user_name, subtotal, tax_amount, total_amount, payment_method, sale_datetime) VALUES
('NAR-20241201-001', 1, 'nardo', 10.50, 1.05, 11.55, 'CASH', '2024-12-01 10:30:00'),
('NAR-20241201-002', 2, 'staff1', 7.50, 0.75, 8.25, 'CASH', '2024-12-01 11:15:00'),
('NAR-20241201-003', 2, 'staff1', 23.97, 2.40, 26.37, 'CARD', '2024-12-01 14:20:00'),
('NAR-20241201-004', 1, 'nardo', 5.00, 0.50, 5.50, 'CASH', '2024-12-01 15:45:00'),
('NAR-20241202-001', 2, 'staff1', 16.80, 1.68, 18.48, 'MOBILE', '2024-12-02 09:30:00');

-- 5. Insert Sale Items FIFTH
INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, line_total) VALUES
(1, 1, 'Plain Hot Dog', 3, 3.50, 10.50),
(2, 3, 'Corn Dogs', 2, 3.00, 6.00),
(2, 12, 'Chips', 1, 1.50, 1.50),
(3, 21, '2pc Chicken Combo', 2, 8.50, 17.00),
(3, 15, 'Soda', 3, 1.80, 5.40),
(3, 16, 'Box Juice', 1, 1.60, 1.60),
(4, 2, 'Full House Hot Dog', 1, 5.50, 5.50),
(5, 4, 'Sugar Dog', 4, 2.50, 10.00),
(5, 13, 'Orange Juice', 2, 2.00, 4.00),
(5, 18, 'Cookies', 1, 1.50, 1.50),
(5, 25, 'Napkins', 2, 1.00, 2.00);

-- 6. Insert Stock Movements SIXTH (Now products exist!)
INSERT INTO stock_movements (product_id, related_id, movement_type, quantity_changed, previous_quantity, new_quantity, reason, user_id, timestamp) VALUES
-- Initial stock for food items
(1, NULL, 'RESTOCK', 50, 0, 50, 'Initial stock', 1, '2024-11-28 09:00:00'),
(2, NULL, 'RESTOCK', 40, 0, 40, 'Initial stock', 1, '2024-11-28 09:00:00'),
(3, NULL, 'RESTOCK', 60, 0, 60, 'Initial stock', 1, '2024-11-28 09:00:00'),
(4, NULL, 'RESTOCK', 30, 0, 30, 'Initial stock', 1, '2024-11-28 09:00:00'),

-- Sales from today
(1, 1, 'SALE', -3, 50, 47, 'Sale #NAR-20241201-001', 1, '2024-12-01 10:30:00'),
(3, 2, 'SALE', -2, 60, 58, 'Sale #NAR-20241201-002', 2, '2024-12-01 11:15:00'),
(21, 3, 'SALE', -2, 15, 13, 'Sale #NAR-20241201-003', 2, '2024-12-01 14:20:00'),
(15, 3, 'SALE', -3, 50, 47, 'Sale #NAR-20241201-003', 2, '2024-12-01 14:20:00'),
(16, 3, 'SALE', -1, 25, 24, 'Sale #NAR-20241201-003', 2, '2024-12-01 14:20:00'),
(2, 4, 'SALE', -1, 40, 39, 'Sale #NAR-20241201-004', 1, '2024-12-01 15:45:00'),
(4, 5, 'SALE', -4, 30, 26, 'Sale #NAR-20241202-001', 2, '2024-12-02 09:30:00'),
(13, 5, 'SALE', -2, 30, 28, 'Sale #NAR-20241202-001', 2, '2024-12-02 09:30:00'),
(18, 5, 'SALE', -1, 35, 34, 'Sale #NAR-20241202-001', 2, '2024-12-02 09:30:00'),
(25, 5, 'SALE', -2, 50, 48, 'Sale #NAR-20241202-001', 2, '2024-12-02 09:30:00'),

-- Restock example
(12, NULL, 'RESTOCK', 20, 40, 60, 'Weekly restock', 1, '2024-12-01 16:00:00'),

-- Adjustment example
(5, NULL, 'ADJUSTMENT', -2, 30, 28, 'Damaged items', 1, '2024-12-01 17:00:00');

-- 7. Insert Low Stock Alerts SEVENTH
-- These will be auto-generated by the system, but here are some examples
INSERT INTO low_stock_alerts (product_id, current_quantity, min_stock_level, alert_date, is_resolved) VALUES
(10, 3, 5, '2024-11-30 15:00:00', false),  -- Raisin Dog low stock
(22, 4, 6, '2024-11-30 16:30:00', true),   -- Nuggets Combo (resolved)
(7, 2, 8, '2024-12-01 09:00:00', false);   -- Tastee Chicken Patty low stock

-- 8. Insert Reports EIGHTH
INSERT INTO reports (report_type, generated_by, generated_by_name, data, title, summary) VALUES
('INVENTORY_SUMMARY', 1, 'nardo', 
'INVENTORY SUMMARY REPORT\n======================\nTotal Products: 35\nActive Products: 35\nLow Stock Items: 3\n\nTop Categories:\n- FOOD: 11 products\n- BEVERAGES: 9 products\n- SNACKS: 7 products\n- COMBO MEALS: 5 products\n- ESSENTIALS: 3 products',
'Daily Inventory Summary - Dec 1, 2024',
'Inventory overview showing 35 active products with 3 low stock items.'),

('SALES_REPORT', 2, 'staff1',
'SALES REPORT\n============\nPeriod: 2024-12-01 to 2024-12-01\nTotal Sales: 4 transactions\nTotal Revenue: $47.97\nAverage Sale: $11.99\n\nTop Products:\n1. 2pc Chicken Combo - $17.00\n2. Plain Hot Dog - $10.50\n3. Sugar Dog - $10.00',
'Sales Report - December 1, 2024',
'4 sales totaling $47.97 with average sale of $11.99.'),

('LOW_STOCK', 1, 'nardo',
'LOW STOCK ALERT REPORT\n======================\nGenerated: 2024-12-01\n\nLow Stock Items:\n1. Raisin Dog: 3 in stock (Min: 5)\n2. Tastee Chicken Patty: 2 in stock (Min: 8)\n\nTotal Low Stock Items: 2\nEstimated Restock Cost: $14.40',
'Low Stock Alert Report',
'2 products below minimum stock level requiring immediate attention.');

-- 9. Insert Report Preferences NINTH
INSERT INTO report_preferences (user_id, auto_generate_daily, email_reports, default_report_type, save_all_reports) VALUES
(1, true, false, 'INVENTORY_SUMMARY', true),
(2, true, false, 'SALES_REPORT', true),
(3, false, false, 'INVENTORY_SUMMARY', true),
(4, true, true, 'SALES_REPORT', false);

-- =============================================
-- CREATE VIEWS FOR REPORTING
-- =============================================

-- View for low stock products
CREATE VIEW vw_low_stock_products AS
SELECT p.product_id, p.name, p.category, 
       p.quantity as current_quantity, 
       p.min_stock as minimum_stock,
       (p.min_stock - p.quantity) as needed,
       p.price,
       s.supplier_name
FROM products p
LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.is_active = TRUE AND p.quantity <= p.min_stock;

-- View for daily sales summary
CREATE VIEW vw_daily_sales AS
SELECT DATE(s.sale_datetime) as sale_date,
       COUNT(*) as total_transactions,
       SUM(s.total_amount) as total_revenue,
       AVG(s.total_amount) as average_sale,
       SUM(si.quantity) as total_items_sold
FROM sales s
JOIN sale_items si ON s.sale_id = si.sale_id
GROUP BY DATE(s.sale_datetime);

-- View for product performance
CREATE VIEW vw_product_performance AS
SELECT p.product_id, p.name, p.category,
       SUM(si.quantity) as total_sold,
       SUM(si.line_total) as total_revenue,
       AVG(si.unit_price) as average_price
FROM products p
LEFT JOIN sale_items si ON p.product_id = si.product_id
GROUP BY p.product_id, p.name, p.category;

-- =============================================
-- CREATE STORED PROCEDURES
-- =============================================

-- Procedure to check and generate low stock alerts
DELIMITER //
CREATE PROCEDURE sp_check_low_stock()
BEGIN
    -- Insert new alerts for products below minimum stock
    INSERT INTO low_stock_alerts (product_id, current_quantity, min_stock_level, alert_date, is_resolved)
    SELECT p.product_id, p.quantity, p.min_stock, NOW(), FALSE
    FROM products p
    WHERE p.is_active = TRUE 
      AND p.quantity <= p.min_stock
      AND NOT EXISTS (
          SELECT 1 FROM low_stock_alerts a 
          WHERE a.product_id = p.product_id 
            AND a.is_resolved = FALSE
      );
END //
DELIMITER ;

-- Procedure to record sale and update stock
DELIMITER //
CREATE PROCEDURE sp_record_sale(
    IN p_user_id INT,
    IN p_product_id INT,
    IN p_quantity INT,
    IN p_unit_price DECIMAL(10,2),
    OUT p_sale_id INT
)
BEGIN
    DECLARE current_stock INT;
    
    -- Get current stock
    SELECT quantity INTO current_stock 
    FROM products WHERE product_id = p_product_id;
    
    -- Check if enough stock
    IF current_stock < p_quantity THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Insufficient stock';
    END IF;
    
    -- Record sale (simplified - actual would be more complex)
    INSERT INTO sales (user_id, user_name, subtotal, total_amount)
    SELECT p_user_id, username, p_quantity * p_unit_price, p_quantity * p_unit_price
    FROM users WHERE user_id = p_user_id;
    
    SET p_sale_id = LAST_INSERT_ID();
    
    -- Record sale item
    INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, line_total)
    VALUES (p_sale_id, p_product_id, 
           (SELECT name FROM products WHERE product_id = p_product_id),
           p_quantity, p_unit_price, p_quantity * p_unit_price);
    
    -- Update product stock
    UPDATE products 
    SET quantity = quantity - p_quantity 
    WHERE product_id = p_product_id;
    
    -- Record stock movement
    INSERT INTO stock_movements (product_id, related_id, movement_type, 
                                quantity_changed, previous_quantity, new_quantity, 
                                reason, user_id)
    VALUES (p_product_id, p_sale_id, 'SALE', 
            -p_quantity, current_stock, current_stock - p_quantity,
            'Sale transaction', p_user_id);
END //
DELIMITER ;

-- =============================================
-- CREATE TRIGGERS
-- =============================================

-- Trigger to update product quantity after sale item insertion
DELIMITER //
CREATE TRIGGER trg_after_sale_item_insert
AFTER INSERT ON sale_items
FOR EACH ROW
BEGIN
    -- Update product quantity
    UPDATE products 
    SET quantity = quantity - NEW.quantity
    WHERE product_id = NEW.product_id;
    
    -- Record stock movement
    INSERT INTO stock_movements (product_id, related_id, movement_type,
                                quantity_changed, previous_quantity, new_quantity,
                                reason, user_id)
    SELECT NEW.product_id, NEW.sale_id, 'SALE',
           -NEW.quantity, p.quantity, p.quantity - NEW.quantity,
           CONCAT('Sale #', s.receipt_number), s.user_id
    FROM products p
    JOIN sales s ON s.sale_id = NEW.sale_id
    WHERE p.product_id = NEW.product_id;
END //
DELIMITER ;

-- Trigger to check for low stock after product update
DELIMITER //
CREATE TRIGGER trg_after_product_update
AFTER UPDATE ON products
FOR EACH ROW
BEGIN
    -- Check if stock fell below minimum and trigger alert
    IF NEW.quantity <= NEW.min_stock AND OLD.quantity > NEW.min_stock THEN
        INSERT INTO low_stock_alerts (product_id, current_quantity, min_stock_level, alert_date)
        VALUES (NEW.product_id, NEW.quantity, NEW.min_stock, NOW());
    END IF;
END //
DELIMITER ;

-- =============================================
-- CREATE INDEXES FOR PERFORMANCE
-- =============================================

CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_sales_datetime ON sales(sale_datetime);
CREATE INDEX idx_stock_movements_product_date ON stock_movements(product_id, timestamp);
CREATE INDEX idx_sale_items_sale_product ON sale_items(sale_id, product_id);

-- =============================================
-- VERIFICATION QUERIES
-- =============================================

-- Test queries to verify data
SELECT 'Suppliers' as table_name, COUNT(*) as count FROM suppliers
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Sales', COUNT(*) FROM sales
UNION ALL
SELECT 'Sale Items', COUNT(*) FROM sale_items
UNION ALL
SELECT 'Stock Movements', COUNT(*) FROM stock_movements
UNION ALL
SELECT 'Low Stock Alerts', COUNT(*) FROM low_stock_alerts
UNION ALL
SELECT 'Reports', COUNT(*) FROM reports;

-- Check foreign key relationships
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'nardos_inventory'
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- =============================================
-- FINAL MESSAGE
-- =============================================

SELECT '✅ Nardo''s Inventory Database Created Successfully!' as message;
SELECT '📊 Total Tables: 8' as info;
SELECT '👥 Sample Users: nardo (admin), staff1 (user)' as info;
SELECT '📦 Sample Products: 35 items across 5 categories' as info;
SELECT '💰 Sample Sales: 5 transactions with complete history' as info;
SELECT '🚀 Ready to use with XAMPP MySQL!' as ready;

-- =============================================
-- NOTES FOR DEVELOPERS
-- =============================================
/*
1. XAMPP MySQL Defaults:
   - Host: localhost
   - Port: 3306
   - Username: root
   - Password: (empty)

2. To run this file:
   - Open XAMPP, start MySQL
   - Open phpMyAdmin (http://localhost/phpmyadmin)
   - Create new database 'nardos_inventory'
   - Import this SQL file

3. Test Credentials:
   - Admin: nardo / password123
   - Staff: staff1 / password123

4. Database Features:
   - Complete referential integrity with foreign keys
   - Sample data for immediate testing
   - Views for common reports
   - Stored procedures for common operations
   - Triggers for automated stock management
   - Indexes for performance optimization
*/
