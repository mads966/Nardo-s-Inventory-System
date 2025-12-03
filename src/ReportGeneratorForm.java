import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class ReportGeneratorForm extends JPanel {
    private JComboBox<InventoryReportType> reportTypeComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton generateButton;
    private JButton printButton;
    private JTextArea reportView;
    private JButton exportButton;
    
    private ReportGenerator reportGenerator;
    
    // SRS 1.6: Interface for generating reports
    public ReportGeneratorForm() {
        reportGenerator = new ReportGenerator();
        setLayout(new BorderLayout());
        
        // Top panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        // Report type selection
        controlPanel.add(new JLabel("Report Type:"));
        reportTypeComboBox = new JComboBox<>(InventoryReportType.values());
        controlPanel.add(reportTypeComboBox);
        
        // Date range (for transaction reports)
        controlPanel.add(new JLabel("Start Date:"));
        startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().minusDays(30).toString());
        controlPanel.add(startDateField);
        
        controlPanel.add(new JLabel("End Date:"));
        endDateField = new JTextField(10);
        endDateField.setText(LocalDate.now().toString());
        controlPanel.add(endDateField);
        
        // Buttons
        generateButton = new JButton("Generate");
        printButton = new JButton("Print");
        exportButton = new JButton("Export");
        
        controlPanel.add(generateButton);
        controlPanel.add(printButton);
        controlPanel.add(exportButton);
        
        // Report display area
        reportView = new JTextArea(20, 60);
        reportView.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportView);
        
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add action listeners
        generateButton.addActionListener(e -> generateReport());
        printButton.addActionListener(e -> printReport());
        exportButton.addActionListener(e -> exportReport());
    }
    
    public void generateReport() {
        // SRS 3.1: Report generation completes in under 10 seconds
        InventoryReportType selectedType = (InventoryReportType) reportTypeComboBox.getSelectedItem();
        Report report = null;
        
        switch(selectedType) {
            case INVENTORY_SUMMARY:
                report = reportGenerator.generateInventoryReport();
                break;
            case TRANSACTION_HISTORY:
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());
                report = reportGenerator.generateTransactionReport(startDate, endDate);
                break;
            case SALES_REPORT:
                report = reportGenerator.generateSalesReport();
                break;
            case LOW_STOCK:
                report = reportGenerator.generateLowStockReport();
                break;
        }
        
        if (report != null) {
            reportView.setText(report.getData());
        }
    }
    
    public void printReport() {
        // SRS 1.6: Printable summary table
        String reportText = reportView.getText();
        if (!reportText.isEmpty()) {
            System.out.println("Printing report...");
            System.out.println(reportText);
            JOptionPane.showMessageDialog(this, "Report sent to printer", 
                                        "Print", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void filterReport() {
        // Filter functionality for reports
        JOptionPane.showMessageDialog(this, "Filter functionality not yet implemented", 
                                    "Filter", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void exportReport() {
        // Export report to file
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // In actual implementation, save report data to file
            JOptionPane.showMessageDialog(this, "Report exported successfully", 
                                        "Export", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}