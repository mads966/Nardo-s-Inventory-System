package main;

import alert.AlertManager;
import login.User;
import product.*;
import stock.StockMovementService;
import stock.StockMovement;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import database.*;
import supplies.SupplierDAO;

public class InventoryManagementPanel extends JPanel {
    private User currentUser;
    private ProductService productService;
    private StockMovementService stockMovementService;
    private AlertManager alertManager;
    
    private JTable productsTable;
    private DefaultTableModel productsTableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JButton addButton, editButton, deleteButton, restockButton;
    private JButton viewMovementsButton, checkAlertsButton;
    
    public InventoryManagementPanel(User user, Connection conn) {
        this.currentUser = user;
        this.productService = new ProductService(null); // Connection handled by DAO
        this.stockMovementService = new StockMovementService(conn);
        this.alertManager = new AlertManager();

        setPreferredSize(new Dimension(1000, 600));

        initComponents();
        layoutComponents();
        loadProducts();

        revalidate();
        repaint();
    }
    
    private void initComponents() {
        // Products table
        String[] columns = {"ID", "Name", "Category", "Price", "Quantity", "Min Stock", "Status"};
        productsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productsTable = new JTable(productsTableModel);
        productsTable.setRowHeight(25);
        
        // Search and filter components
        searchField = new JTextField(20);
        categoryFilter = new JComboBox<>(new String[]{"All", "FOOD", "BEVERAGES", "SNACKS", "ESSENTIALS", "COMBO MEALS"});
        
        // Buttons
        addButton = new JButton("Add Product");
        editButton = new JButton("Edit Product");
        deleteButton = new JButton("Deactivate");
        restockButton = new JButton("Restock");
        viewMovementsButton = new JButton("View Movements");
        checkAlertsButton = new JButton("Check Alerts");
        
        // Add action listeners
        addButton.addActionListener(e -> addNewProduct());
        editButton.addActionListener(e -> editSelectedProduct());
        deleteButton.addActionListener(e -> deactivateSelectedProduct());
        restockButton.addActionListener(e -> restockSelectedProduct());
        viewMovementsButton.addActionListener(e -> viewStockMovements());
        checkAlertsButton.addActionListener(e -> checkAlerts());
        
        searchField.addActionListener(e -> searchProducts());
        categoryFilter.addActionListener(e -> filterByCategory());
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel: Search and filters
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBorder(new TitledBorder("Search & Filter"));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("Category:"));
        topPanel.add(categoryFilter);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center: Products table
        JScrollPane tableScroll = new JScrollPane(productsTable);
        tableScroll.setBorder(new TitledBorder("Products"));
        add(tableScroll, BorderLayout.CENTER);
        
        // Bottom: Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(restockButton);
        buttonPanel.add(viewMovementsButton);
        buttonPanel.add(checkAlertsButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void refreshData() {
        loadProducts();
    }
    
    private void loadProducts() {
        SwingWorker<List<Product>, Void> worker = new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return productService.getAllActiveProducts();
            }
            
            @Override
            protected void done() {
                try {
                    List<Product> products = get();
                    productsTableModel.setRowCount(0);
                    
                    for (Product product : products) {
                        String status = product.getQuantity() <= product.getMinStock() ? 
                                      "LOW STOCK" : "OK";
                        
                        productsTableModel.addRow(new Object[]{
                            product.getProductId(),
                            product.getName(),
                            product.getCategory(),
                            String.format("$%.2f", product.getPrice()),
                            product.getQuantity(),
                            product.getMinStock(),
                            status
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(InventoryManagementPanel.this,
                        "Error loading products: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void searchProducts() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadProducts();
            return;
        }
        
        try {
            List<Product> products = productService.searchProducts(query);
            productsTableModel.setRowCount(0);
            
            for (Product product : products) {
                String status = product.getQuantity() <= product.getMinStock() ? 
                              "LOW STOCK" : "OK";
                
                productsTableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    String.format("$%.2f", product.getPrice()),
                    product.getQuantity(),
                    product.getMinStock(),
                    status
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error searching products: " + e.getMessage(),
                "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterByCategory() {
        String category = (String) categoryFilter.getSelectedItem();
        if ("All".equals(category)) {
            loadProducts();
            return;
        }
        
        try {
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.filterByCategory(category);
            productsTableModel.setRowCount(0);
            
            for (Product product : products) {
                String status = product.getQuantity() <= product.getMinStock() ? 
                              "LOW STOCK" : "OK";
                
                productsTableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    String.format("$%.2f", product.getPrice()),
                    product.getQuantity(),
                    product.getMinStock(),
                    status
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error filtering products: " + e.getMessage(),
                "Filter Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addNewProduct() {
        ProductDialog dialog = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadProducts();
        }
    }
    
    private void editSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to edit",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) productsTableModel.getValueAt(selectedRow, 0);
        
        try {
            Product product = productService.getProductById(productId);
            if (product != null) {
                ProductDialog dialog = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), product);
                dialog.setVisible(true);
                
                if (dialog.isSaved()) {
                    loadProducts();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading product: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deactivateSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to deactivate",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) productsTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productsTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deactivate product: " + productName + "?\n\n" +
            "This will mark the product as inactive but preserve sales history.",
            "Confirm Deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productService.deleteProduct(productId);
                JOptionPane.showMessageDialog(this,
                    "Product deactivated successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error deactivating product: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void restockSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to restock",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) productsTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productsTableModel.getValueAt(selectedRow, 1);
        int currentQty = (int) productsTableModel.getValueAt(selectedRow, 4);
        
        String input = JOptionPane.showInputDialog(this,
            "Restock " + productName + "\nCurrent quantity: " + currentQty + "\n\n" +
            "Enter quantity to add:",
            "Restock Product", JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                int quantityToAdd = Integer.parseInt(input.trim());
                if (quantityToAdd <= 0) {
                    throw new NumberFormatException("Quantity must be positive");
                }
                
                stockMovementService.recordRestockMovement(
                    productId,
                    currentUser.getUserId(),
                    quantityToAdd,
                    0, // supplier ID (could be selected from dialog)
                    "Manual restock by " + currentUser.getUsername()
                );
                
                JOptionPane.showMessageDialog(this,
                    "Restocked " + quantityToAdd + " units of " + productName,
                    "Restock Complete", JOptionPane.INFORMATION_MESSAGE);
                
                loadProducts();
                
                // Check if alerts need to be resolved
                alertManager.checkStockLevels();
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid positive number",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error restocking product: " + e.getMessage(),
                    "Restock Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewStockMovements() {
        StockMovementsDialog dialog = new StockMovementsDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            stockMovementService,
            currentUser.getUserId()
        );
        dialog.setVisible(true);
    }
    
    private void checkAlerts() {
        alertManager.checkStockLevels();
        
        List<stock.LowStockAlert> alerts = alertManager.getActiveAlerts();
        
        if (alerts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No active low stock alerts",
                "Alerts Check", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StringBuilder alertText = new StringBuilder();
        alertText.append("ACTIVE LOW STOCK ALERTS\n");
        alertText.append("=======================\n\n");
        
        ProductDAO productDAO = new ProductDAO();
        for (stock.LowStockAlert alert : alerts) {
            try {
                Product product = productDAO.getProductById(alert.getProductId());
                if (product != null) {
                    alertText.append(String.format("%s (ID: %d)\n", 
                        product.getName(), product.getProductId()));
                    alertText.append(String.format("  Current: %d | Minimum: %d | Need: %d\n\n",
                        alert.getCurrentQuantity(),
                        alert.getMinStockLevel(),
                        alert.getMinStockLevel() - alert.getCurrentQuantity()));
                }
            } catch (Exception e) {
                // Skip if product not found
            }
        }
        
        JTextArea alertArea = new JTextArea(alertText.toString());
        alertArea.setEditable(false);
        alertArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(alertArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane,
            "Low Stock Alerts", JOptionPane.WARNING_MESSAGE);
    }
    
    public void addNewStock() {
        addNewProduct();
    }

    public void manageSuppliers(Connection connection) {
        SupplierManagementDialog dialog = new SupplierManagementDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                connection,
                currentUser
        );
        dialog.setVisible(true);
    }
    
    // Inner class for product dialog
    private class ProductDialog extends JDialog {
        private Product product;
        private boolean saved = false;
        
        private JTextField nameField, priceField, quantityField, minStockField;
        private JComboBox<String> categoryCombo;
        private JButton saveButton, cancelButton;
        
        public ProductDialog(Frame parent, Product existingProduct) {
            super(parent, existingProduct == null ? "Add New Product" : "Edit Product", true);
            this.product = existingProduct;
            
            initComponents();
            layoutComponents();
            
            if (existingProduct != null) {
                loadProductData();
            }
            
            setSize(400, 300);
            setLocationRelativeTo(parent);
        }
        
        private void initComponents() {
            nameField = new JTextField(20);
            priceField = new JTextField(10);
            quantityField = new JTextField(10);
            minStockField = new JTextField(10);
            
            String[] categories = {"FOOD", "BEVERAGES", "SNACKS", "ESSENTIALS", "COMBO MEALS"};
            categoryCombo = new JComboBox<>(categories);
            
            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");
            
            saveButton.addActionListener(e -> saveProduct());
            cancelButton.addActionListener(e -> dispose());
        }
        
        private void layoutComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Row 0: Name
            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Name:"), gbc);
            
            gbc.gridx = 1; gbc.weightx = 1.0;
            add(nameField, gbc);
            
            // Row 1: Category
            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Category:"), gbc);
            
            gbc.gridx = 1;
            add(categoryCombo, gbc);
            
            // Row 2: Price
            gbc.gridx = 0; gbc.gridy = 2;
            add(new JLabel("Price ($):"), gbc);
            
            gbc.gridx = 1;
            add(priceField, gbc);
            
            // Row 3: Quantity
            gbc.gridx = 0; gbc.gridy = 3;
            add(new JLabel("Quantity:"), gbc);
            
            gbc.gridx = 1;
            add(quantityField, gbc);
            
            // Row 4: Minimum Stock
            gbc.gridx = 0; gbc.gridy = 4;
            add(new JLabel("Min Stock:"), gbc);
            
            gbc.gridx = 1;
            add(minStockField, gbc);
            
            // Row 5: Buttons
            gbc.gridx = 0; gbc.gridy = 5;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, gbc);
        }
        
        private void loadProductData() {
            if (product != null) {
                nameField.setText(product.getName());
                categoryCombo.setSelectedItem(product.getCategory());
                priceField.setText(String.valueOf(product.getPrice()));
                quantityField.setText(String.valueOf(product.getQuantity()));
                minStockField.setText(String.valueOf(product.getMinStock()));
            }
        }
        
        private void saveProduct() {
            try {
                // Validate inputs
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Product name is required");
                }
                
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    throw new IllegalArgumentException("Price must be positive");
                }
                
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity < 0) {
                    throw new IllegalArgumentException("Quantity cannot be negative");
                }
                
                int minStock = Integer.parseInt(minStockField.getText().trim());
                if (minStock < 0) {
                    throw new IllegalArgumentException("Minimum stock cannot be negative");
                }
                
                String category = (String) categoryCombo.getSelectedItem();
                
                // Create or update product
                Product newProduct = new Product();
                newProduct.setName(name);
                newProduct.setCategory(category);
                newProduct.setPrice(price);
                newProduct.setQuantity(quantity);
                newProduct.setMinStock(minStock);
                
                if (product != null) {
                    // Update existing product
                    newProduct.setProductId(product.getProductId());
                    productService.updateProduct(newProduct);
                } else {
                    // Add new product
                    productService.saveProduct(newProduct);
                    
                    // Record initial stock movement
                    if (quantity > 0) {
                        stockMovementService.recordRestockMovement(
                            newProduct.getProductId(),
                            currentUser.getUserId(),
                            quantity,
                            0,
                            "Initial stock"
                        );
                    }
                }
                
                saved = true;
                dispose();
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for price, quantity, and minimum stock",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error saving product: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        public boolean isSaved() {
            return saved;
        }
    }
    
    // Inner class for stock movements dialog
    private class StockMovementsDialog extends JDialog {
        private StockMovementService movementService;
        private int userId;
        private JTable movementsTable;
        private DefaultTableModel tableModel;
        private JComboBox<String> filterCombo;
        
        public StockMovementsDialog(Frame parent, StockMovementService service, int userId) {
            super(parent, "Stock Movement History", true);
            this.movementService = service;
            this.userId = userId;
            
            initComponents();
            layoutComponents();
            loadMovements();
            
            setSize(800, 500);
            setLocationRelativeTo(parent);
        }
        
        private void initComponents() {
            String[] columns = {"Date", "Product ID", "Type", "Change", "From", "To", "Reason"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            movementsTable = new JTable(tableModel);
            movementsTable.setRowHeight(25);
            
            String[] filters = {"All Movements", "Sales", "Restocks", "Adjustments", "My Actions"};
            filterCombo = new JComboBox<>(filters);
            filterCombo.addActionListener(e -> filterMovements());
        }
        
        private void layoutComponents() {
            setLayout(new BorderLayout(10, 10));
            
            // Filter panel
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filterPanel.add(new JLabel("Filter:"));
            filterPanel.add(filterCombo);
            add(filterPanel, BorderLayout.NORTH);
            
            // Table
            JScrollPane scrollPane = new JScrollPane(movementsTable);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Stock Movements"));
            add(scrollPane, BorderLayout.CENTER);
        }
        
        private void loadMovements() {
            SwingWorker<List<StockMovement>, Void> worker = new SwingWorker<List<StockMovement>, Void>() {
                @Override
                protected List<StockMovement> doInBackground() throws Exception {
                    return movementService.getAllMovementHistory();
                }
                
                @Override
                protected void done() {
                    try {
                        List<StockMovement> movements = get();
                        populateTable(movements);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(StockMovementsDialog.this,
                            "Error loading movements: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
        
        private void populateTable(List<StockMovement> movements) {
            tableModel.setRowCount(0);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (StockMovement movement : movements) {
                String change = movement.getQuantityChanged() > 0 ? 
                    "+" + movement.getQuantityChanged() : 
                    String.valueOf(movement.getQuantityChanged());
                
                tableModel.addRow(new Object[]{
                    movement.getTimestamp().format(formatter),
                    movement.getProductId(),
                    movement.getMovementType(),
                    change,
                    movement.getPreviousQuantity(),
                    movement.getNewQuantity(),
                    movement.getReason()
                });
            }
        }
        
        private void filterMovements() {
            String filter = (String) filterCombo.getSelectedItem();
            if ("All Movements".equals(filter)) {
                loadMovements();
                return;
            }
            
            SwingWorker<List<StockMovement>, Void> worker = new SwingWorker<List<StockMovement>, Void>() {
                @Override
                protected List<StockMovement> doInBackground() throws Exception {
                    List<StockMovement> allMovements = movementService.getAllMovementHistory();
                    
                    if ("My Actions".equals(filter)) {
                        allMovements.removeIf(m -> m.getUserId() != userId);
                    } else {
                        String type = filter.toUpperCase().replace("S", ""); // Remove plural
                        allMovements.removeIf(m -> !m.getMovementType().equals(type));
                    }
                    
                    return allMovements;
                }
                
                @Override
                protected void done() {
                    try {
                        List<StockMovement> filtered = get();
                        populateTable(filtered);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(StockMovementsDialog.this,
                            "Error filtering movements: " + e.getMessage(),
                            "Filter Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
        }
    }

    // Inner class for supplier management dialog
    private class SupplierManagementDialog extends JDialog {
        private Connection connection;
        private User currentUser;
        private SupplierDAO supplierDAO;

        private JTable suppliersTable;
        private DefaultTableModel tableModel;
        private JButton addButton, editButton, deleteButton, refreshButton;
        private JTextField searchField;

        public SupplierManagementDialog(Frame parent, Connection connection, User user) {
            super(parent, "Manage Suppliers", true);
            this.connection = connection;
            this.currentUser = user;
            this.supplierDAO = new SupplierDAO(connection);

            initComponents();
            layoutComponents();
            loadSuppliers();

            setSize(900, 500);
            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            // Table setup
            String[] columns = {"ID", "Name", "Contact Person", "Phone", "Email", "Address"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            suppliersTable = new JTable(tableModel);
            suppliersTable.setRowHeight(25);
            suppliersTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

            // Search field
            searchField = new JTextField(20);
            searchField.addActionListener(e -> searchSuppliers());

            // Buttons
            addButton = new JButton("Add Supplier");
            editButton = new JButton("Edit Supplier");
            deleteButton = new JButton("Delete Supplier");
            refreshButton = new JButton("Refresh");

            JButton searchButton = new JButton("Search");
            JButton clearSearchButton = new JButton("Clear");

            // Add action listeners
            addButton.addActionListener(e -> addNewSupplier());
            editButton.addActionListener(e -> editSelectedSupplier());
            deleteButton.addActionListener(e -> deleteSelectedSupplier());
            refreshButton.addActionListener(e -> loadSuppliers());
            searchButton.addActionListener(e -> searchSuppliers());
            clearSearchButton.addActionListener(e -> {
                searchField.setText("");
                loadSuppliers();
            });

            // Initially disable edit/delete buttons
            updateButtonStates();
        }

        private void layoutComponents() {
            setLayout(new BorderLayout(10, 10));

            // Top panel: Search
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            searchPanel.setBorder(new TitledBorder("Search Suppliers"));
            searchPanel.add(new JLabel("Search by name:"));
            searchPanel.add(searchField);
            searchPanel.add(new JButton("Search") {{
                addActionListener(e -> searchSuppliers());
            }});
            searchPanel.add(new JButton("Clear") {{
                addActionListener(e -> {
                    searchField.setText("");
                    loadSuppliers();
                });
            }});

            add(searchPanel, BorderLayout.NORTH);

            // Center: Suppliers table
            JScrollPane tableScroll = new JScrollPane(suppliersTable);
            tableScroll.setBorder(new TitledBorder("Suppliers List"));
            add(tableScroll, BorderLayout.CENTER);

            // Bottom: Action buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(refreshButton);

            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void loadSuppliers() {
            SwingWorker<List<supplies.Supplier>, Void> worker = new SwingWorker<List<supplies.Supplier>, Void>() {
                @Override
                protected List<supplies.Supplier> doInBackground() throws Exception {
                    return supplierDAO.getAllSuppliers();
                }

                @Override
                protected void done() {
                    try {
                        List<supplies.Supplier> suppliers = get();
                        tableModel.setRowCount(0);

                        for (supplies.Supplier supplier : suppliers) {
                            tableModel.addRow(new Object[]{
                                    supplier.getSupplierId(),
                                    supplier.getName(),
                                    supplier.getContactPerson(),
                                    supplier.getPhone(),
                                    supplier.getEmail(),
                                    supplier.getAddress()
                            });
                        }

                        // Update status
                        JOptionPane.showMessageDialog(SupplierManagementDialog.this,
                                "Loaded " + suppliers.size() + " supplier(s)",
                                "Information", JOptionPane.INFORMATION_MESSAGE);

                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(SupplierManagementDialog.this,
                                "Error loading suppliers: " + e.getMessage(),
                                "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        }

        private void searchSuppliers() {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                loadSuppliers();
                return;
            }

            // Filter existing rows in the table for quick search
            DefaultTableModel currentModel = (DefaultTableModel) suppliersTable.getModel();
            int rowCount = currentModel.getRowCount();

            if (rowCount == 0) return;

            List<Object[]> filteredRows = new ArrayList<>();

            for (int i = 0; i < rowCount; i++) {
                String name = currentModel.getValueAt(i, 1).toString().toLowerCase();
                if (name.contains(query.toLowerCase())) {
                    Object[] row = new Object[6];
                    for (int j = 0; j < 6; j++) {
                        row[j] = currentModel.getValueAt(i, j);
                    }
                    filteredRows.add(row);
                }
            }

            // Update table with filtered results
            tableModel.setRowCount(0);
            for (Object[] row : filteredRows) {
                tableModel.addRow(row);
            }

            if (filteredRows.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No suppliers found matching: " + query,
                        "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void addNewSupplier() {
            SupplierFormDialog dialog = new SupplierFormDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    null,
                    supplierDAO
            );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                loadSuppliers();
            }
        }

        private void editSelectedSupplier() {
            int selectedRow = suppliersTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select a supplier to edit",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int supplierId = (int) tableModel.getValueAt(selectedRow, 0);

            try {
                supplies.Supplier supplier = supplierDAO.getSupplierById(supplierId);
                if (supplier != null) {
                    SupplierFormDialog dialog = new SupplierFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            supplier,
                            supplierDAO
                    );
                    dialog.setVisible(true);

                    if (dialog.isSaved()) {
                        loadSuppliers();
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading supplier: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteSelectedSupplier() {
            int selectedRow = suppliersTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select a supplier to delete",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
            String supplierName = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete supplier:\n" +
                            supplierName + " (ID: " + supplierId + ")?\n\n" +
                            "Note: This action cannot be undone.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Check if supplier is being used by any products before deleting
                if (isSupplierUsed(supplierId)) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete supplier " + supplierName +
                                    " because it is associated with existing products.\n" +
                                    "Please reassign products to another supplier first.",
                            "Supplier in Use", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        // In real implementation, you would have a delete method in SupplierDAO
                        // For now, we'll simulate it by preparing the query
                        String query = "DELETE FROM suppliers WHERE supplier_id = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(query)) {
                            stmt.setInt(1, supplierId);
                            return stmt.executeUpdate() > 0;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(SupplierManagementDialog.this,
                                        "Supplier deleted successfully",
                                        "Success", JOptionPane.INFORMATION_MESSAGE);
                                loadSuppliers();
                            } else {
                                JOptionPane.showMessageDialog(SupplierManagementDialog.this,
                                        "Failed to delete supplier",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(SupplierManagementDialog.this,
                                    "Error deleting supplier: " + e.getMessage(),
                                    "Database Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

                worker.execute();
            }
        }

        private boolean isSupplierUsed(int supplierId) {
            // Check if any products reference this supplier
            try {
                String query = "SELECT COUNT(*) FROM products WHERE supplier_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setInt(1, supplierId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1) > 0;
                        }
                    }
                }
            } catch (Exception e) {
                // If error, assume it might be used to prevent accidental deletion
                return true;
            }
            return false;
        }

        private void updateButtonStates() {
            int selectedRow = suppliersTable.getSelectedRow();
            boolean hasSelection = selectedRow >= 0;

            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);

            // Only owners can add/edit/delete suppliers
            boolean isOwner = currentUser != null && "OWNER".equals(currentUser.getRole());
            addButton.setEnabled(isOwner);
            editButton.setEnabled(isOwner && hasSelection);
            deleteButton.setEnabled(isOwner && hasSelection);
        }

        // Inner class for supplier form dialog
        private class SupplierFormDialog extends JDialog {
            private supplies.Supplier supplier;
            private SupplierDAO supplierDAO;
            private boolean saved = false;

            private JTextField nameField, contactField, phoneField, emailField, addressField;
            private JButton saveButton, cancelButton;

            public SupplierFormDialog(Frame parent, supplies.Supplier existingSupplier, SupplierDAO supplierDAO) {
                super(parent, existingSupplier == null ? "Add New Supplier" : "Edit Supplier", true);
                this.supplier = existingSupplier;
                this.supplierDAO = supplierDAO;

                initComponents();
                layoutComponents();

                if (existingSupplier != null) {
                    loadSupplierData();
                }

                setSize(500, 350);
                setLocationRelativeTo(parent);
            }

            private void initComponents() {
                nameField = new JTextField(30);
                contactField = new JTextField(30);
                phoneField = new JTextField(20);
                emailField = new JTextField(30);
                addressField = new JTextField(40);

                saveButton = new JButton(supplier == null ? "Add Supplier" : "Update Supplier");
                cancelButton = new JButton("Cancel");

                saveButton.addActionListener(e -> saveSupplier());
                cancelButton.addActionListener(e -> dispose());
            }

            private void layoutComponents() {
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                // Row 0: Name
                gbc.gridx = 0; gbc.gridy = 0;
                add(new JLabel("Supplier Name:*"), gbc);

                gbc.gridx = 1; gbc.weightx = 1.0;
                add(nameField, gbc);

                // Row 1: Contact Person
                gbc.gridx = 0; gbc.gridy = 1;
                add(new JLabel("Contact Person:"), gbc);

                gbc.gridx = 1;
                add(contactField, gbc);

                // Row 2: Phone
                gbc.gridx = 0; gbc.gridy = 2;
                add(new JLabel("Phone:"), gbc);

                gbc.gridx = 1;
                add(phoneField, gbc);

                // Row 3: Email
                gbc.gridx = 0; gbc.gridy = 3;
                add(new JLabel("Email:"), gbc);

                gbc.gridx = 1;
                add(emailField, gbc);

                // Row 4: Address
                gbc.gridx = 0; gbc.gridy = 4;
                add(new JLabel("Address:"), gbc);

                gbc.gridx = 1;
                add(addressField, gbc);

                // Row 5: Buttons
                gbc.gridx = 0; gbc.gridy = 5;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.CENTER;

                JPanel buttonPanel = new JPanel(new FlowLayout());
                buttonPanel.add(saveButton);
                buttonPanel.add(cancelButton);
                add(buttonPanel, gbc);

                // Add note about required field
                gbc.gridy = 6;
                JLabel noteLabel = new JLabel("* Required field");
                noteLabel.setFont(new Font("Arial", Font.ITALIC, 10));
                noteLabel.setForeground(Color.GRAY);
                add(noteLabel, gbc);
            }

            private void loadSupplierData() {
                if (supplier != null) {
                    nameField.setText(supplier.getName());
                    contactField.setText(supplier.getContactPerson());
                    phoneField.setText(supplier.getPhone());
                    emailField.setText(supplier.getEmail());
                    addressField.setText(supplier.getAddress());
                }
            }

            private void saveSupplier() {
                try {
                    // Validate inputs
                    String name = nameField.getText().trim();
                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("Supplier name is required");
                    }

                    String contactPerson = contactField.getText().trim();
                    String phone = phoneField.getText().trim();
                    String email = emailField.getText().trim();
                    String address = addressField.getText().trim();

                    // Validate email format if provided
                    if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new IllegalArgumentException("Invalid email format");
                    }

                    // Validate phone format if provided
                    if (!phone.isEmpty() && !phone.matches("^[+]?[0-9\\s-]+$")) {
                        throw new IllegalArgumentException("Invalid phone number format");
                    }

                    // Create or update supplier
                    supplies.Supplier newSupplier;
                    if (supplier == null) {
                        // Check for duplicate supplier name
                        if (supplierDAO.supplierNameExists(name)) {
                            throw new IllegalArgumentException("A supplier with this name already exists");
                        }

                        newSupplier = new supplies.Supplier();
                        newSupplier.setName(name);
                        newSupplier.setContactPerson(contactPerson.isEmpty() ? null : contactPerson);
                        newSupplier.setPhone(phone.isEmpty() ? null : phone);
                        newSupplier.setEmail(email.isEmpty() ? null : email);
                        newSupplier.setAddress(address.isEmpty() ? null : address);

                        // Save new supplier
                        boolean success = supplierDAO.createSupplier(newSupplier);
                        if (!success) {
                            throw new Exception("Failed to create supplier");
                        }
                    } else {
                        // Update existing supplier
                        newSupplier = supplier;
                        newSupplier.setName(name);
                        newSupplier.setContactPerson(contactPerson.isEmpty() ? null : contactPerson);
                        newSupplier.setPhone(phone.isEmpty() ? null : phone);
                        newSupplier.setEmail(email.isEmpty() ? null : email);
                        newSupplier.setAddress(address.isEmpty() ? null : address);

                        // Check for duplicate name (excluding current supplier)
                        if (!name.equals(supplier.getName()) && supplierDAO.supplierNameExists(name)) {
                            throw new IllegalArgumentException("A supplier with this name already exists");
                        }

                        boolean success = supplierDAO.updateSupplier(newSupplier);
                        if (!success) {
                            throw new Exception("Failed to update supplier");
                        }
                    }

                    saved = true;
                    JOptionPane.showMessageDialog(this,
                            "Supplier " + (supplier == null ? "added" : "updated") + " successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();

                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this,
                            e.getMessage(),
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving supplier: " + e.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            public boolean isSaved() {
                return saved;
            }
        }
    }
}