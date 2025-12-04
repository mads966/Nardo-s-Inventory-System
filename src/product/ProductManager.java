package product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {
    private ProductService productService;
    
    public ProductManager(Connection connection) {
        this.productService = new ProductService(connection);
    }
    
    public void addProduct(Product productData) {
        try {
            productService.saveProduct(productData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProduct(int productId, Product productData) {
        productData.setProductId(productId);
        try {
            productService.updateProduct(productData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deactivateProduct(int productId) {
        // Instead of deleting, set as inactive or mark for deletion
        try {
            Product product = getProductById(productId);
            if (product != null) {
                // Mark as inactive (implementation depends on your DB schema)
                // For now, just delete
                productService.deleteProduct(productId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Product> searchProducts(String criteria) {
        ProductDAO dao = new ProductDAO();
        try {
            return dao.searchByName(criteria);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Product> filterProducts(String category) {
        ProductDAO dao = new ProductDAO();
        try {
            return dao.filterByCategory(category);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public int getProductStockLevel(int productId) {
        ProductDAO dao = new ProductDAO();
        try {
            // This would need a new method in DAO to get stock by ID
            List<Product> products = dao.searchByName("");
            for (Product p : products) {
                if (p.getProductId() == productId) {
                    return p.getQuantity();
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private Product getProductById(int productId) {
        try {
            return productService.getProductById(productId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}