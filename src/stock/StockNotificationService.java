// File: StockNotificationService.java
package stock;

import product.*;
import alert.*;
import product.Product;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import product.*;
import report.*;
import sale.*;
import stock.*;
import supplies.*;
import login.*;

public class StockNotificationService {
    private ProductDAO productDAO;
    private AlertDAO alertDAO;
    
    public StockNotificationService(Connection connection) {
        this.productDAO = new ProductDAO(connection);
        this.alertDAO = new AlertDAO();
    }

    // No-arg constructor for test-mode
    public StockNotificationService() {
        this(null);
    }
    
    public void checkAllStockLevels() throws SQLException {
        List<Product> products = productDAO.getAllProducts();
        
        for (Product product : products) {
            if (product.isActive() && product.getCurrentQuantity() <= product.getMinStockLevel()) {
                triggerLowStockAlert(product);
            }
        }
    }
    
    public boolean triggerLowStockAlert(Product product) throws SQLException {
        // Check if there's already an unresolved alert for this product
        boolean existingAlert = alertDAO.hasUnresolvedAlert(product.getProductId());
        if (existingAlert) {
            return false; // Alert already exists
        }
        
        LowStockAlert alert = new LowStockAlert(
            0, // Will be set by DAO
            product.getProductId(),
            product.getCurrentQuantity(),
            product.getMinStockLevel(),
            LocalDateTime.now(),
            false
        );
        
        return alertDAO.createAlert(alert);
    }
    
    public boolean resolveAlert(int alertId) throws SQLException {
        return alertDAO.resolveAlert(alertId);
    }
    
    public List<LowStockAlert> getActiveAlerts() throws SQLException {
        return alertDAO.getActiveAlerts();
    }
    
    public List<LowStockAlert> getAlertsByProduct(int productId) throws SQLException {
        return alertDAO.getAlertsByProduct(productId);
    }
    
    public void clearResolvedAlerts() throws SQLException {
        alertDAO.deleteResolvedAlerts();
    }
    
    public int getActiveAlertCount() throws SQLException {
        return alertDAO.getActiveAlertCount();
    }
    
    public boolean shouldTriggerAlert(Product product) {
        return product.getCurrentQuantity() <= product.getMinStockLevel();
    }
    
    public int calculateReorderQuantity(Product product) {
        int minStock = product.getMinStockLevel();
        int currentStock = product.getCurrentQuantity();
        int shortage = minStock - currentStock;
        
        // Order at least 2x the shortage or minimum of 10 units
        return Math.max(shortage * 2, 10);
    }
}