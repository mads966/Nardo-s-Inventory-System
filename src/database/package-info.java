/**
 * Database Layer - SRS Component: 2.3.2 Database Management
 * 
 * This package manages all database connectivity and JDBC operations for the
 * Nardo's Inventory Management System.
 * 
 * SRS Requirement Coverage:
 * - Requirement 1.1 (Authentication): Stores and retrieves user credentials with encrypted passwords
 * - Requirement 1.2 (Product Management): CRUD operations for products and categories
 * - Requirement 1.3 (Stock Management): Records stock movements and product quantities
 * - Requirement 1.4 (Sales Processing): Stores sales transactions and payment records
 * - Requirement 1.5 (Alerts): Manages low stock alerts
 * - Requirement 1.6 (Reports): Stores transaction history for report generation
 * 
 * Architecture: Layered Architecture (Database Access Layer)
 * - Connection pooling via DBManager
 * - DAO pattern for data access (UserDAO, ProductDAO, SaleDAO, etc.)
 * - Prepared statements for SQL injection prevention
 * 
 * Key Classes:
 * - DBManager: Manages database connection lifecycle
 * - UserDAO: CRUD operations for users
 * - ProductDAO: CRUD operations for products
 * - SaleDAO: CRUD operations for sales transactions
 * - StockMovementDAO: Records inventory movements
 * - AlertDAO: Manages low stock alerts
 * - ReportDAO: Provides data for reports
 * 
 * Note: All SQL queries use prepared statements to prevent injection attacks.
 * All connection management follows singleton pattern for efficiency.
 */
package database;
