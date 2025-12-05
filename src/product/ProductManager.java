package product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import stock.StockMovement;
import stock.StockMovementDAO;

public class ProductManager {
    private ProductService productService;
    private StockMovementDAO stockMovementDAO;
    private Connection connection;
    
    public ProductManager(Connection connection) {
        this.connection = connection;
        this.productService = new ProductService(connection);
        this.stockMovementDAO = new StockMovementDAO(connection);
    }
    
    /**
     * Add a new product and log stock movement
     * SRS 1.1: Add Product, SRS 1.5: Track Stock Movement
     */
    public void addProduct(Product productData) {
        try {
            productService.saveProduct(productData);
            
            // Log stock movement for new product addition
            if (productData.getProductId() > 0 && productData.getQuantity() > 0) {
                logStockMovement(productData.getProductId(), "ADDITION", 
                    productData.getQuantity(), 0, productData.getQuantity(),
                    "New product added to inventory");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update an existing product and log stock movement if quantity changed
     * SRS 1.1: Update Product, SRS 1.5: Track Stock Movement
     */
    public void updateProduct(Product productData) {
        try {
            Product oldProduct = getProductById(productData.getProductId());
            productService.updateProduct(productData);
            
            // Log stock movement if quantity changed
            if (oldProduct != null && oldProduct.getQuantity() != productData.getQuantity()) {
                int quantityDelta = productData.getQuantity() - oldProduct.getQuantity();
                logStockMovement(productData.getProductId(), "ADJUSTMENT",
                    quantityDelta, oldProduct.getQuantity(), productData.getQuantity(),
                    "Product quantity adjusted");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deactivate a product and log the action
     * SRS 1.1: Deactivate Product, SRS 1.5: Track Stock Movement
     */
    public void deactivateProduct(int productId) {
        try {
            Product product = getProductById(productId);
            if (product != null) {
                productService.deleteProduct(productId);
                
                // Log deactivation as stock movement
                logStockMovement(productId, "DEACTIVATION", 0, product.getQuantity(), 0,
                    "Product deactivated - removed from active inventory");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to log stock movements
     * SRS 1.5: Track Stock Movement
     */
    private void logStockMovement(int productId, String movementType, int quantityChanged,
                                  int previousQuantity, int newQuantity, String reason) {
        try {
            if (stockMovementDAO != null) {
                StockMovement movement = new StockMovement();
                movement.setProductId(productId);
                movement.setMovementType(movementType);
                movement.setQuantityChanged(quantityChanged);
                movement.setPreviousQuantity(previousQuantity);
                movement.setNewQuantity(newQuantity);
                movement.setReason(reason);
                movement.setUserId(0); // System user
                
                stockMovementDAO.saveStockMovement(movement);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to log stock movement: " + e.getMessage());
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