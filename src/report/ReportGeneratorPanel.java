package report;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class ReportGeneratorPanel extends JPanel {
    private ReportGenerator reportGenerator;
    private int userId;
    private String userName;

    public ReportGeneratorPanel(Connection connection, int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.reportGenerator = new ReportGenerator(connection);

        setLayout(new BorderLayout());
        add(new JLabel("<html><center><h1>Report Generator</h1>" +
                "<p>Generate comprehensive inventory and sales reports</p></center></html>",
                SwingConstants.CENTER), BorderLayout.CENTER);
    }

    public void refreshData() {
        // Refresh report data
    }

    public void generateInventoryReport(int userId) throws SQLException {
        try {
            String report = String.valueOf(reportGenerator.generateInventorySummary(userId));
            showReport("Inventory Report", report);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public void generateSalesReport(int userId) throws SQLException {
        try {
            String report = String.valueOf(reportGenerator.generateSalesReport(
                java.time.LocalDate.now().minusDays(7),
                java.time.LocalDate.now(),
                userId
            ));
            showReport("Sales Report", report);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public void generateTransactionReport() {
        try {
            String report = String.valueOf(reportGenerator.generateTransactionHistory(
                java.time.LocalDate.now().minusDays(7),
                java.time.LocalDate.now(),
                userId
            ));
            showReport("Transaction Report", report);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public void openCustomReport() {
        JOptionPane.showMessageDialog(this, "Custom Report Generator functionality");
    }

    private void showReport(String title, String content) {
        JTextArea reportArea = new JTextArea(content);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
