package product;

import enums.Category;
import login.User;
import login.UserRole;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * ProductManagementForm - Complete GUI for managing products.
 * Features: Search, filter, add, edit, deactivate with role-based access control.
 * SRS Requirements: 1.1 (Add/Edit/Deactivate), 1.4 (Search), 1.6 (Category filter)
 */
public class ProductManagementForm extends JFrame {
    private JTable productTable;
    private JTextField searchField;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JButton searchButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deactivateButton;
    private JButton refreshButton;
    private DefaultTableModel tableModel;
    private ProductDAO productDAO;
    private ProductManager productManager;
    private User currentUser;
    private JLabel statusLabel;
    private long searchStartTime;
    private static final long SEARCH_TIMEOUT_MS = 10000; // 10 seconds per SRS

    public ProductManagementForm(User currentUser) {
        this.currentUser = currentUser;
        this.productDAO = new ProductDAO();
        this.productManager = new ProductManager(null); // Connection handled by DAO
        
        setTitle("Product Management - " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        layoutComponents();
        loadProducts();
        
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        // Table setup
        String[] columnNames = {"Product ID", "Name", "Category", "Price", "Quantity", "Min Stock", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setDefaultEditor(Object.class, null); // Disable editing
        
        // Search field with autocomplete trigger
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        // Category filter
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.addItem("All Categories");
        for (Category cat : Category.values()) {
            categoryFilterCombo.addItem(cat.toString());
        }
        
        // Status filter
        statusFilterCombo = new JComboBox<>();
        statusFilterCombo.addItem("All Status");
        statusFilterCombo.addItem("Active");
        statusFilterCombo.addItem("Inactive");
        
        // Buttons
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        
        addButton = new JButton("Add Product");
        addButton.addActionListener(e -> addNewProduct());
        addButton.setEnabled(currentUser.getRole() == UserRole.OWNER);
        
        editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelectedProduct());
        editButton.setEnabled(currentUser.getRole() == UserRole.OWNER);
        
        deactivateButton = new JButton("Deactivate");
        deactivateButton.addActionListener(e -> deactivateSelectedProduct());
        deactivateButton.setEnabled(currentUser.getRole() == UserRole.OWNER);
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadProducts());
        
        statusLabel = new JLabel("Ready");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Search & Filter"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Top search panel
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(new JSeparator());
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryFilterCombo);
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusFilterCombo);
        searchPanel.add(refreshButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deactivateButton);
        
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Center table with proper sizing
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Status panel with padding
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }
    
    /**
     * Load all products into the table (SRS 1.1, 1.4)
     */
    private void loadProducts() {
        try {
            searchStartTime = System.currentTimeMillis();
            statusLabel.setText("Loading products...");
            tableModel.setRowCount(0);
            
            List<Product> products = productDAO.getAllProducts();
            for (Product p : products) {
                addProductRow(p);
            }
            
            long elapsed = System.currentTimeMillis() - searchStartTime;
            statusLabel.setText("Loaded " + products.size() + " products in " + elapsed + "ms");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error loading products");
        }
    }
    
    /**
     * Perform search with validation (SRS 1.4)
     */
    private void performSearch() {
        String criteria = searchField.getText().trim();
        
        // Validate search criteria
        if (!InputValidator.validateSearchCriteria(criteria)) {
            JOptionPane.showMessageDialog(this, "Invalid search criteria. Max 50 characters, no SQL keywords.",
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            searchStartTime = System.currentTimeMillis();
            statusLabel.setText("Searching...");
            tableModel.setRowCount(0);
            
            List<Product> products;
            if (criteria.isEmpty()) {
                products = productDAO.getAllProducts();
            } else {
                products = productDAO.searchByName(criteria);
            }
            
            // Apply category filter
            String selectedCategory = (String) categoryFilterCombo.getSelectedItem();
            if (!selectedCategory.equals("All Categories")) {
                products = filterByCategory(products, selectedCategory);
            }
            
            // Apply status filter
            String selectedStatus = (String) statusFilterCombo.getSelectedItem();
            if (!selectedStatus.equals("All Status")) {
                products = filterByStatus(products, selectedStatus.equals("Active"));
            }
            
            for (Product p : products) {
                addProductRow(p);
            }
            
            long elapsed = System.currentTimeMillis() - searchStartTime;
            if (elapsed > 10000) {
                JOptionPane.showMessageDialog(this, "Search took " + elapsed + "ms. Consider narrowing your criteria.",
                    "Performance Warning", JOptionPane.WARNING_MESSAGE);
            }
            statusLabel.setText("Found " + products.size() + " products in " + elapsed + "ms");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching products: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Search error");
        }
    }
    
    private List<Product> filterByCategory(List<Product> products, String category) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                filtered.add(p);
            }
        }
        return filtered;
    }
    
    private List<Product> filterByStatus(List<Product> products, boolean isActive) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : products) {
            if (p.isActive() == isActive) {
                filtered.add(p);
            }
        }
        return filtered;
    }
    
    private void addProductRow(Product p) {
        Object[] row = {
            p.getProductId(),
            p.getName(),
            p.getCategory(),
            "$" + String.format("%.2f", p.getPrice()),
            p.getQuantity(),
            p.getMinStock(),
            p.isActive() ? "Active" : "Inactive"
        };
        tableModel.addRow(row);
    }
    
    /**
     * Add a new product (OWNER only) - SRS 1.1
     */
    private void addNewProduct() {
        if (currentUser.getRole() != UserRole.OWNER) {
            JOptionPane.showMessageDialog(this, "Only OWNER can add products.",
                "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show dialog for product input
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input fields
        JTextField nameField = new JTextField();
        JComboBox<String> categoryCombo = new JComboBox<>();
        for (Category c : Category.values()) {
            categoryCombo.addItem(c.toString());
        }
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField minStockField = new JTextField();
        JTextField supplierIdField = new JTextField();
        
        panel.add(new JLabel("Product Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryCombo);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Min Stock:"));
        panel.add(minStockField);
        panel.add(new JLabel("Supplier ID:"));
        panel.add(supplierIdField);
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String category = (String) categoryCombo.getSelectedItem();
            String priceStr = priceField.getText();
            String quantityStr = quantityField.getText();
            String minStockStr = minStockField.getText();
            String supplierIdStr = supplierIdField.getText();
            
            // Validate all inputs
            if (!InputValidator.validateProductName(name)) {
                JOptionPane.showMessageDialog(dialog, "Invalid product name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!InputValidator.validateCategory(category)) {
                JOptionPane.showMessageDialog(dialog, "Invalid category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!InputValidator.validatePrice(priceStr)) {
                JOptionPane.showMessageDialog(dialog, "Invalid price (0 < price <= 999999.99).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!InputValidator.validateQuantity(quantityStr)) {
                JOptionPane.showMessageDialog(dialog, "Invalid quantity (0-999999).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!InputValidator.validateMinStock(minStockStr)) {
                JOptionPane.showMessageDialog(dialog, "Invalid minimum stock (0-10000).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!InputValidator.validateSupplierId(supplierIdStr)) {
                JOptionPane.showMessageDialog(dialog, "Invalid supplier ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create and save product
            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setPrice(Double.parseDouble(priceStr));
            product.setQuantity(Integer.parseInt(quantityStr));
            product.setMinStock(Integer.parseInt(minStockStr));
            if (!supplierIdStr.isEmpty()) {
                product.setSupplierId(Integer.parseInt(supplierIdStr));
            }
            product.setActive(true);
            
            try {
                productManager.addProduct(product);
                JOptionPane.showMessageDialog(dialog, "Product added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadProducts();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        panel.add(saveButton);
        panel.add(cancelButton);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Edit selected product (OWNER only) - SRS 1.1
     */
    private void editSelectedProduct() {
        if (currentUser.getRole() != UserRole.OWNER) {
            JOptionPane.showMessageDialog(this, "Only OWNER can edit products.",
                "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        try {
            Product product = productDAO.getProductById(productId);
            if (product == null) {
                JOptionPane.showMessageDialog(this, "Product not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // TODO: Implement edit dialog similar to add product
            JOptionPane.showMessageDialog(this, "Edit functionality for Product ID: " + productId,
                "Edit Product", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading product: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Deactivate selected product (OWNER only) - SRS 1.1
     */
    private void deactivateSelectedProduct() {
        if (currentUser.getRole() != UserRole.OWNER) {
            JOptionPane.showMessageDialog(this, "Only OWNER can deactivate products.",
                "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to deactivate.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to deactivate this product? It will remain in history.",
            "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productManager.deactivateProduct(productId);
                JOptionPane.showMessageDialog(this, "Product deactivated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deactivating product: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // For testing - create a test user
            User testOwner = new User(1, "testOwner", "hash", UserRole.OWNER, "test@nardo.com", true);
            new ProductManagementForm(testOwner).setVisible(true);
        });
    }
}