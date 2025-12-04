/**
 * Stock and Alert Management Layer - SRS Component: 1.5 Low Stock Alerts
 * 
 * This package manages inventory tracking, low stock detection, and alert
 * notifications for proactive inventory management.
 * 
 * SRS Requirement 1.5 Coverage:
 * - Trigger alerts when product quantity falls below minimum stock level
 * - Display low stock alerts in main dashboard
 * - Retrieve unresolved alerts for inventory review
 * - Mark alerts as resolved when stock is replenished
 * - Prevent duplicate alerts for the same product
 * - Auto-generated alerts from stock movements (no manual entry)
 * - Alert resolution timestamp tracking
 * 
 * Architecture: Monitoring and Notification Layer
 * - StockMovement: Entity tracking all inventory changes
 * - StockMovementDAO: Persistence for stock movement audit trail
 * - StockMovementService: Core business logic for inventory management
 * - Alert: Entity representing low stock alerts
 * - AlertDAO: Database operations for alerts
 * - AlertManager: Logic for alert creation and resolution
 * - LowStockAlert: Specialized alert type for threshold breaches
 * - StockNotificationService: Detects and triggers alerts
 * 
 * Alert Management Flow:
 * 1. Stock movement occurs (sales, restocking, manual adjustment)
 * 2. StockMovementService updates product quantity
 * 3. StockNotificationService checks if quantity < minimum stock
 * 4. If below threshold and no unresolved alert exists, create new alert
 * 5. Alert appears in dashboard for visibility
 * 6. User acknowledges/resolves alert when stock is replenished
 * 7. Resolved alert remains in history for audit purposes
 * 
 * Key Business Rules:
 * - Only one unresolved alert per product at any time
 * - Alerts cannot be manually created (system-generated only)
 * - Alert resolution requires manual acknowledgment (prevents automation errors)
 * - All alerts include timestamp and associated product information
 * - Alerts persist across sessions (not session-dependent)
 * - Alert history is permanent for audit trail
 * 
 * Stock Movement Types:
 * - SALE: Inventory deduction from sales
 * - RESTOCK: Inventory increase from supplier
 * - ADJUSTMENT: Manual quantity corrections
 * - RETURN: Customer returns of products
 * 
 * Performance Requirements:
 * - Alert detection occurs immediately after stock change
 * - Alert display updates within 1 second
 * - Alert retrieval from database within 500ms
 */
package stock;
