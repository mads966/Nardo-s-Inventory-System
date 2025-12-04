
package alert;

import product.Product;
import product.ProductDAO;
import stock.LowStockAlert;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertManager {
    private final ProductDAO productDAO;
    private final AlertDAO alertDAO;

    public AlertManager() {
        this.productDAO = new ProductDAO();
        this.alertDAO = new AlertDAO();
    }

    public void checkStockLevels() {
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product product : products) {
                if (product.isActive() && product.getQuantity() <= product.getMinStock()) {
                    if (!hasUnresolvedAlert(product.getProductId())) {
                        triggerLowStockAlert(product);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock levels: " + e.getMessage());
        }
    }

    public LowStockAlert triggerLowStockAlert(Product product) {
        try {
            LowStockAlert alert = new LowStockAlert(
                    0,
                    product.getProductId(),
                    product.getQuantity(),
                    product.getMinStock(),
                    LocalDateTime.now(),
                    false
            );

            alertDAO.createAlert(alert);
            System.out.println("ALERT: Low stock for " + product.getName() +
                    " (Current: " + product.getQuantity() +
                    ", Min: " + product.getMinStock() + ")");
            return alert;
        } catch (SQLException e) {
            System.err.println("Error triggering low stock alert: " + e.getMessage());
            return null;
        }
    }

    public boolean resolveAlert(int alertId) {
        try {
            return alertDAO.resolveAlert(alertId);
        } catch (SQLException e) {
            System.err.println("Error resolving alert: " + e.getMessage());
            return false;
        }
    }

    public List<LowStockAlert> getActiveAlerts() {
        try {
            return alertDAO.getActiveAlerts();
        } catch (SQLException e) {
            System.err.println("Error getting active alerts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<LowStockAlert> getAlertsByProduct(int productId) {
        try {
            return alertDAO.getAlertsByProduct(productId);
        } catch (SQLException e) {
            System.err.println("Error getting alerts by product: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int getActiveAlertCount() {
        try {
            return alertDAO.getActiveAlertCount();
        } catch (SQLException e) {
            System.err.println("Error getting alert count: " + e.getMessage());
            return 0;
        }
    }

    public void clearResolvedAlerts() {
        try {
            alertDAO.deleteResolvedAlerts();
        } catch (SQLException e) {
            System.err.println("Error clearing resolved alerts: " + e.getMessage());
        }
    }

    private boolean hasUnresolvedAlert(int productId) {
        try {
            return alertDAO.hasUnresolvedAlert(productId);
        } catch (SQLException e) {
            System.err.println("Error checking unresolved alerts: " + e.getMessage());
            return false;
        }
    }
}