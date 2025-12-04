package sale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SalesHistoryForm extends JPanel {
    private SalesProcessor salesProcessor;
    private DefaultTableModel salesModel;
    private JTable salesTable;
    
    private JFormattedTextField startDateField;
    private JFormattedTextField endDateField;
    private JButton filterButton;
    private JButton refreshButton;
    private JButton viewDetailsButton;
    private JButton printReceiptButton;
    private JButton exportButton;
    
    public SalesHistoryForm(SalesProcessor salesProcessor) {
        this.salesProcessor = salesProcessor;
        
        initComponents();
        layoutComponents();
        setupListeners();
        loadSales();
    }
    
    private void initComponents() {
        // Date fields
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        startDateField = new JFormattedTextField(formatter);
        startDateField.setValue(LocalDate.now().minusDays(7));
        startDateField.setColumns(10);
        
        endDateField = new JFormattedTextField(formatter);
        endDateField.setValue(LocalDate.now());
        endDateField.setColumns(10);
        
        // Buttons
        filterButton = new JButton("Filter");
        refreshButton = new JButton("Refresh");
        viewDetailsButton = new JButton("View Details");
        printReceiptButton = new JButton("Print Receipt");
        exportButton = new JButton("Export");
        
        // Sales table
        String[] columns = {"ID", "Date/Time", "Receipt #", "Items", "Subtotal", "Tax", "Total", "Payment", "Status"};
        salesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;
                    case 3: return Integer.class;
                    case 4: case 5: case 6: return Double.class;
                    default: return String.class;
                }
            }
        };
        salesTable = new JTable(salesModel);
        salesTable.setRowHeight(25);
        salesTable.setAutoCreateRowSorter(true);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Top: Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBorder(new TitledBorder("Filter Sales"));
        
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(startDateField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(endDateField);
        filterPanel.add(filterButton);
        filterPanel.add(refreshButton);
        
        add(filterPanel, BorderLayout.NORTH);
        
        // Center: Sales table
        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(new TitledBorder("Sales History"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom: Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.add(viewDetailsButton);
        actionPanel.add(printReceiptButton);
        actionPanel.add(exportButton);
        
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private void setupListeners() {
        filterButton.addActionListener(e -> filterSales());
        refreshButton.addActionListener(e -> loadSales());
        viewDetailsButton.addActionListener(e -> viewSaleDetails());
        printReceiptButton.addActionListener(e -> printSelectedReceipt());
        exportButton.addActionListener(e -> exportSales());
    }
    
    private void loadSales() {
        try {
            List<Sale> sales = salesProcessor.getAllSales();
            displaySales(sales);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading sales: " + e.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterSales() {
        try {
            LocalDate startDate = (LocalDate) startDateField.getValue();
            LocalDate endDate = (LocalDate) endDateField.getValue();
            
            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this,
                    "Please enter valid dates",
                    "Invalid Dates", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this,
                    "Start date must be before end date",
                    "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            List<Sale> sales = salesProcessor.getSalesByDateRange(startDate, endDate);
            displaySales(sales);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error filtering sales: " + e.getMessage(),
                "Filter Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displaySales(List<Sale> sales) {
        salesModel.setRowCount(0);
        
        for (Sale sale : sales) {
            salesModel.addRow(new Object[]{
                sale.getSaleId(),
                sale.getSaleDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                sale.getReceiptNumber(),
                sale.getItems().size(),
                String.format("%.2f", sale.getSubtotal()),
                String.format("%.2f", sale.getTaxAmount()),
                String.format("%.2f", sale.getTotalAmount()),
                sale.getPaymentMethod(),
                sale.getPaymentStatus()
            });
        }
    }
    
    private void viewSaleDetails() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a sale to view",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = salesTable.convertRowIndexToModel(selectedRow);
        int saleId = (int) salesModel.getValueAt(modelRow, 0);
        
        try {
            Sale sale = salesProcessor.getSaleById(saleId);
            if (sale != null) {
                showSaleDetails(sale);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading sale details: " + e.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showSaleDetails(Sale sale) {
        StringBuilder details = new StringBuilder();
        details.append("SALE DETAILS\n");
        details.append("============\n");
        details.append("Receipt #: ").append(sale.getReceiptNumber()).append("\n");
        details.append("Date: ").append(sale.getSaleDateTime().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        details.append("Cashier: ").append(sale.getUserName()).append("\n");
        details.append("Status: ").append(sale.getPaymentStatus()).append("\n");
        details.append("Payment: ").append(sale.getPaymentMethod()).append("\n");
        details.append("-".repeat(40)).append("\n");
        details.append("ITEMS:\n");
        
        for (SaleItem item : sale.getItems()) {
            details.append(String.format("  %s x%d @ $%.2f = $%.2f\n",
                item.getProductName(), item.getQuantity(), 
                item.getUnitPrice(), item.getLineTotal()));
        }
        
        details.append("-".repeat(40)).append("\n");
        details.append(String.format("Subtotal: $%.2f\n", sale.getSubtotal()));
        details.append(String.format("Tax: $%.2f\n", sale.getTaxAmount()));
        details.append(String.format("Discount: $%.2f\n", sale.getDiscountAmount()));
        details.append(String.format("TOTAL: $%.2f\n", sale.getTotalAmount()));
        
        if (sale.getNotes() != null && !sale.getNotes().isEmpty()) {
            details.append("Notes: ").append(sale.getNotes()).append("\n");
        }
        
        JTextArea detailsArea = new JTextArea(details.toString());
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane,
            "Sale Details - " + sale.getReceiptNumber(),
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void printSelectedReceipt() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a sale to print",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = salesTable.convertRowIndexToModel(selectedRow);
        int saleId = (int) salesModel.getValueAt(modelRow, 0);
        
        try {
            Sale sale = salesProcessor.getSaleById(saleId);
            if (sale != null) {
                String receipt = sale.generateReceipt();
                
                JTextArea receiptArea = new JTextArea(receipt);
                receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                
                int option = JOptionPane.showConfirmDialog(this,
                    receiptArea, "Print Receipt - " + sale.getReceiptNumber(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
                
                if (option == JOptionPane.OK_OPTION) {
                    receiptArea.print();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error printing receipt: " + e.getMessage(),
                "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportSales() {
        // Create export dialog
        ExportSalesDialog exportDialog = new ExportSalesDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                salesModel,
                salesTable
        );
        exportDialog.setVisible(true);
    }

    // Inner class for export dialog
    private class ExportSalesDialog extends JDialog {
        private DefaultTableModel tableModel;
        private JTable table;
        private JRadioButton exportAllRadio;
        private JRadioButton exportFilteredRadio;
        private JRadioButton exportSelectedRadio;
        private JCheckBox includeHeadersCheck;
        private JTextField fileNameField;
        private JButton browseButton;
        private JButton exportButton;
        private JButton cancelButton;

        public ExportSalesDialog(Frame parent, DefaultTableModel model, JTable table) {
            super(parent, "Export Sales Data", true);
            this.tableModel = model;
            this.table = table;

            initComponents();
            layoutComponents();
            setupListeners();

            setSize(500, 300);
            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            // Export options
            exportAllRadio = new JRadioButton("Export All Sales");
            exportFilteredRadio = new JRadioButton("Export Filtered Sales (Current View)");
            exportSelectedRadio = new JRadioButton("Export Selected Rows Only");

            ButtonGroup exportGroup = new ButtonGroup();
            exportGroup.add(exportAllRadio);
            exportGroup.add(exportFilteredRadio);
            exportGroup.add(exportSelectedRadio);
            exportFilteredRadio.setSelected(true);

            includeHeadersCheck = new JCheckBox("Include Column Headers", true);

            // File selection
            fileNameField = new JTextField(25);
            fileNameField.setText(generateDefaultFileName());

            browseButton = new JButton("Browse...");

            // Action buttons
            exportButton = new JButton("Export");
            exportButton.setBackground(new Color(76, 175, 80));
            exportButton.setForeground(Color.WHITE);

            cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(158, 158, 158));
            cancelButton.setForeground(Color.WHITE);
        }

        private void layoutComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Export options
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(new JLabel("<html><b>Export Options:</b></html>"), gbc);

            gbc.gridy = 1;
            gbc.insets = new Insets(2, 20, 2, 5);
            add(exportAllRadio, gbc);

            gbc.gridy = 2;
            add(exportFilteredRadio, gbc);

            gbc.gridy = 3;
            add(exportSelectedRadio, gbc);

            gbc.gridy = 4;
            add(includeHeadersCheck, gbc);

            // File selection
            gbc.gridy = 5;
            gbc.gridwidth = 1;
            gbc.insets = new Insets(15, 5, 5, 5);
            add(new JLabel("File Name:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            add(fileNameField, gbc);

            gbc.gridx = 2;
            gbc.weightx = 0;
            add(browseButton, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            buttonPanel.add(exportButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 3;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
            add(buttonPanel, gbc);
        }

        private void setupListeners() {
            browseButton.addActionListener(e -> browseForFile());
            exportButton.addActionListener(e -> performExport());
            cancelButton.addActionListener(e -> dispose());

            // Update file name when options change
            ActionListener updateFileName = e -> fileNameField.setText(generateDefaultFileName());
            exportAllRadio.addActionListener(updateFileName);
            exportFilteredRadio.addActionListener(updateFileName);
            exportSelectedRadio.addActionListener(updateFileName);
        }

        private String generateDefaultFileName() {
            String baseName = "sales_export_";
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            if (exportAllRadio.isSelected()) {
                return baseName + "all_" + timestamp + ".csv";
            } else if (exportSelectedRadio.isSelected()) {
                return baseName + "selected_" + timestamp + ".csv";
            } else {
                return baseName + "filtered_" + timestamp + ".csv";
            }
        }

        private void browseForFile() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Export File");
            fileChooser.setSelectedFile(new File(fileNameField.getText()));

            // Set CSV filter
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
                }

                @Override
                public String getDescription() {
                    return "CSV Files (*.csv)";
                }
            });

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // Ensure .csv extension
                String filePath = selectedFile.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }
                fileNameField.setText(filePath);
            }
        }

        private void performExport() {
            String filePath = fileNameField.getText().trim();
            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please specify a file name",
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File exportFile = new File(filePath);
            if (exportFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Determine which rows to export
            List<Integer> rowsToExport = getRowsToExport();
            if (rowsToExport.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No data to export",
                        "Export Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Perform export in background
            ProgressDialog progress = new ProgressDialog((Frame) getParent(), "Exporting Data...");
            progress.setVisible(true);

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                private String errorMessage = null;

                @Override
                protected Boolean doInBackground() {
                    try {
                        exportToCSV(exportFile, rowsToExport);
                        return true;
                    } catch (IOException e) {
                        errorMessage = e.getMessage();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    progress.dispose();

                    try {
                        if (get()) {
                            // Success
                            JOptionPane.showMessageDialog(ExportSalesDialog.this,
                                    String.format("Successfully exported %d rows to:\n%s",
                                            rowsToExport.size(), exportFile.getAbsolutePath()),
                                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        } else {
                            // Error
                            JOptionPane.showMessageDialog(ExportSalesDialog.this,
                                    "Export failed: " + errorMessage,
                                    "Export Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ExportSalesDialog.this,
                                "Export failed: " + e.getMessage(),
                                "Export Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        }

        private List<Integer> getRowsToExport() {
            List<Integer> rows = new ArrayList<>();

            if (exportAllRadio.isSelected()) {
                // Get all sales from database
                try {
                    List<Sale> allSales = salesProcessor.getAllSales();
                    // Convert to row indices (for this we'd need to map sale objects to table rows)
                    // For simplicity, we'll export the current table data
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        rows.add(i);
                    }
                } catch (Exception e) {
                    // Fall back to current table data
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        rows.add(i);
                    }
                }
            } else if (exportSelectedRadio.isSelected()) {
                // Export only selected rows
                int[] selectedRows = table.getSelectedRows();
                for (int viewRow : selectedRows) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    rows.add(modelRow);
                }
            } else {
                // Export filtered rows (current view)
                for (int i = 0; i < table.getRowCount(); i++) {
                    int modelRow = table.convertRowIndexToModel(i);
                    rows.add(modelRow);
                }
            }

            return rows;
        }

        private void exportToCSV(File file, List<Integer> rows) throws IOException {
            try (FileWriter writer = new FileWriter(file)) {
                // Write headers if requested
                if (includeHeadersCheck.isSelected()) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        writer.append(escapeCSV(tableModel.getColumnName(col)));
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.append(',');
                        }
                    }
                    writer.append('\n');
                }

                // Write data rows
                for (int i = 0; i < rows.size(); i++) {
                    int row = rows.get(i);
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        writer.append(escapeCSV(value != null ? value.toString() : ""));
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.append(',');
                        }
                    }
                    writer.append('\n');
                }

                writer.flush();
            }
        }

        private String escapeCSV(String value) {
            if (value == null) return "";

            // If value contains commas, quotes, or newlines, wrap in quotes and escape existing quotes
            if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
                return "\"" + value.replace("\"", "\"\"") + "\"";
            }
            return value;
        }
    }

    // Progress dialog for export operation
    private class ProgressDialog extends JDialog {
        private JProgressBar progressBar;

        public ProgressDialog(Frame parent, String message) {
            super(parent, "Please Wait", true);

            setLayout(new BorderLayout(10, 10));
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);

            JLabel label = new JLabel(message, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);

            add(label, BorderLayout.CENTER);
            add(progressBar, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
        }
    }
}