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
    private JLabel itemsCountLabel;
    
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
        
        // Set properties for immediate visibility
        setOpaque(true);
        setBackground(Color.WHITE);
        setDoubleBuffered(true); // Enable double buffering

        initComponents();
        layoutComponents();
        setupListeners();
        loadProducts();

        setupKeyboardShortcuts();

        // Force initial state
        setVisible(true);
        setEnabled(true);

        // Immediate validation
        revalidate();
        repaint();

        // Schedule additional validation
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
            if (getParent() != null) {
                getParent().revalidate();
                getParent().repaint();
            }
        });
    }
    
    private void initComponents() {
        // Product search
        productSearchField = new JTextField(20);
        productSearchField.setFont(new Font("Arial", Font.PLAIN, 12));
        productSearchField.setOpaque(true);
        productSearchField.setBackground(Color.WHITE);
        
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.setBackground(new Color(33, 150, 243));
        searchButton.setForeground(Color.WHITE);
        searchButton.setOpaque(true);
        searchButton.setBorderPainted(true);
        
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
        productsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        productsTable.setOpaque(true);
        productsTable.setBackground(Color.WHITE);
        productsTable.setGridColor(new Color(220, 220, 220));
        productsTable.setFillsViewportHeight(true);
        
        // Sale items table
        String[] saleColumns = {"ID", "Name", "Qty", "Price", "Total", "Action"};
        saleItemsModel = new DefaultTableModel(saleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 5;
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
        saleItemsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        saleItemsTable.setOpaque(true);
        saleItemsTable.setBackground(Color.WHITE);
        saleItemsTable.setGridColor(new Color(220, 220, 220));
        saleItemsTable.setFillsViewportHeight(true);
        
        // Quantity spinner
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setFont(new Font("Arial", Font.PLAIN, 12));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) quantitySpinner.getEditor();
        editor.getTextField().setOpaque(true);
        editor.getTextField().setBackground(Color.WHITE);
        
        // Action buttons
        addToSaleButton = createStyledButton("Add to Sale", new Color(76, 175, 80));
        removeFromSaleButton = createStyledButton("Remove Selected", new Color(244, 67, 54));
        clearSaleButton = createStyledButton("Clear Sale", new Color(255, 152, 0));
        
        // Totals display
        subtotalLabel = createStyledLabel("$0.00");
        taxLabel = createStyledLabel("$0.00");
        discountLabel = createStyledLabel("$0.00");
        totalLabel = createStyledLabel("$0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(33, 150, 243));
        
        itemsCountLabel = createStyledLabel("0");
        
        // Payment and discount
        paymentMethodCombo = new JComboBox<>(Sale.PAYMENT_METHODS);
        paymentMethodCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        paymentMethodCombo.setOpaque(true);
        paymentMethodCombo.setBackground(Color.WHITE);
        
        discountField = new JTextField(10);
        discountField.setFont(new Font("Arial", Font.PLAIN, 12));
        discountField.setOpaque(true);
        discountField.setBackground(Color.WHITE);
        
        applyDiscountButton = createStyledButton("Apply Discount %", new Color(156, 39, 176));
        
        notesArea = new JTextArea(3, 30);
        notesArea.setFont(new Font("Arial", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setOpaque(true);
        notesArea.setBackground(Color.WHITE);
        
        // Main action buttons
        processSaleButton = createStyledButton("Process Sale", new Color(76, 175, 80));
        processSaleButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        printReceiptButton = createStyledButton("Print Receipt", new Color(33, 150, 243));
        saveSaleButton = createStyledButton("Save as Draft", new Color(255, 152, 0));
        cancelSaleButton = createStyledButton("Cancel", new Color(158, 158, 158));
        
        // Status label
        statusLabel = new JLabel("Ready to process sales");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusLabel.setBackground(new Color(245, 245, 245));
        statusLabel.setOpaque(true);
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(true);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    
    private void layoutComponents() {
        // Use a main container with proper background
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        
        // Top: Product search panel
        JPanel searchPanel = createWhitePanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Product Search"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        searchPanel.add(createLabel("Search:"));
        searchPanel.add(productSearchField);
        searchPanel.add(searchButton);
        searchPanel.add(createLabel("  Quantity:"));
        searchPanel.add(quantitySpinner);
        searchPanel.add(addToSaleButton);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Center: Split pane for products and sale items
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setResizeWeight(0.5);
        centerSplit.setDividerLocation(400);
        centerSplit.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        centerSplit.setOpaque(true);
        centerSplit.setBackground(Color.WHITE);
        
        // Products panel
        JPanel productsPanel = createWhitePanel();
        productsPanel.setLayout(new BorderLayout(5, 5));
        productsPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Available Products"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane productsScroll = new JScrollPane(productsTable);
        productsScroll.setPreferredSize(new Dimension(800, 300));
        productsScroll.setMinimumSize(new Dimension(800, 200));
        productsScroll.setOpaque(true);
        productsScroll.getViewport().setBackground(Color.WHITE);
        productsPanel.add(productsScroll, BorderLayout.CENTER);
        
        // Sale items panel
        JPanel salePanel = createWhitePanel();
        salePanel.setLayout(new BorderLayout(5, 5));
        salePanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Current Sale"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane saleScroll = new JScrollPane(saleItemsTable);
        saleScroll.setPreferredSize(new Dimension(800, 300));
        saleScroll.setMinimumSize(new Dimension(800, 200));
        saleScroll.setOpaque(true);
        saleScroll.getViewport().setBackground(Color.WHITE);
        salePanel.add(saleScroll, BorderLayout.CENTER);
        
        // Sale action buttons
        JPanel saleActions = createWhitePanel();
        saleActions.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        saleActions.add(removeFromSaleButton);
        saleActions.add(clearSaleButton);
        salePanel.add(saleActions, BorderLayout.SOUTH);
        
        centerSplit.setTopComponent(productsPanel);
        centerSplit.setBottomComponent(salePanel);
        
        add(centerSplit, BorderLayout.CENTER);
        
        // Right: Totals and payment panel
        JPanel rightPanel = createWhitePanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        // Totals panel
        JPanel totalsPanel = createWhitePanel();
        totalsPanel.setLayout(new GridLayout(6, 2, 10, 10));
        totalsPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Sale Totals"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        totalsPanel.setMaximumSize(new Dimension(350, 250));
        
        totalsPanel.add(createBoldLabel("Subtotal:"));
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(createBoldLabel("Tax (10%):"));
        totalsPanel.add(taxLabel);
        totalsPanel.add(createBoldLabel("Discount:"));
        totalsPanel.add(discountLabel);
        totalsPanel.add(createBoldLabel("Total:"));
        totalsPanel.add(totalLabel);
        totalsPanel.add(createBoldLabel("Items:"));
        totalsPanel.add(itemsCountLabel);
        
        // Payment panel
        JPanel paymentPanel = createWhitePanel();
        paymentPanel.setLayout(new GridBagLayout());
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Payment"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        paymentPanel.setMaximumSize(new Dimension(350, 300));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Payment method
        gbc.gridx = 0; gbc.gridy = 0;
        paymentPanel.add(createBoldLabel("Payment Method:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        paymentPanel.add(paymentMethodCombo, gbc);
        
        // Discount
        gbc.gridx = 0; gbc.gridy = 1;
        paymentPanel.add(createBoldLabel("Discount (%):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        paymentPanel.add(discountField, gbc);
        
        // Apply discount button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        paymentPanel.add(applyDiscountButton, gbc);
        
        // Notes label
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paymentPanel.add(createBoldLabel("Notes:"), gbc);
        
        // Notes area
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(300, 80));
        notesScroll.setOpaque(true);
        notesScroll.getViewport().setBackground(Color.WHITE);
        paymentPanel.add(notesScroll, gbc);
        
        // Main action buttons panel
        JPanel actionPanel = createWhitePanel();
        actionPanel.setLayout(new GridLayout(4, 1, 10, 10));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Actions"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        actionPanel.setMaximumSize(new Dimension(350, 250));
        
        actionPanel.add(processSaleButton);
        actionPanel.add(printReceiptButton);
        actionPanel.add(saveSaleButton);
        actionPanel.add(cancelSaleButton);
        
        // Add all panels to right panel
        rightPanel.add(totalsPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(paymentPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(actionPanel);
        rightPanel.add(Box.createVerticalGlue());
        
        add(rightPanel, BorderLayout.EAST);
        
        // Bottom: Status bar
        JPanel statusPanel = createWhitePanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createWhitePanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    
    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
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
        itemsCountLabel.setText(String.valueOf(totalItems));
        
        // Force UI update
        revalidate();
        repaint();
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
    private class ProgressDialog extends JDialog {
        private JProgressBar progressBar;
        
        public ProgressDialog(Frame parent, String message) {
            super(parent, "Please Wait", true);
            
            setLayout(new BorderLayout(10, 10));
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);
            setOpaque(true);
            setBackground(Color.WHITE);
            
            JLabel label = new JLabel(message, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            
            progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setPreferredSize(new Dimension(300, 20));
            progressBar.setOpaque(true);
            progressBar.setBackground(Color.WHITE);
            
            add(label, BorderLayout.CENTER);
            add(progressBar, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(parent);
        }
    }
}