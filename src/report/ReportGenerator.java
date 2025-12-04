package report;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {
    private final ReportDAO reportDAO;

    public static int nextReportId = 0;

    public static int getNextReportId() {
        return nextReportId++;
    }

    public ReportGenerator(Connection connection) {
        this.reportDAO = new ReportDAO(connection);
    }

    public static Report createReport(InventoryReportType type, int user, String data, String title, String summary) {
        return new Report(getNextReportId(), user, type, data, title, summary);
    }

    // SRS 1.6: Generate inventory summary report
    public Report generateInventorySummary(int userId) throws Exception {
        try {
            String data = reportDAO.generateInventorySummaryData();
            String title = "Inventory Summary Report - " + LocalDate.now();

            // Create summary statistics
            String summary = extractInventorySummary(data);

            Report report = createReport(
                InventoryReportType.INVENTORY_SUMMARY,
                userId, data, title, summary
            );
            report.setSummary(summary);

            // Save to database
            reportDAO.saveReport(report);

            return report;
        } catch (Exception e) {
            throw new Exception("Failed to generate inventory summary: " + e.getMessage(), e);
        }
    }

    // SRS 1.6: Generate sales report
    public Report generateSalesReport(LocalDate startDate, LocalDate endDate, int userId) throws Exception {
        try {
            String data = reportDAO.generateSalesReportData(startDate, endDate);
            String title = String.format("Sales Report - %s to %s",
                    startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            // Extract summary from data
            String summary = extractSalesSummary(data);

            Report report = createReport(
                InventoryReportType.SALES_REPORT,
                userId,
                data,
                title,
                ""
            );
            report.setSummary(summary);

            // Save to database
            reportDAO.saveReport(report);

            return report;
        } catch (Exception e) {
            throw new Exception("Failed to generate sales report: " + e.getMessage(), e);
        }
    }

    // SRS 1.2: Generate low stock report
    public Report generateLowStockReport(int userId) throws Exception {
        try {
            String data = reportDAO.generateLowStockReportData();
            String title = "Low Stock Alert Report - " + LocalDate.now();

            // Extract summary from data
            String summary = extractLowStockSummary(data);

            Report report = createReport(
                InventoryReportType.LOW_STOCK,
                userId,
                data,
                title,
                ""
            );
            report.setSummary(summary);

            // Save to database
            reportDAO.saveReport(report);

            return report;
        } catch (Exception e) {
            throw new Exception("Failed to generate low stock report: " + e.getMessage(), e);
        }
    }

    // SRS 1.5: Generate transaction history report
    public Report generateTransactionHistory(LocalDate startDate, LocalDate endDate, int userId) throws Exception {
        try {
            String data = reportDAO.generateTransactionHistoryData(startDate, endDate);
            String title = String.format("Transaction History - %s to %s",
                    startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            String summary = extractTransactionSummary(data); // Extract summary from data

            Report report = createReport(
                InventoryReportType.TRANSACTION_HISTORY,
                userId,
                data,
                title,
                ""
            );
            report.setSummary(summary);

            reportDAO.saveReport(report); // Save to database

            return report;
        } catch (Exception e) {
            throw new Exception("Failed to generate transaction history: " + e.getMessage(), e);
        }
    }

    // Generate custom report
    public Report generateCustomReport(String title, String data, InventoryReportType type, int userId, String userName) throws Exception {
        try {
            Report report = createReport(type, userId, data, title, "");
            reportDAO.saveReport(report);
            return report;
        } catch (Exception e) {
            throw new Exception("Failed to generate custom report: " + e.getMessage(), e);
        }
    }

    // Get all saved reports
    public List<Report> getAllReports() throws Exception {
        try {
            return reportDAO.getAllReports();
        } catch (Exception e) {
            throw new Exception("Failed to get reports: " + e.getMessage(), e);
        }
    }

    // Get report by ID
    public Report getReportById(int reportId) throws Exception {
        try {
            return reportDAO.getReportById(reportId);
        } catch (Exception e) {
            throw new Exception("Failed to get report: " + e.getMessage(), e);
        }
    }

    // Delete report
    public boolean deleteReport(int reportId) throws Exception {
        try {
            return reportDAO.deleteReport(reportId);
        } catch (Exception e) {
            throw new Exception("Failed to delete report: " + e.getMessage(), e);
        }
    }

    // SRS 1.6: Ask user if they want to view summary before exiting
    public boolean shouldShowExitSummary() {
        // This would typically show a GUI dialog
        // For now, we return true to always show summary
        return true;
    }

    // SRS 1.6: Generate exit summary
    public String generateExitSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("DAILY ACTIVITY SUMMARY\n");
        summary.append("----------------------\n");
        summary.append("Date: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        try {
            // Get today's sales
            LocalDate today = LocalDate.now();
            String salesData = reportDAO.generateSalesReportData(today, today);
            summary.append("Today's Sales Overview:\n");
            summary.append(extractDailySummary(salesData)).append("\n");

            // Get current low stock items
            String lowStockData = reportDAO.generateLowStockReportData();
            summary.append("Current Low Stock Items:\n");
            summary.append(extractLowStockCount(lowStockData)).append("\n");

        } catch (Exception e) {
            summary.append("Error generating summary: ").append(e.getMessage()).append("\n");
        }

        return summary.toString();
    }

    // Helper methods to extract summaries from report data
    private String extractInventorySummary(String data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder summary = new StringBuilder();
        String[] lines = data.split("\n");

        for (String line : lines) {
            if (line.contains("Total Products:") ||
                line.contains("Active Products:") ||
                line.contains("Low Stock Items:") ||
                line.contains("OUT OF STOCK")) {
                summary.append(line).append("\n");
            }
        }

        return summary.toString();
    }

    private String extractSalesSummary(String data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder summary = new StringBuilder();
        String[] lines = data.split("\n");

        for (String line : lines) {
            if (line.contains("TOTAL") || line.contains("Period:")) {
                summary.append(line).append("\n");
            }
        }

        return summary.toString();
    }

    private String extractLowStockSummary(String data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder summary = new StringBuilder();
        String[] lines = data.split("\n");

        for (String line : lines) {
            if (line.contains("Total Low Stock Items:") ||
                    line.contains("Estimated Restock Cost:")) {
                summary.append(line).append("\n");
            }
        }

        return summary.toString();
    }

    private String extractTransactionSummary(String data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder summary = new StringBuilder();
        String[] lines = data.split("\n");

        for (String line : lines) {
            if (line.contains("Total Transactions:") ||
                    line.contains("Total Items Added:") ||
                    line.contains("Total Items Removed:") ||
                    line.contains("Net Change:")) {
                summary.append(line).append("\n");
            }
        }

        return summary.toString();
    }

    private String extractDailySummary(String salesData) {
        if (salesData == null || salesData.isEmpty()) return "No sales data available";

        String[] lines = salesData.split("\n");
        for (String line : lines) {
            if (line.contains("TOTAL")) {
                return line.trim();
            }
        }
        return "No sales today";
    }

    private String extractLowStockCount(String lowStockData) {
        if (lowStockData == null || lowStockData.isEmpty()) return "No low stock items";

        String[] lines = lowStockData.split("\n");
        for (String line : lines) {
            if (line.contains("Total Low Stock Items:")) {
                return line.trim();
            }
        }
        return "0 low stock items";
    }

    // Performance check - SRS 3.1: Reports under 10 seconds
    public boolean checkReportPerformance() {
        long startTime = System.currentTimeMillis();
        try {
            // Generate a quick test report
            generateLowStockReport(-1); // -1 means system
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            return duration < 10000; // 10 seconds
        } catch (Exception e) {
            return false;
        }
    }
}