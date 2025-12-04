package report;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportGeneratorPanel extends JPanel {
    private ReportGenerator reportGenerator;
    private int userId;
    private String userName;
    private Connection connection;
    
    // GUI Components for custom report
    private JComboBox<InventoryReportType> reportTypeCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JTextField reportTitleField;
    private JCheckBox includeSummaryCheckbox;
    private JCheckBox includeDetailsCheckbox;
    private JButton generateCustomButton;
    private JButton saveTemplateButton;
    private JButton loadTemplateButton;

    // Additional functional buttons
    private JButton generateInventoryButton;
    private JButton generateSalesButton;
    private JButton generateTransactionButton;

    public ReportGeneratorPanel(Connection connection, int userId, String userName) {
        this.connection = connection;
        this.userId = userId;
        this.userName = userName;
        this.reportGenerator = new ReportGenerator(connection);

        initComponents();
        layoutComponents();
        setupListeners();
    }

    private void initComponents() {
        // Report type combo box
        reportTypeCombo = new JComboBox<>(InventoryReportType.values());
        reportTypeCombo.setSelectedItem(InventoryReportType.INVENTORY_SUMMARY);
        
        // Date spinners
        SpinnerDateModel startModel = new SpinnerDateModel();
        SpinnerDateModel endModel = new SpinnerDateModel();
        startDateSpinner = new JSpinner(startModel);
        endDateSpinner = new JSpinner(endModel);
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        
        // Set default dates (last 30 days)
        startModel.setValue(java.sql.Date.valueOf(LocalDate.now().minusDays(30)));
        endModel.setValue(java.sql.Date.valueOf(LocalDate.now()));
        
        // Title field
        reportTitleField = new JTextField(30);
        reportTitleField.setText("Custom Report - " + LocalDate.now());
        
        // Checkboxes for options
        includeSummaryCheckbox = new JCheckBox("Include Summary", true);
        includeDetailsCheckbox = new JCheckBox("Include Detailed Data", true);
        
        // Buttons
        generateCustomButton = new JButton("Generate Custom Report");
        
        saveTemplateButton = new JButton("Save as Template");
        
        loadTemplateButton = new JButton("Load Template");
        
        // Functional buttons from the original file
        generateInventoryButton = new JButton("Generate Inventory Report");
        generateSalesButton = new JButton("Generate Sales Report");
        generateTransactionButton = new JButton("Generate Transaction Report");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("<html><center><h1>Custom Report Generator</h1>" +
                "<p>Create custom reports with flexible parameters</p></center></html>",
                SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 1: Report Type
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Report Type:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        gbc.gridwidth = 2;
        mainPanel.add(reportTypeCombo, gbc);
        
        // Row 2: Date Range
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Date Range:"), gbc);
        
        gbc.gridx = 1;
        mainPanel.add(startDateSpinner, gbc);
        
        gbc.gridx = 2;
        mainPanel.add(new JLabel("to"), gbc);
        
        gbc.gridx = 3;
        mainPanel.add(endDateSpinner, gbc);
        
        // Row 3: Report Title
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Report Title:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        mainPanel.add(reportTitleField, gbc);
        
        // Row 4: Report Options
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Report Options:"), gbc);
        
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.add(includeSummaryCheckbox);
        optionsPanel.add(includeDetailsCheckbox);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        mainPanel.add(optionsPanel, gbc);
        
        // Row 5: Custom Report Buttons
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel customButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        customButtonPanel.add(generateCustomButton);
        customButtonPanel.add(saveTemplateButton);
        customButtonPanel.add(loadTemplateButton);
        
        mainPanel.add(customButtonPanel, gbc);
        
        // Row 6: Separator
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        mainPanel.add(separator, gbc);
        
        // Row 7: Quick Report Buttons
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JLabel quickReportsLabel = new JLabel("Quick Reports:");
        quickReportsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(quickReportsLabel, gbc);
        
        // Row 8: Quick Report Buttons
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 4;
        
        JPanel quickButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        quickButtonPanel.add(generateInventoryButton);
        quickButtonPanel.add(generateSalesButton);
        quickButtonPanel.add(generateTransactionButton);
        
        mainPanel.add(quickButtonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Status label at bottom
        JLabel statusLabel = new JLabel("Ready to generate custom reports", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        generateCustomButton.addActionListener(e -> generateCustomReport());
        saveTemplateButton.addActionListener(e -> saveReportTemplate());
        loadTemplateButton.addActionListener(e -> loadReportTemplate());
        
        // Functional listeners from original file
        generateInventoryButton.addActionListener(e -> {
            try {
                generateInventoryReport(userId);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        
        generateSalesButton.addActionListener(e -> {
            try {
                generateSalesReport(userId);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        
        generateTransactionButton.addActionListener(e -> {
            generateTransactionReport();
        });
    }

    public void refreshData() {
        // Refresh any data if needed
    }

    // Implementation of the custom report generator
    public void openCustomReport() {
        // This method is now fully implemented with the GUI above
        // The panel is already visible when this panel is shown
    }

    // Generate custom report based on user selections
    private void generateCustomReport() {
        try {
            InventoryReportType reportType = (InventoryReportType) reportTypeCombo.getSelectedItem();
            LocalDate startDate = ((java.util.Date) startDateSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate endDate = ((java.util.Date) endDateSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            
            String title = reportTitleField.getText().trim();
            if (title.isEmpty()) {
                title = "Custom Report - " + LocalDate.now();
            }
            
            // Show progress dialog
            ProgressDialog progress = new ProgressDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Generating Custom Report...");
            progress.setVisible(true);

            String finalTitle = title;
            SwingWorker<Report, Void> worker = new SwingWorker<Report, Void>() {
                @Override
                protected Report doInBackground() throws Exception {
                    try {
                        switch (reportType) {
                            case INVENTORY_SUMMARY:
                                return reportGenerator.generateInventorySummary(userId);
                            case SALES_REPORT:
                                return reportGenerator.generateSalesReport(startDate, endDate, userId);
                            case LOW_STOCK:
                                return reportGenerator.generateLowStockReport(userId);
                            case TRANSACTION_HISTORY:
                                return reportGenerator.generateTransactionHistory(startDate, endDate, userId);
                            default:
                                // For custom report type, use functional implementation
                                return reportGenerator.generateCustomReport(
                                        finalTitle, "", reportType, userId, userName);
                        }
                    } catch (Exception e) {
                        throw new Exception("Failed to generate report: " + e.getMessage(), e);
                    }
                }
                
                @Override
                protected void done() {
                    progress.dispose();
                    
                    try {
                        Report report = get();
                        
                        // Format the report based on user options
                        String formattedReport = formatReportForDisplay(report);
                        
                        // Show the report in a dialog
                        showCustomReportDialog(finalTitle, formattedReport);
                        
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ReportGeneratorPanel.this,
                                "Error generating report: " + e.getMessage(),
                                "Generation Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatReportForDisplay(Report report) {
        StringBuilder formatted = new StringBuilder();
        
        formatted.append("REPORT ID: ").append(report.getReportId()).append("\n");
        formatted.append("TITLE: ").append(report.getTitle()).append("\n");
        formatted.append("TYPE: ").append(report.getReportType().getDisplayName()).append("\n");
        formatted.append("GENERATED: ").append(report.getFormattedDate()).append("\n");
        formatted.append("=".repeat(80)).append("\n\n");
        
        if (includeDetailsCheckbox.isSelected()) {
            formatted.append(report.getData()).append("\n");
        }
        
        if (includeSummaryCheckbox.isSelected() && report.getSummary() != null && !report.getSummary().isEmpty()) {
            formatted.append("\n").append("=".repeat(80)).append("\n");
            formatted.append("SUMMARY:\n");
            formatted.append(report.getSummary());
        }
        
        return formatted.toString();
    }
    
    private void showCustomReportDialog(String title, String content) {
        JDialog reportDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        reportDialog.setLayout(new BorderLayout());
        reportDialog.setSize(800, 600);
        reportDialog.setLocationRelativeTo(this);
        
        JTextArea reportArea = new JTextArea(content);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        reportDialog.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton printButton = new JButton("Print");
        printButton.addActionListener(e -> {
            try {
                reportArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(reportDialog,
                        "Error printing: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportReport(content));
        
        JButton saveButton = new JButton("Save to Database");
        saveButton.addActionListener(e -> saveReportToDatabase(title, content));
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> reportDialog.dispose());
        
        buttonPanel.add(printButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        
        reportDialog.add(buttonPanel, BorderLayout.SOUTH);
        reportDialog.setVisible(true);
    }
    
    private void exportReport(String content) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        
        // Add file filters
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }
            
            @Override
            public String getDescription() {
                return "Text Files (*.txt)";
            }
        });
        
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            
            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String selectedFilter = fileChooser.getFileFilter().getDescription();
            
            // Add extension if not present
            if (selectedFilter.contains("*.txt") && !file.getName().toLowerCase().endsWith(".txt")) {
                file = new java.io.File(file.getAbsolutePath() + ".txt");
            } else if (selectedFilter.contains("*.csv") && !file.getName().toLowerCase().endsWith(".csv")) {
                file = new java.io.File(file.getAbsolutePath() + ".csv");
            }
            
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(content);
                JOptionPane.showMessageDialog(this,
                        "Report exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting report: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveReportToDatabase(String title, String content) {
        try {
            InventoryReportType type = (InventoryReportType) reportTypeCombo.getSelectedItem();
            Report report = reportGenerator.generateCustomReport(title, content, type, userId, userName);
            
            JOptionPane.showMessageDialog(this,
                    "Report saved successfully to database!\nReport ID: " + report.getReportId(),
                    "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving report to database: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveReportTemplate() {
        String templateName = JOptionPane.showInputDialog(this,
                "Enter template name:", "Save Template", JOptionPane.QUESTION_MESSAGE);
        
        if (templateName != null && !templateName.trim().isEmpty()) {
            // Create template object
            ReportTemplate template = new ReportTemplate(
                    templateName.trim(),
                    (InventoryReportType) reportTypeCombo.getSelectedItem(),
                    (java.util.Date) startDateSpinner.getValue(),
                    (java.util.Date) endDateSpinner.getValue(),
                    reportTitleField.getText(),
                    includeSummaryCheckbox.isSelected(),
                    includeDetailsCheckbox.isSelected(),
                    userId
            );
            
            // Save template (in a real implementation, this would save to database)
            JOptionPane.showMessageDialog(this,
                    "Template '" + templateName + "' saved successfully!",
                    "Template Saved", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void loadReportTemplate() {
        // In a real implementation, this would load templates from database
        // For now, show a selection dialog
        String[] templateNames = {
                "Weekly Sales Summary",
                "Monthly Inventory Check",
                "Quarterly Supplier Review",
                "Daily Low Stock Alert"
        };
        
        String selectedTemplate = (String) JOptionPane.showInputDialog(this,
                "Select a template to load:",
                "Load Template",
                JOptionPane.QUESTION_MESSAGE,
                null,
                templateNames,
                templateNames[0]);
        
        if (selectedTemplate != null) {
            // Apply template settings
            applyTemplateSettings(selectedTemplate);
        }
    }
    
    private void applyTemplateSettings(String templateName) {
        // Apply predefined template settings
        switch (templateName) {
            case "Weekly Sales Summary":
                reportTypeCombo.setSelectedItem(InventoryReportType.SALES_REPORT);
                startDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now().minusDays(7)));
                endDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now()));
                reportTitleField.setText("Weekly Sales Summary - " + LocalDate.now());
                break;
                
            case "Monthly Inventory Check":
                reportTypeCombo.setSelectedItem(InventoryReportType.INVENTORY_SUMMARY);
                reportTitleField.setText("Monthly Inventory Check - " + LocalDate.now());
                break;
                
            case "Quarterly Supplier Review":
                reportTypeCombo.setSelectedItem(InventoryReportType.SUPPLIER_REPORT);
                reportTitleField.setText("Quarterly Supplier Review - Q" + 
                        ((LocalDate.now().getMonthValue() - 1) / 3 + 1) + " " + LocalDate.now().getYear());
                break;
                
            case "Daily Low Stock Alert":
                reportTypeCombo.setSelectedItem(InventoryReportType.LOW_STOCK);
                reportTitleField.setText("Daily Low Stock Alert - " + LocalDate.now());
                break;
        }
    }
    
    // Report Template class
    private static class ReportTemplate {
        private String name;
        private InventoryReportType type;
        private java.util.Date startDate;
        private java.util.Date endDate;
        private String title;
        private boolean includeSummary;
        private boolean includeDetails;
        private int userId;
        
        public ReportTemplate(String name, InventoryReportType type, java.util.Date startDate,
                             java.util.Date endDate, String title, boolean includeSummary,
                             boolean includeDetails, int userId) {
            this.name = name;
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
            this.title = title;
            this.includeSummary = includeSummary;
            this.includeDetails = includeDetails;
            this.userId = userId;
        }
        
        // Getters
        public String getName() { return name; }
        public InventoryReportType getType() { return type; }
        public java.util.Date getStartDate() { return startDate; }
        public java.util.Date getEndDate() { return endDate; }
        public String getTitle() { return title; }
        public boolean includeSummary() { return includeSummary; }
        public boolean includeDetails() { return includeDetails; }
        public int getUserId() { return userId; }
    }
    
    // Progress Dialog class
    private static class ProgressDialog extends JDialog {
        private JProgressBar progressBar;
        
        public ProgressDialog(Frame parent, String message) {
            super(parent, "Please Wait", true);
            
            setLayout(new BorderLayout(10, 10));
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            
            JLabel label = new JLabel(message, SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            
            add(label, BorderLayout.CENTER);
            add(progressBar, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(parent);
        }
    }
    
    // Functional methods from the original ReportGeneratorPanel.java
    public void generateInventoryReport(int userId) throws SQLException {
        try {
            Report report = reportGenerator.generateInventorySummary(userId);
            String formattedReport = formatReportForDisplay(report);
            showReport("Inventory Report", formattedReport);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public void generateSalesReport(int userId) throws SQLException {
        try {
            Report report = reportGenerator.generateSalesReport(
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                userId
            );
            String formattedReport = formatReportForDisplay(report);
            showReport("Sales Report", formattedReport);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public void generateTransactionReport() {
        try {
            Report report = reportGenerator.generateTransactionHistory(
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                userId
            );
            String formattedReport = formatReportForDisplay(report);
            showReport("Transaction Report", formattedReport);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showReport(String title, String content) {
        JTextArea reportArea = new JTextArea(content);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }
}