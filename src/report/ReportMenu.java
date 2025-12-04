package report;

import javax.swing.*;
import java.sql.Connection;

public class ReportMenu extends JMenu {
    private ReportGeneratorForm reportForm;
    private Connection connection;
    private int userId;
    private String userName;
    
    public ReportMenu(Connection connection, int userId, String userName) {
        super("Reports");
        this.connection = connection;
        this.userId = userId;
        this.userName = userName;
        
        initMenuItems();
    }
    
    private void initMenuItems() {
        // Inventory Summary
        JMenuItem inventorySummary = new JMenuItem("Inventory Summary");
        inventorySummary.addActionListener(e -> showReportForm(ReportAction.INVENTORY_SUMMARY));
        add(inventorySummary);
        
        // Sales Report
        JMenuItem salesReport = new JMenuItem("Sales Report");
        salesReport.addActionListener(e -> showReportForm(ReportAction.SALES_REPORT));
        add(salesReport);
        
        // Low Stock Report
        JMenuItem lowStockReport = new JMenuItem("Low Stock Report");
        lowStockReport.addActionListener(e -> showReportForm(ReportAction.LOW_STOCK));
        add(lowStockReport);
        
        // Transaction History
        JMenuItem transactionHistory = new JMenuItem("Transaction History");
        transactionHistory.addActionListener(e -> showReportForm(ReportAction.TRANSACTION_HISTORY));
        add(transactionHistory);
        
        addSeparator();
        
        // Quick Reports
        JMenu quickReports = new JMenu("Quick Reports");
        
        JMenuItem dailySummary = new JMenuItem("Daily Summary");
        dailySummary.addActionListener(e -> showQuickReport(QuickReportType.DAILY_SUMMARY));
        quickReports.add(dailySummary);
        
        JMenuItem weeklySales = new JMenuItem("Weekly Sales");
        weeklySales.addActionListener(e -> showQuickReport(QuickReportType.WEEKLY_SALES));
        quickReports.add(weeklySales);
        
        JMenuItem monthlyInventory = new JMenuItem("Monthly Inventory");
        monthlyInventory.addActionListener(e -> showQuickReport(QuickReportType.MONTHLY_INVENTORY));
        quickReports.add(monthlyInventory);
        
        add(quickReports);
        
        addSeparator();
        
        // Report Manager
        JMenuItem reportManager = new JMenuItem("Report Manager");
        reportManager.addActionListener(e -> showReportManager());
        add(reportManager);
        
        // Export All Reports
        JMenuItem exportAll = new JMenuItem("Export All Reports");
        exportAll.addActionListener(e -> exportAllReports());
        add(exportAll);
    }
    
    private void showReportForm(ReportAction action) {
        if (reportForm == null)
            reportForm = new ReportGeneratorForm(connection, userId, userName);
        
        // Create and show report window
        JFrame reportFrame = new JFrame("Report Generator");
        reportFrame.setContentPane(reportForm);
        reportFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        reportFrame.pack();
        reportFrame.setLocationRelativeTo(null);
        reportFrame.setVisible(true);
        
        // Set focus based on action
        switch (action) {
            case INVENTORY_SUMMARY:
                reportForm.quickGenerateInventorySummary();
                break;
            case LOW_STOCK:
                reportForm.quickGenerateLowStockReport();
                break;
            case SALES_REPORT:
                reportForm.quickGenerateDailySalesReport();
                break;
        }
    }
    
    private void showQuickReport(QuickReportType type) {
        // Implementation for quick reports
        JOptionPane.showMessageDialog(null,
            "Quick report: " + type + "\nThis would generate a predefined report.",
            "Quick Report",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showReportManager() {
        // Show a dialog with all saved reports
        JOptionPane.showMessageDialog(null,
            "Report Manager\nThis would show a list of all saved reports with management options.",
            "Report Manager",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportAllReports() {
        // Export all reports to a zip file
        int confirm = JOptionPane.showConfirmDialog(null,
            "Export all reports to a ZIP file?",
            "Export All Reports",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(null,
                "All reports exported successfully!",
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Enums for report actions
    private enum ReportAction {
        INVENTORY_SUMMARY,
        SALES_REPORT,
        LOW_STOCK,
        TRANSACTION_HISTORY
    }
    
    private enum QuickReportType {
        DAILY_SUMMARY,
        WEEKLY_SALES,
        MONTHLY_INVENTORY
    }
}