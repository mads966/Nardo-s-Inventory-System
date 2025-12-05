package sale;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import product.*;
import stock.*;
import login.User;
import login.UserRole;

public class SalesProcessor {
    private SaleDAO saleDAO;
    private ProductDAO productDAO;
    private StockMovementDAO stockMovementDAO;
    private Connection connection;
    private User currentUser;
    
    public SalesProcessor(Connection connection) {
        this.connection = connection;
        this.saleDAO = new SaleDAO(connection);
        this.productDAO = new ProductDAO(connection);
        this.stockMovementDAO = new StockMovementDAO(connection);
    }
    
    // Process sale and deduct inventory
    public Sale processSale(Sale sale) throws Exception {
        try {
            // Validate sale
            validateSale(sale);
            
            // Check stock availability
            checkStockAvailability(sale);
            
            // If no DB connection, use test mode
            if (connection == null) {
                return processSaleTestMode(sale);
            }

            // Start transaction
            connection.setAutoCommit(false);

            try {
                // Save sale to database
                int saleId = saleDAO.saveSale(sale);
                sale.setSaleId(saleId);

                // Deduct inventory for each item
                for (SaleItem item : sale.getItems()) {
                    deductInventory(item, saleId);
                }

                // Mark sale as completed
                sale.setCompleted(true);
                saleDAO.updateSaleStatus(saleId, Sale.STATUS_COMPLETED, true);

                // Commit transaction
                connection.commit();

                // Log successful sale
                logSaleTransaction(sale);

                return sale;

            } catch (Exception e) {
                // Rollback on error
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        // Ignore rollback error
                    }
                }
                throw new Exception("Failed to process sale: " + e.getMessage(), e);
            } finally {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            }
            
        } catch (SQLException e) {
            throw new Exception("Database error processing sale: " + e.getMessage(), e);
        }
    }
    
    // Test mode processing
    private Sale processSaleTestMode(Sale sale) {
        int mockSaleId = (int) (Math.random() * 100000);
        sale.setSaleId(mockSaleId);
        
        for (SaleItem item : sale.getItems()) {
            productDAO.updateProductQuantity(item.getProductId(), -item.getQuantity());
        }
        
        sale.setCompleted(true);
        logSaleTransaction(sale);
        return sale;
    }
    
    // Validate sale before processing
    private void validateSale(Sale sale) throws Exception {
        if (sale == null) {
            throw new Exception("Sale cannot be null");
        }
        
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new Exception("Sale must contain at least one item");
        }
        
        if (sale.getUserId() <= 0) {
            throw new Exception("Invalid user ID");
        }
        
        if (sale.getTotalAmount() <= 0) {
            throw new Exception("Sale total must be greater than zero");
        }
        
        // Validate each item
        for (SaleItem item : sale.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new Exception("Item quantity must be greater than zero");
            }
            
            if (item.getUnitPrice() <= 0) {
                throw new Exception("Item price must be greater than zero");
            }
        }
    }
    
    // Check stock availability
    private void checkStockAvailability(Sale sale) throws Exception {
        for (SaleItem item : sale.getItems()) {
            int availableStock = productDAO.getProductStockLevel(item.getProductId());
            
            if (availableStock < item.getQuantity()) {
                throw new Exception(String.format(
                    "Insufficient stock for %s. Available: %d, Requested: %d",
                    item.getProductName(), availableStock, item.getQuantity()));
            }
        }
    }
    
    // Deduct inventory for a sale item
    private void deductInventory(SaleItem item, int saleId) throws SQLException {
        // Get current stock
        int previousQuantity = productDAO.getProductStockLevel(item.getProductId());
        
        // Update product quantity
        boolean updated = productDAO.updateProductQuantity(item.getProductId(), -item.getQuantity());
        
        if (!updated && connection != null) {
            throw new SQLException("Failed to update product quantity");
        }
        
        // Get new stock level
        int newQuantity = productDAO.getProductStockLevel(item.getProductId());
        
        // Create stock movement record
        if (stockMovementDAO != null) {
            StockMovement movement = new StockMovement();
            movement.setProductId(item.getProductId());
            movement.setRelatedId(saleId);
            movement.setMovementType("SALE");
            movement.setQuantityChanged(-item.getQuantity());
            movement.setPreviousQuantity(previousQuantity);
            movement.setNewQuantity(newQuantity);
            movement.setReason("Sale transaction");
            movement.setUserId(item.getSaleId()); // Using saleId as user placeholder
            
            try {
                stockMovementDAO.saveStockMovement(movement);
            } catch (Exception e) {
                // Log but don't fail sale if stock movement fails
                System.out.println("Warning: Failed to log stock movement: " + e.getMessage());
            }
        }
    }
    
    // Get today's sales statistics
    public SaleDAO.SalesStatistics getTodayStatistics() throws Exception {
        try {
            LocalDate today = LocalDate.now();
            return saleDAO.getSalesStatistics(today, today);
        } catch (SQLException e) {
            // Return empty stats for test mode
            SaleDAO.SalesStatistics stats = new SaleDAO.SalesStatistics();
            stats.setTotalSales(0);
            stats.setTotalRevenue(0);
            stats.setAverageSale(0);
            stats.setTotalItems(0);
            return stats;
        }
    }
    
    // Get sales by date range
    public java.util.List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        try {
            return saleDAO.getSalesByDateRange(startDate, endDate);
        } catch (SQLException e) {
            // Return empty list for test mode
            return new java.util.ArrayList<>();
        }
    }
    
    // Get all sales
    public java.util.List<Sale> getAllSales() throws Exception {
        try {
            return saleDAO.getAllSales();
        } catch (SQLException e) {
            // Return empty list for test mode
            return new java.util.ArrayList<>();
        }
    }
    
    // Get sale by ID
    public Sale getSaleById(int saleId) throws Exception {
        try {
            return saleDAO.getSaleById(saleId);
        } catch (SQLException e) {
            // Return mock sale for test mode
            Sale mockSale = Sale.createNewSale(1, "Test User");
            mockSale.setSaleId(saleId);
            mockSale.setReceiptNumber("TEST-" + saleId);
            return mockSale;
        }
    }
    
    // Quick sale - 3-click sales
    public Sale quickSale(int productId, int quantity, int userId, String userName) throws Exception {
        try {
            // Get product details
            Product product = productDAO.getProductById(productId);
            if (product == null) {
                throw new Exception("Product not found: " + productId);
            }
            
            // Create sale with single item
            Sale sale = Sale.createNewSale(userId, userName);
            SaleItem item = SaleItem.create(
                productId,
                product.getName(),
                product.getCategory().toString(),
                quantity,
                product.getPrice()
            );
            sale.addItem(item);
            
            // Process sale
            return processSale(sale);
            
        } catch (Exception e) {
            throw new Exception("Quick sale failed: " + e.getMessage(), e);
        }
    }
    
    // Log sale transaction
    private void logSaleTransaction(Sale sale) {
        System.out.println("[" + LocalDateTime.now() + "] Sale processed: #" + 
                          sale.getSaleId() + " - $" + sale.getTotalAmount());
    }
}