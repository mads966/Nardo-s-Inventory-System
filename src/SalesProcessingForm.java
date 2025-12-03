import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SalesProcessingForm extends JPanel {
    private JTextField productSearch;
    private JSpinner quantityField;
    private JTable saleItemsTable;
    private JLabel totalAmount;
    private JButton processSaleButton;
    private JButton printReceiptButton;
    private JButton addButton;
    private JButton removeButton;
    
    private DefaultTableModel tableModel;
    private List<SaleItem> currentSaleItems;
    private SalesProcessor salesProcessor;
    
    // SRS 3.3.1: First-time user can process sale in 3 clicks or less
    public SalesProcessingForm() {
        currentSaleItems = new ArrayList<>();
        salesProcessor = new SalesProcessor();
        
        setLayout(new BorderLayout());
        
        // Top panel for product search
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Product Search:"));
        productSearch = new JTextField(20);
        searchPanel.add(productSearch);
        
        searchPanel.add(new JLabel("Quantity:"));
        quantityField = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        searchPanel.add(quantityField);
        
        addButton = new JButton("Add to Sale");
        searchPanel.add(addButton);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Center panel for sale items table
        String[] columnNames = {"Product ID", "Product Name", "Quantity", "Unit Price", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0);
        saleItemsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(saleItemsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel for total and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Total display
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.add(new JLabel("Total Amount:"));
        totalAmount = new JLabel("$0.00");
        totalPanel.add(totalAmount);
        bottomPanel.add(totalPanel, BorderLayout.WEST);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        removeButton = new JButton("Remove Selected");
        processSaleButton = new JButton("Process Sale");
        printReceiptButton = new JButton("Print Receipt");
        
        buttonPanel.add(removeButton);
        buttonPanel.add(processSaleButton);
        buttonPanel.add(printReceiptButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToSale();
            }
        });
        
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });
        
        processSaleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processSale();
            }
        });
        
        printReceiptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printReceipt();
            }
        });
        
        // SRS 3.3.1: Quick product lookup
        productSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchProductByName(productSearch.getText());
            }
        });
    }
    
    // SRS 1.4: Search product by name
    public void searchProductByName(String productName) {
        // In actual implementation, search database
        System.out.println("Searching for product: " + productName);
        // For demo, just show in search field
    }
    
    public void addToSale() {
        // SRS 1.3: Add items to current sale
        String productName = productSearch.getText();
        int quantity = (int) quantityField.getValue();
        
        if (!productName.isEmpty() && quantity > 0) {
            // In actual implementation, get product details from database
            int productId = 101; // Simulated
            double unitPrice = 7.99; // Simulated
            
            SaleItem item = new SaleItem(productId, productName, quantity, unitPrice);
            currentSaleItems.add(item);
            
            // Add to table
            tableModel.addRow(new Object[]{
                productId, productName, quantity, 
                String.format("$%.2f", unitPrice),
                String.format("$%.2f", item.getLineTotal())
            });
            
            calculateTotal();
            
            // Clear input fields
            productSearch.setText("");
            quantityField.setValue(1);
        }
    }
    
    private void removeSelectedItem() {
        int selectedRow = saleItemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            currentSaleItems.remove(selectedRow);
            tableModel.removeRow(selectedRow);
            calculateTotal();
        }
    }
    
    public void calculateTotal() {
        double total = salesProcessor.calculateTotal(currentSaleItems);
        totalAmount.setText(String.format("$%.2f", total));
    }
    
    // SRS 1.3: Process sale and deduct inventory
    public void processSale() {
        if (!currentSaleItems.isEmpty()) {
            int userId = 1; // In actual implementation, get from session
            
            // SRS 3.1: Process under 3 seconds
            long startTime = System.currentTimeMillis();
            salesProcessor.processSale(currentSaleItems, userId);
            long endTime = System.currentTimeMillis();
            
            // Clear current sale
            currentSaleItems.clear();
            tableModel.setRowCount(0);
            calculateTotal();
            
            JOptionPane.showMessageDialog(this, 
                String.format("Sale processed successfully in %d ms!", (endTime - startTime)),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "No items in sale!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void printReceipt() {
        // Generate receipt for last sale
        JOptionPane.showMessageDialog(this, 
            "Receipt printing functionality would be implemented here",
            "Print Receipt", JOptionPane.INFORMATION_MESSAGE);
    }
}