/**
 * Sales Processing Layer - SRS Component: 1.4 Sales Processing
 * 
 * This package handles sales transactions, payment processing, and real-time
 * inventory deduction during point-of-sale operations.
 * 
 * SRS Requirement 1.4 Coverage:
 * - Record sales transactions with product list and quantities
 * - Calculate totals and apply discounts if applicable
 * - Support multiple payment methods (Cash, Card)
 * - Generate receipts with transaction details
 * - Real-time inventory deduction upon sale completion
 * - Prevent sales exceeding available stock
 * - Automatic low stock alerts after inventory deduction
 * - Track sales by date range for reporting
 * 
 * Architecture: Transaction Processing Layer
 * - Sale: Entity representing a sales transaction
 * - SaleItem: Entity representing individual items in a sale
 * - Payment: Entity for payment information
 * - SaleDAO: Database operations for sales
 * - SalesProcessor: Core business logic for transaction processing
 * - SalesProcessingForm: GUI for point-of-sale operations
 * - SalesPanel: Dashboard for sales statistics and quick sales
 * - SalesStatistics: Data holder for sales metrics
 * - SalesHistoryForm: View past sales transactions
 * 
 * Transaction Flow:
 * 1. User selects products and quantities in SalesProcessingForm
 * 2. System validates stock availability for each item
 * 3. User enters payment information (method, amount)
 * 4. SalesProcessor validates the complete transaction
 * 5. Sale is recorded in database with timestamp and user ID
 * 6. Inventory is deducted for each product
 * 7. Stock movements are logged
 * 8. Low stock alerts are triggered if minimums are breached
 * 9. Receipt is generated and displayed
 * 10. Sales statistics are updated in dashboard
 * 
 * Key Business Rules:
 * - Sales cannot be completed without payment information
 * - Inventory must be sufficient before processing
 * - All sales are immutable after completion (audit trail)
 * - Receipt includes itemized list, subtotal, tax (if applicable), total
 * - Only OWNER and STAFF can process sales
 * - Stock deduction is atomic (all-or-nothing)
 * 
 * Performance Requirements:
 * - Sales processing completes within 5 seconds
 * - Stock validation occurs within 1 second
 * - Receipt generation within 2 seconds
 */
package sale;
