package sale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        JOptionPane.showMessageDialog(this,
            "Export functionality would save sales data to CSV/Excel",
            "Export", JOptionPane.INFORMATION_MESSAGE);
    }
}