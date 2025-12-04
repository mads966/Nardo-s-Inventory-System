package sale;
import product.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.List;

public class SalesPanel extends JPanel {
    private SalesProcessor salesProcessor;
    private ProductService productService;
    private Connection connection;
    
    private JComboBox<String> productCombo;
    private JSpinner quantitySpinner;
    private JButton quickSaleButton;
    private JButton viewSalesButton;
    private JButton newSaleButton;
    
    private JLabel todaySalesLabel;
    private JLabel todayRevenueLabel;
    private JLabel todayItemsLabel;
    
    private JTable recentSalesTable;
    private DefaultTableModel recentSalesModel;
    
    private int currentUserId;
    private String currentUserName;
    
    public SalesPanel(Connection connection, int userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.connection = connection;
        this.salesProcessor = new SalesProcessor(connection);
        this.productService = new ProductService(connection);
        
        initComponents();
        layoutComponents();
        setupListeners();
        loadDashboardData();
        loadProductCombo();
    }
    
    private void initComponents() {
        // Product selection for quick sales
        productCombo = new JComboBox<>();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        
        // Quick sale button (3-click sales - SRS 3.3.1)
        quickSaleButton = new JButton("Quick Sale (3 Clicks)");
        quickSaleButton.setBackground(new Color(76, 175, 80));
        quickSaleButton.setForeground(Color.WHITE);
        quickSaleButton.setFont(new Font("Arial", Font.BOLD, 12));
        quickSaleButton.setToolTipText("1. Select product 2. Enter quantity 3. Click to process");
        
        viewSalesButton = new JButton("View Sales History");
        newSaleButton = new JButton("New Detailed Sale");
        
        // Statistics labels
        todaySalesLabel = new JLabel("0", SwingConstants.CENTER);
        todaySalesLabel.setFont(new Font("Arial", Font.BOLD, 24));
        todaySalesLabel.setForeground(new Color(33, 150, 243));
        
        todayRevenueLabel = new JLabel("$0.00", SwingConstants.CENTER);
        todayRevenueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        todayRevenueLabel.setForeground(new Color(76, 175, 80));
        
        todayItemsLabel = new JLabel("0", SwingConstants.CENTER);
        todayItemsLabel.setFont(new Font("Arial", Font.BOLD, 24));
        todayItemsLabel.setForeground(new Color(255, 152, 0));
        
        // Recent sales table
        String[] columns = {"Time", "Receipt #", "Items", "Total", "Status"};
        recentSalesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentSalesTable = new JTable(recentSalesModel);
        recentSalesTable.setRowHeight(25);
        recentSalesTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        recentSalesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        recentSalesTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        recentSalesTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        recentSalesTable.getColumnModel().getColumn(4).setPreferredWidth(80);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top: Quick sales panel
        JPanel quickSalesPanel = new JPanel(new GridBagLayout());
        quickSalesPanel.setBorder(new TitledBorder("Quick Sale (3 Clicks)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 1: Product selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        quickSalesPanel.add(new JLabel("Product:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        productCombo.setPreferredSize(new Dimension(200, 25));
        quickSalesPanel.add(productCombo, gbc);
        
        // Row 2: Quantity
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        quickSalesPanel.add(new JLabel("Quantity:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        quickSalesPanel.add(quantitySpinner, gbc);
        
        // Row 3: Quick sale button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        quickSalesPanel.add(quickSaleButton, gbc);
        
        // Row 4: Other buttons
        gbc.gridy = 3;
        JPanel otherButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        otherButtons.add(viewSalesButton);
        otherButtons.add(newSaleButton);
        quickSalesPanel.add(otherButtons, gbc);
        
        add(quickSalesPanel, BorderLayout.NORTH);
        
        // Center: Split pane for stats and recent sales
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setResizeWeight(0.3);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(new TitledBorder("Today's Statistics"));
        
        JPanel salesPanel = createStatPanel("Sales", todaySalesLabel, "transactions");
        JPanel revenuePanel = createStatPanel("Revenue", todayRevenueLabel, "total");
        JPanel itemsPanel = createStatPanel("Items Sold", todayItemsLabel, "units");
        
        statsPanel.add(salesPanel);
        statsPanel.add(revenuePanel);
        statsPanel.add(itemsPanel);
        
        // Recent sales panel
        JPanel recentSalesPanel = new JPanel(new BorderLayout(5, 5));
        recentSalesPanel.setBorder(new TitledBorder("Recent Sales"));
        
        JScrollPane tableScroll = new JScrollPane(recentSalesTable);
        tableScroll.setPreferredSize(new Dimension(600, 150));
        recentSalesPanel.add(tableScroll, BorderLayout.CENTER);
        
        centerSplit.setTopComponent(statsPanel);
        centerSplit.setBottomComponent(recentSalesPanel);
        
        add(centerSplit, BorderLayout.CENTER);
    }
    
    private JPanel createStatPanel(String title, JLabel valueLabel, String unit) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel unitLabel = new JLabel(unit, SwingConstants.CENTER);
        unitLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        unitLabel.setForeground(Color.GRAY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(unitLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupListeners() {
        quickSaleButton.addActionListener(e -> processQuickSale());
        viewSalesButton.addActionListener(e -> viewSalesHistory());
        newSaleButton.addActionListener(e -> openNewSaleWindow());
        
        // Double-click on recent sales to view details
        recentSalesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewSelectedSale();
                }
            }
        });
    }
    
    private void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private SaleDAO.SalesStatistics stats;
            private java.util.List<Sale> recentSales;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    stats = salesProcessor.getTodayStatistics();
                    recentSales = salesProcessor.getSalesByDateRange(
                        java.time.LocalDate.now(),
                        java.time.LocalDate.now()
                    );
                } catch (Exception e) {
                    // Handle error quietly
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    if (stats != null) {
                        todaySalesLabel.setText(String.valueOf(stats.getTotalSales()));
                        todayRevenueLabel.setText(String.format("$%.2f", stats.getTotalRevenue()));
                        todayItemsLabel.setText(String.valueOf(stats.getTotalItems()));
                    }
                    
                    if (recentSales != null) {
                        loadRecentSales(recentSales);
                    }
                } catch (Exception e) {
                    // Handle error
                }
            }
        };
        
        worker.execute();
    }
    
    private void loadRecentSales(java.util.List<Sale> sales) {
        recentSalesModel.setRowCount(0);
        
        // Show last 10 sales
        int count = Math.min(sales.size(), 10);
        for (int i = 0; i < count; i++) {
            Sale sale = sales.get(i);
            
            recentSalesModel.addRow(new Object[]{
                sale.getSaleDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                sale.getReceiptNumber(),
                sale.getItems().size(),
                String.format("$%.2f", sale.getTotalAmount()),
                sale.getPaymentStatus()
            });
        }
    }
    
    private void loadProductCombo() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Product> products;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    products = productService.getAllActiveProducts();
                } catch (Exception e) {
                    // Handle error
                }
                return null;
            }
            
            @Override
            protected void done() {
                productCombo.removeAllItems();
                
                if (products != null) {
                    for (Product product : products) {
                        if (product.getQuantity() > 0) {
                            productCombo.addItem(product.getName() + " ($" + 
                                String.format("%.2f", product.getPrice()) + ") - ID: " + 
                                product.getProductId());
                        }
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    public void processQuickSale() {
        String selected = (String) productCombo.getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select a product",
                "No Product Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extract product ID from combo box text
        String[] parts = selected.split("ID: ");
        if (parts.length < 2) {
            JOptionPane.showMessageDialog(this,
                "Invalid product selection",
                "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int productId = Integer.parseInt(parts[1].trim());
            int quantity = (int) quantitySpinner.getValue();
            
            // Process quick sale
            ProgressDialog progress = new ProgressDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Processing Quick Sale...");
            progress.setVisible(true);
            
            SwingWorker<Sale, Void> worker = 
                new SwingWorker<Sale, Void>() {
                @Override
                protected Sale doInBackground() throws Exception {
                    return salesProcessor.quickSale(productId, quantity, currentUserId, currentUserName);
                }
                
                @Override
                protected void done() {
                    progress.dispose();
                    
                    try {
                        Sale sale = get();
                        
                        // Show success
                        JOptionPane.showMessageDialog(SalesPanel.this,
                            "Quick sale processed!\n" +
                            "Receipt #: " + sale.getReceiptNumber() + "\n" +
                            "Total: $" + sale.getTotalAmount(),
                            "Sale Complete", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload dashboard data
                        loadDashboardData();
                        loadProductCombo();
                        
                        // Reset quantity
                        quantitySpinner.setValue(1);
                        
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(SalesPanel.this,
                            "Quick sale failed: " + e.getMessage(),
                            "Sale Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid product ID",
                "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewSalesHistory() {
        // Open sales history window
        JOptionPane.showMessageDialog(this,
            "Sales history would open in a new window",
            "Sales History", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openNewSaleWindow() {
        // Create and show sales processing form
        JFrame frame = new JFrame("Sales Processing");
        SalesProcessingForm salesForm = new SalesProcessingForm(
            connection, currentUserId, currentUserName);
        frame.setContentPane(salesForm);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }
    
    private void viewSelectedSale() {
        int selectedRow = recentSalesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String receiptNumber = (String) recentSalesModel.getValueAt(selectedRow, 1);
            
            JOptionPane.showMessageDialog(this,
                "Viewing sale: " + receiptNumber + "\n" +
                "This would open sale details in a new window.",
                "Sale Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Update dashboard data (call periodically)
    public void refreshDashboard() {
        loadDashboardData();
    }
    
    // Get quick sale statistics for dashboard display
    public String getQuickStats() {
        return String.format("Sales: %s | Revenue: %s | Items: %s",
            todaySalesLabel.getText(),
            todayRevenueLabel.getText(),
            todayItemsLabel.getText());
    }
    
    // Progress dialog
    private static class ProgressDialog extends JDialog {
        private JProgressBar progressBar;
        
        public ProgressDialog(Frame parent, String message) {
            super(parent, "Processing", true);
            
            setLayout(new BorderLayout(10, 10));
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);
            
            JLabel label = new JLabel(message, SwingConstants.CENTER);
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