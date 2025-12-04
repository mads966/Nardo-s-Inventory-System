package sale;
import product.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.List;

public class SalesProcessingForm extends JPanel {
    private SalesProcessor salesProcessor;
    private ProductService productService;
    private Sale currentSale;
    
    // GUI Components
    private JTextField productSearchField;
    private JButton searchButton;
    private JTable productsTable;
    private JTable saleItemsTable;
    private DefaultTableModel productsModel;
    private DefaultTableModel saleItemsModel;
    
    private JSpinner quantitySpinner;
    private JButton addToSaleButton;
    private JButton removeFromSaleButton;
    private JButton clearSaleButton;
    
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;
    
    private JComboBox<String> paymentMethodCombo;
    private JTextField discountField;
    private JButton applyDiscountButton;
    private JTextArea notesArea;
    
    private JButton processSaleButton;
    private JButton printReceiptButton;
    private JButton saveSaleButton;
    private JButton cancelSaleButton;
    
    private JLabel statusLabel;
    
    private int currentUserId;
    private String currentUserName;
    
    public SalesProcessingForm(Connection connection, int userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.salesProcessor = new SalesProcessor(connection);
        this.productService = new ProductService(connection);
        this.currentSale = Sale.createNewSale(userId, userName);
        
        initComponents();
        layoutComponents();
        setupListeners();
        loadProducts();
        
        // Set keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void initComponents() {
        // Product search
        productSearchField = new JTextField(20);
        searchButton = new JButton("Search");
        
        // Products table
        String[] productColumns = {"ID", "Name", "Category", "Price", "Stock"};
        productsModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;
                    case 3: return Double.class;
                    case 4: return Integer.class;
                    default: return String.class;
                }
            }
        };
        productsTable = new JTable(productsModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.setRowHeight(25);
        
        // Sale items table
        String[] saleColumns = {"ID", "Name", "Qty", "Price", "Total", "Action"};
        saleItemsModel = new DefaultTableModel(saleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 5; // Quantity and Action columns are editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Integer.class;
                    case 2: return Integer.class;
                    case 3: return Double.class;
                    case 4: return Double.class;
                    default: return String.class;
                }
            }
        };
        saleItemsTable = new JTable(saleItemsModel);
        saleItemsTable.setRowHeight(25);
        
        // Quantity spinner
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        
        // Action buttons
        addToSaleButton = new JButton("Add to Sale");
        removeFromSaleButton = new JButton("Remove Selected");
        clearSaleButton = new JButton("Clear Sale");
        
        // Totals display
        subtotalLabel = new JLabel("$0.00");
        taxLabel = new JLabel("$0.00");
        discountLabel = new JLabel("$0.00");
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Payment and discount
        paymentMethodCombo = new JComboBox<>(Sale.PAYMENT_METHODS);
        discountField = new JTextField(10);
        applyDiscountButton = new JButton("Apply Discount %");
        notesArea = new JTextArea(3, 30);
        
        // Main action buttons
        processSaleButton = new JButton("Process Sale");
        processSaleButton.setBackground(new Color(76, 175, 80));
        processSaleButton.setForeground(Color.WHITE);
        processSaleButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        printReceiptButton = new JButton("Print Receipt");
        saveSaleButton = new JButton("Save as Draft");
        cancelSaleButton = new JButton("Cancel");
        
        // Status label
        statusLabel = new JLabel("Ready to process sales");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Top: Product search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(new TitledBorder("Product Search"));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(productSearchField);
        searchPanel.add(searchButton);
        searchPanel.add(new JLabel("  Quantity:"));
        searchPanel.add(quantitySpinner);
        searchPanel.add(addToSaleButton);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Center: Split pane for products and sale items
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setResizeWeight(0.5);
        
        // Products panel
        JPanel productsPanel = new JPanel(new BorderLayout(5, 5));
        productsPanel.setBorder(new TitledBorder("Available Products"));
        
        JScrollPane productsScroll = new JScrollPane(productsTable);
        productsScroll.setPreferredSize(new Dimension(700, 200));
        productsPanel.add(productsScroll, BorderLayout.CENTER);
        
        // Sale items panel
        JPanel salePanel = new JPanel(new BorderLayout(5, 5));
        salePanel.setBorder(new TitledBorder("Current Sale"));
        
        JScrollPane saleScroll = new JScrollPane(saleItemsTable);
        saleScroll.setPreferredSize(new Dimension(700, 200));
        salePanel.add(saleScroll, BorderLayout.CENTER);
        
        // Sale action buttons
        JPanel saleActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saleActions.add(removeFromSaleButton);
        saleActions.add(clearSaleButton);
        salePanel.add(saleActions, BorderLayout.SOUTH);
        
        centerSplit.setTopComponent(productsPanel);
        centerSplit.setBottomComponent(salePanel);
        
        add(centerSplit, BorderLayout.CENTER);
        
        // Right: Totals and payment panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        
        // Totals panel
        JPanel totalsPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        totalsPanel.setBorder(new TitledBorder("Sale Totals"));
        
        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(new JLabel("Tax (10%):"));
        totalsPanel.add(taxLabel);
        totalsPanel.add(new JLabel("Discount:"));
        totalsPanel.add(discountLabel);
        totalsPanel.add(new JLabel("Total:"));
        totalsPanel.add(totalLabel);
        totalsPanel.add(new JLabel("Items:"));
        totalsPanel.add(new JLabel("0"));
        
        // Payment panel
        JPanel paymentPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        paymentPanel.setBorder(new TitledBorder("Payment"));
        
        paymentPanel.add(new JLabel("Payment Method:"));
        paymentPanel.add(paymentMethodCombo);
        paymentPanel.add(new JLabel("Discount (%):"));
        paymentPanel.add(discountField);
        paymentPanel.add(new JLabel(""));
        paymentPanel.add(applyDiscountButton);
        paymentPanel.add(new JLabel("Notes:"));
        paymentPanel.add(new JScrollPane(notesArea));
        
        // Combine totals and payment
        JPanel rightContent = new JPanel();
        rightContent.setLayout(new BoxLayout(rightContent, BoxLayout.Y_AXIS));
        rightContent.add(totalsPanel);
        rightContent.add(Box.createVerticalStrut(10));
        rightContent.add(paymentPanel);
        
        rightPanel.add(rightContent, BorderLayout.NORTH);
        
        // Main action buttons at bottom of right panel
        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        actionPanel.setBorder(new TitledBorder("Actions"));
        actionPanel.add(processSaleButton);
        actionPanel.add(printReceiptButton);
        actionPanel.add(saveSaleButton);
        actionPanel.add(cancelSaleButton);
        
        rightPanel.add(actionPanel, BorderLayout.SOUTH);
        
        add(rightPanel, BorderLayout.EAST);
        
        // Bottom: Status bar
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void setupListeners() {
        // Search button
        searchButton.addActionListener(e -> searchProducts());
        productSearchField.addActionListener(e -> searchProducts());
        
        // Add to sale button
        addToSaleButton.addActionListener(e -> addSelectedProductToSale());
        
        // Remove from sale button
        removeFromSaleButton.addActionListener(e -> removeSelectedItemFromSale());
        
        // Clear sale button
        clearSaleButton.addActionListener(e -> clearCurrentSale());
        
        // Apply discount button
        applyDiscountButton.addActionListener(e -> applyDiscount());
        
        // Main action buttons
        processSaleButton.addActionListener(e -> processCurrentSale());
        printReceiptButton.addActionListener(e -> printCurrentReceipt());
        saveSaleButton.addActionListener(e -> saveCurrentSale());
        cancelSaleButton.addActionListener(e -> cancelCurrentSale());
        
        // Double-click on products table to add to sale
        productsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    addSelectedProductToSale();
                }
            }
        });
        
        // Quantity change in sale items table
        saleItemsModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == 2) { // Quantity column
                updateItemQuantity(row);
            }
        });
    }
    
    private void setupKeyboardShortcuts() {
        // F1: Process sale
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "processSale");
        getActionMap().put("processSale", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processCurrentSale();
            }
        });
        
        // F2: Add to sale
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "addToSale");
        getActionMap().put("addToSale", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedProductToSale();
            }
        });
        
        // F3: Clear sale
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "clearSale");
        getActionMap().put("clearSale", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCurrentSale();
            }
        });
        
        // ESC: Cancel
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelSale");
        getActionMap().put("cancelSale", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelCurrentSale();
            }
        });
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productService.getAllActiveProducts();
            productsModel.setRowCount(0);
            
            for (Product product : products) {
                productsModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    String.format("$%.2f", product.getPrice()),
                    product.getQuantity()
                });
            }
            
            statusLabel.setText("Loaded " + products.size() + " products");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading products: " + e.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchProducts() {
        String searchTerm = productSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }
        
        try {
            List<Product> products = productService.searchProducts(searchTerm);
            productsModel.setRowCount(0);
            
            for (Product product : products) {
                productsModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    String.format("$%.2f", product.getPrice()),
                    product.getQuantity()
                });
            }
            
            statusLabel.setText("Found " + products.size() + " products matching '" + searchTerm + "'");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error searching products: " + e.getMessage(),
                "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addSelectedProductToSale() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to add to sale",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) productsModel.getValueAt(selectedRow, 0);
        String productName = (String) productsModel.getValueAt(selectedRow, 1);
        String category = (String) productsModel.getValueAt(selectedRow, 2);
        
        // Parse price (remove $ sign)
        String priceStr = (String) productsModel.getValueAt(selectedRow, 3);
        double price = Double.parseDouble(priceStr.substring(1));
        
        int quantity = (int) quantitySpinner.getValue();
        
        // Check stock
        try {
            Product product = productService.getProductById(productId);
            if (product.getQuantity() < quantity) {
                int option = JOptionPane.showConfirmDialog(this,
                    "Only " + product.getQuantity() + " in stock. Add " + 
                    product.getQuantity() + " instead?",
                    "Insufficient Stock", JOptionPane.YES_NO_OPTION);
                
                if (option == JOptionPane.YES_OPTION) {
                    quantity = product.getQuantity();
                } else {
                    return;
                }
            }
            
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Cannot add zero quantity",
                    "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Add to sale
            SaleItem item = SaleItem.create(productId, productName, category, quantity, price);
            currentSale.addItem(item);
            
            // Update sale items table
            updateSaleItemsTable();
            
            // Update totals
            updateTotalsDisplay();
            
            // Reset quantity spinner
            quantitySpinner.setValue(1);
            
            // Clear search and select next product
            productSearchField.setText("");
            productsTable.clearSelection();
            
            statusLabel.setText("Added " + quantity + " x " + productName + " to sale");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error adding product to sale: " + e.getMessage(),
                "Add Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSaleItemsTable() {
        saleItemsModel.setRowCount(0);
        
        for (SaleItem item : currentSale.getItems()) {
            saleItemsModel.addRow(new Object[]{
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                String.format("$%.2f", item.getUnitPrice()),
                String.format("$%.2f", item.getLineTotal()),
                "Remove"
            });
        }
    }
    
    private void updateItemQuantity(int row) {
        if (row < 0 || row >= currentSale.getItems().size()) return;
        
        Object value = saleItemsModel.getValueAt(row, 2);
        if (value instanceof Integer) {
            int newQuantity = (int) value;
            
            if (newQuantity <= 0) {
                removeItemFromSale(row);
            } else {
                SaleItem item = currentSale.getItems().get(row);
                
                try {
                    // Check stock
                    Product product = productService.getProductById(item.getProductId());
                    if (product.getQuantity() + item.getQuantity() < newQuantity) {
                        JOptionPane.showMessageDialog(this,
                            "Insufficient stock. Only " + 
                            (product.getQuantity() + item.getQuantity()) + 
                            " available.",
                            "Stock Error", JOptionPane.ERROR_MESSAGE);
                        
                        // Reset to original quantity
                        saleItemsModel.setValueAt(item.getQuantity(), row, 2);
                        return;
                    }
                    
                    item.setQuantity(newQuantity);
                    saleItemsModel.setValueAt(
                        String.format("$%.2f", item.getLineTotal()), row, 4);
                    
                    updateTotalsDisplay();
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Error updating quantity: " + e.getMessage(),
                        "Update Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void removeSelectedItemFromSale() {
        int selectedRow = saleItemsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an item to remove",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        removeItemFromSale(selectedRow);
    }
    
    private void removeItemFromSale(int row) {
        if (row < 0 || row >= currentSale.getItems().size()) return;
        
        SaleItem item = currentSale.getItems().get(row);
        currentSale.removeItem(item.getProductId());
        
        updateSaleItemsTable();
        updateTotalsDisplay();
        
        statusLabel.setText("Removed " + item.getProductName() + " from sale");
    }
    
    private void clearCurrentSale() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear current sale? This cannot be undone.",
            "Confirm Clear", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            currentSale.clearItems();
            updateSaleItemsTable();
            updateTotalsDisplay();
            notesArea.setText("");
            discountField.setText("");
            
            statusLabel.setText("Sale cleared");
        }
    }
    
    private void updateTotalsDisplay() {
        subtotalLabel.setText(String.format("$%.2f", currentSale.getSubtotal()));
        taxLabel.setText(String.format("$%.2f", currentSale.getTaxAmount()));
        discountLabel.setText(String.format("$%.2f", currentSale.getDiscountAmount()));
        totalLabel.setText(String.format("$%.2f", currentSale.getTotalAmount()));
        
        // Update items count
        int totalItems = currentSale.getItems().stream()
            .mapToInt(SaleItem::getQuantity)
            .sum();
        
        // Find the items label (it's the last label in totals panel)
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] subComps = ((JPanel) comp).getComponents();
                for (Component subComp : subComps) {
                    if (subComp instanceof JPanel) {
                        Component[] innerComps = ((JPanel) subComp).getComponents();
                        for (int i = 0; i < innerComps.length; i++) {
                            if (innerComps[i] instanceof JLabel) {
                                JLabel label = (JLabel) innerComps[i];
                                if ("Items:".equals(label.getText())) {
                                    // Next component should be the items count label
                                    if (i + 1 < innerComps.length && innerComps[i + 1] instanceof JLabel) {
                                        ((JLabel) innerComps[i + 1]).setText(String.valueOf(totalItems));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void applyDiscount() {
        String discountText = discountField.getText().trim();
        if (discountText.isEmpty()) {
            return;
        }
        
        try {
            double discountPercent = Double.parseDouble(discountText);
            if (discountPercent < 0 || discountPercent > 100) {
                JOptionPane.showMessageDialog(this,
                    "Discount must be between 0 and 100 percent",
                    "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            currentSale.applyDiscount(discountPercent);
            updateTotalsDisplay();
            
            statusLabel.setText("Applied " + discountPercent + "% discount");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid discount percentage",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // SRS 1.3: Process sale and deduct inventory
    private void processCurrentSale() {
        if (currentSale.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Cannot process empty sale",
                "Empty Sale", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (currentSale.getTotalAmount() <= 0) {
            JOptionPane.showMessageDialog(this,
                "Sale total must be greater than zero",
                "Invalid Total", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Set payment method and notes
        currentSale.setPaymentMethod((String) paymentMethodCombo.getSelectedItem());
        currentSale.setNotes(notesArea.getText());
        
        // Show confirmation dialog
        String message = String.format(
            "Process this sale?\n\n" +
            "Items: %d\n" +
            "Subtotal: $%.2f\n" +
            "Tax: $%.2f\n" +
            "Discount: $%.2f\n" +
            "Total: $%.2f\n" +
            "Payment: %s",
            currentSale.getItems().size(),
            currentSale.getSubtotal(),
            currentSale.getTaxAmount(),
            currentSale.getDiscountAmount(),
            currentSale.getTotalAmount(),
            currentSale.getPaymentMethod()
        );
        
        int confirm = JOptionPane.showConfirmDialog(this,
            message, "Confirm Sale", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Process sale in background
        ProgressDialog progress = new ProgressDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Processing Sale...");
        progress.setVisible(true);
        
        SwingWorker<Sale, Void> worker = new SwingWorker<Sale, Void>() {
            @Override
            protected Sale doInBackground() throws Exception {
                return salesProcessor.processSale(currentSale);
            }
            
            @Override
            protected void done() {
                progress.dispose();
                
                try {
                    Sale processedSale = get();
                    
                    // Show success message
                    JOptionPane.showMessageDialog(SalesProcessingForm.this,
                        "Sale processed successfully!\n" +
                        "Receipt #: " + processedSale.getReceiptNumber() + "\n" +
                        "Total: $" + processedSale.getTotalAmount(),
                        "Sale Complete", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Print receipt automatically
                    printReceipt(processedSale);
                    
                    // Reset for next sale
                    resetForNextSale();
                    
                    statusLabel.setText("Sale #" + processedSale.getSaleId() + " processed successfully");
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SalesProcessingForm.this,
                        "Error processing sale: " + e.getMessage(),
                        "Sale Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void printCurrentReceipt() {
        if (currentSale.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No sale to print",
                "Empty Sale", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        printReceipt(currentSale);
    }
    
    private void printReceipt(Sale sale) {
        String receipt = sale.generateReceipt();
        
        // Show receipt in dialog
        JTextArea receiptArea = new JTextArea(receipt);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(400, 500));
        
        JOptionPane.showMessageDialog(this, scrollPane,
            "Receipt - " + sale.getReceiptNumber(),
            JOptionPane.INFORMATION_MESSAGE);
        
        // Ask if user wants to print
        int printOption = JOptionPane.showConfirmDialog(this,
            "Print this receipt?",
            "Print Receipt", JOptionPane.YES_NO_OPTION);
        
        if (printOption == JOptionPane.YES_OPTION) {
            try {
                receiptArea.print();
                statusLabel.setText("Receipt printed");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error printing receipt: " + e.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveCurrentSale() {
        // For now, just mark as draft in memory
        // In real implementation, save to database as draft
        JOptionPane.showMessageDialog(this,
            "Sale saved as draft. Note: Drafts are not saved to database in this demo.",
            "Sale Saved", JOptionPane.INFORMATION_MESSAGE);
        
        statusLabel.setText("Sale saved as draft");
    }
    
    private void cancelCurrentSale() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel current sale and return to dashboard?",
            "Cancel Sale", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Close this window
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        }
    }
    
    private void resetForNextSale() {
        // Create new sale
        currentSale = Sale.createNewSale(currentUserId, currentUserName);
        
        // Clear UI
        updateSaleItemsTable();
        updateTotalsDisplay();
        notesArea.setText("");
        discountField.setText("");
        productSearchField.setText("");
        
        // Reload products (in case stock changed)
        loadProducts();
    }
    
    // Quick sale method for 3-click sales (SRS 3.3.1)
    public void quickSale(int productId, int quantity) {
        try {
            // Get product
            Product product = productService.getProductById(productId);
            if (product == null) {
                throw new Exception("Product not found");
            }
            
            // Add to sale
            SaleItem item = SaleItem.create(
                productId,
                product.getName(),
                product.getCategory().toString(),
                quantity,
                product.getPrice()
            );
            currentSale.addItem(item);
            
            // Update display
            updateSaleItemsTable();
            updateTotalsDisplay();
            
            // Auto-process if enabled
            if (currentSale.getItems().size() == 1) { // First item
                statusLabel.setText("Added " + product.getName() + ". Add more items or click Process Sale.");
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Quick sale error: " + e.getMessage(),
                "Quick Sale Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Get current sale for dashboard integration
    public Sale getCurrentSale() {
        return currentSale;
    }
    
    // Progress dialog for long operations
    private static class ProgressDialog extends JDialog {
        private JProgressBar progressBar;
        
        public ProgressDialog(Frame parent, String message) {
            super(parent, "Please Wait", true);
            
            setLayout(new BorderLayout(10, 10));
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);
            
            JLabel label = new JLabel(message, SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
            
            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setPreferredSize(new Dimension(300, 20));
            
            add(label, BorderLayout.CENTER);
            add(progressBar, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(parent);
        }
    }
}