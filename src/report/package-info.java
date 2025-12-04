/**
 * Report Generation Layer - SRS Component: 1.6 Reports
 * 
 * This package generates various reports for inventory analysis and business
 * decision-making.
 * 
 * SRS Requirement 1.6 Coverage:
 * - Generate Sales Summary Report (daily/monthly totals and statistics)
 * - Generate Inventory Report (current stock levels and minimum stock analysis)
 * - Generate Low Stock Report (products below minimum threshold)
 * - Generate Exit Summary (activity summary when user logs out)
 * - Display reports within 10 seconds
 * - Support date range filtering for historical analysis
 * - Include charts or detailed tables in reports
 * - Export report data if needed
 * 
 * Architecture: Reporting and Analysis Layer
 * - Report: Base entity for report data
 * - ReportConfig: Configuration for report generation (filters, date ranges)
 * - ReportDAO: Data retrieval for reports
 * - ReportGenerator: Core logic for report calculation and assembly
 * - ReportGeneratorForm: User interface for report customization
 * - ReportGeneratorPanel: Report display and management
 * - ReportMenu: Navigation for different report types
 * - ExitSummaryDialog: Exit report showing user activity
 * - InventoryReportType: Enum for report categories
 * 
 * Supported Report Types:
 * - Sales Summary: Total sales, item count, revenue by date
 * - Inventory Status: Product list with current quantities and status
 * - Low Stock Alert: Products below minimum threshold (actionable list)
 * - Exit Summary: Activity metrics for current session
 * 
 * Report Components:
 * - Report Header: Title, generation timestamp, user information
 * - Report Body: Detailed data in table or chart format
 * - Report Footer: Summary statistics and totals
 * - Filtering Options: Date range, product category, stock status
 * 
 * Data Source:
 * - Sales data from SaleDAO (transactions and revenue)
 * - Product data from ProductDAO (inventory levels)
 * - Stock movement data from StockMovementDAO (audit trail)
 * - Alert data from AlertDAO (current issues)
 * - User session data from SessionManager (activity tracking)
 * 
 * Key Business Rules:
 * - Reports are read-only (no modifications)
 * - All data is calculated at report generation time (not cached)
 * - Reports include data from the database at report time
 * - Exit summary is mandatory when logging out
 * - Only OWNER can view all reports
 * - STAFF can view limited reports (with their data only)
 * 
 * Performance Requirements:
 * - Sales Summary Report: < 10 seconds
 * - Inventory Report: < 10 seconds
 * - Low Stock Report: < 5 seconds
 * - Exit Summary: < 5 seconds
 * - Date range queries: < 20 seconds for 1 year of data
 */
package report;
