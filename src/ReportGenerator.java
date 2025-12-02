import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportGenerator {
    // SRS 1.6: Generate inventory and transaction reports
    // SRS 3.1: Reports complete in under 10 seconds
    
    private ReportDAO reportDAO;
    
    public ReportGenerator() {
        this.reportDAO = new ReportDAO();
    }
    
    public Report generateInventoryReport() {
        LocalDate now = LocalDate.now();
        // SRS 1.6: Summary of all items' transactions
        String inventoryData = reportDAO.fetchInventorySummary();
        return new Report(reportDAO.getNextReportId(), InventoryReportType.INVENTORY_SUMMARY, 
                         now, 0, inventoryData);
    }
    
    public Report generateTransactionReport(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        // SRS 1.5: Transaction history for audit purposes
        String transactionData = reportDAO.fetchTransactionsByDateRange(startDate, endDate);
        return new Report(reportDAO.getNextReportId(), InventoryReportType.TRANSACTION_HISTORY, 
                         now, 0, transactionData);
    }
    
    public Report generateSalesReport() {
        LocalDate now = LocalDate.now();
        // Track sales data for business analysis
        String salesData = reportDAO.fetchSalesData();
        return new Report(reportDAO.getNextReportId(), InventoryReportType.SALES_REPORT, 
                         now, 0, salesData);
    }
    
    public Report generateLowStockReport() {
        LocalDate now = LocalDate.now();
        // SRS 1.2: Alert staff when product quantity is low
        String lowStockData = reportDAO.fetchLowStockItems();
        return new Report(reportDAO.getNextReportId(), InventoryReportType.LOW_STOCK, 
                         now, 0, lowStockData);
    }
    
    // SRS 1.6: Ask user whether to view summary table before exiting
    public boolean promptForSummary() {
        // In actual implementation, this would show a GUI dialog
        System.out.println("Would you like to view the summary table before exiting? (yes/no)");
        // For demo purposes, returning true
        return true;
    }
    
    // SRS 1.6: Generate printable track of all items' transactions
    public String generatePrintableSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("NARDO'S INVENTORY SUMMARY REPORT\n");
        summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ISO_DATE)).append("\n");
        summary.append("=========================================\n");
        summary.append(reportDAO.fetchAllTransactionsForPrint());
        return summary.toString();
    }
}