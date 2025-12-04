package product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;
    private final Connection connection;
    
    public ProductService(Connection connection) {
        this.connection = connection;
        this.productDAO = new ProductDAO();
    }

    public Product getProductById(int productId) throws SQLException {
        return productDAO.getProductById(productId);
    }

    public int getTotalProductCount() {
        try {
            return productDAO.getTotalProductCount();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get total product count: " + e.getMessage(), e);
        }
    }

    public int getLowStockCount() {
        try {
            return productDAO.getLowStockCount();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get low stock count: " + e.getMessage(), e);
        }
    }

    public List<Product> getLowStockProducts() {
        try {
            return productDAO.getLowStockProducts();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get low stock products: " + e.getMessage(), e);
        }
    }

    public int getActiveAlertCount() {
        // Active alerts are low stock items
        return getLowStockCount();
    }

    public java.util.List<Product> getAllActiveProducts() {
        try {
            return productDAO.getAllProducts();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get active products: " + e.getMessage(), e);
        }
    }

    // Additional methods that might be needed
    public void saveProduct(Product product) throws SQLException {
        productDAO.saveProduct(product);
    }
    
    public void updateProduct(Product product) throws SQLException {
        productDAO.updateProduct(product);
    }
    
    public void deleteProduct(int productId) throws SQLException {
        productDAO.deleteProduct(productId);
    }

    public List<Product> searchProducts(String query) {
        try {
            return productDAO.searchByName(query);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}