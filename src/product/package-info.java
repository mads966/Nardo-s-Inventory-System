/**
 * Product Management Layer - SRS Components: 1.2 Product Management, 1.3 Stock Management
 * 
 * This package manages product information, inventory levels, and business logic
 * for the inventory system.
 * 
 * SRS Requirements Coverage:
 * 
 * Requirement 1.2 - Product Management:
 * - Add new products with name, price, quantity, category, minimum stock level
 * - Deactivate products (soft delete) instead of permanent deletion
 * - Search products by name, ID, or category
 * - Filter products by active/inactive status
 * - Display product information in GUI tables
 * - Validate all product inputs (name, price, quantity, minimum stock)
 * - Prevent negative prices or quantities
 * 
 * Requirement 1.3 - Stock Management:
 * - Track inventory levels for each product
 * - Log all stock movements (sales, restocking, adjustments)
 * - Enforce minimum stock levels
 * - Generate low stock alerts automatically
 * - Audit trail for all inventory transactions
 * 
 * Architecture: Business Logic Layer with DAO Support
 * - Product: Entity class representing product data
 * - ProductDAO: Database CRUD operations
 * - ProductService: Business logic and validation
 * - ProductManager: Higher-level operations (search, filter, reporting)
 * - InputValidator: Input validation and sanitization
 * - ProductManagementForm: GUI for product operations (role-based access)
 * - StockMovement: Entity for inventory transaction tracking
 * - StockMovementDAO: Persistence for stock movements
 * - StockMovementService: Stock level management logic
 * 
 * Key Business Rules:
 * - Product names must be unique
 * - Prices cannot be negative
 * - Minimum stock must not exceed current quantity
 * - Deactivated products remain in history for audit trail
 * - All stock changes are logged with timestamp and user ID
 * - Only OWNER role can add/deactivate products
 * - STAFF role can only view and search products
 */
package product;
