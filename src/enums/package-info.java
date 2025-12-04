/**
 * Supporting Infrastructure Layer - Utilities and Configuration
 * 
 * This package contains utility classes and shared infrastructure components.
 * 
 * SRS Components:
 * - Enums: Category and MovementType - Support for product categorization
 *   and inventory tracking
 * - Supplies: Supplier and SupplierDAO - (Optional enhancement layer)
 *   Supporting supplier information management for future extensions
 * - Alert: AlertDAO - Supports SRS 1.5 Low Stock Alerts functionality
 * 
 * Component Descriptions:
 * 
 * ENUMS PACKAGE:
 * - Category: Product categorization (FOOD, BEVERAGES, SNACKS, ESSENTIALS, COMBO_MEALS)
 * - MovementType: Stock movement types (SALE, RESTOCK, ADJUSTMENT, RETURN)
 * 
 * ALERT PACKAGE:
 * - AlertDAO: Manages alert persistence and retrieval
 * - AlertManager: Alert creation and lifecycle management
 * - LowStockAlert: Specialized alert for inventory threshold breaches
 * 
 * SUPPLIES PACKAGE (Optional):
 * - Supplier: Entity for supplier information
 * - SupplierDAO: Database operations for suppliers
 * Note: Supplier management is not a core SRS requirement but is included
 * for future enhancements and data consistency.
 */
package utilities;
