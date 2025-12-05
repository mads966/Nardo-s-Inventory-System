package product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchFilterService - SRS Requirement 1.4: Search and Filter Component
 * 
 * Provides unified search and filtering functionality for products.
 * Supports searching by:
 * - Product name
 * - Category
 * - Supplier
 * - Stock level
 * - Advanced combined searches
 */
public class SearchFilterService {
    private final ProductDAO productDAO;
    
    public SearchFilterService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }
    
    /**
     * Search products by name (case-insensitive, partial match)
     * SRS 1.4: Search by product name
     * 
     * @param name Product name or partial name to search for
     * @return List of matching products
     */
    public List<Product> searchByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return productDAO.searchByName(name.trim());
    }
    
    /**
     * Filter products by category
     * SRS 1.4: Filter by category
     * 
     * @param category Category name to filter by
     * @return List of products in the specified category
     */
    public List<Product> filterByCategory(String category) throws SQLException {
        if (category == null || category.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return productDAO.filterByCategory(category.trim());
    }
    
    /**
     * Filter products by supplier
     * SRS 1.4: Filter by supplier
     * 
     * @param supplierId Supplier ID to filter by
     * @return List of products from the specified supplier
     */
    public List<Product> filterBySupplier(int supplierId) throws SQLException {
        if (supplierId <= 0) {
            return new ArrayList<>();
        }
        
        List<Product> allProducts = productDAO.getAllProducts();
        List<Product> filtered = new ArrayList<>();
        
        for (Product product : allProducts) {
            if (product.getSupplierId() != null && product.getSupplierId() == supplierId) {
                filtered.add(product);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter products by stock level range
     * SRS 1.4: Filter by stock level
     * 
     * @param minStock Minimum stock level (inclusive)
     * @param maxStock Maximum stock level (inclusive)
     * @return List of products within the specified stock range
     */
    public List<Product> filterByStockLevel(int minStock, int maxStock) throws SQLException {
        if (minStock < 0 || maxStock < 0 || minStock > maxStock) {
            return new ArrayList<>();
        }
        
        List<Product> allProducts = productDAO.getAllProducts();
        List<Product> filtered = new ArrayList<>();
        
        for (Product product : allProducts) {
            if (product.getQuantity() >= minStock && product.getQuantity() <= maxStock) {
                filtered.add(product);
            }
        }
        
        return filtered;
    }
    
    /**
     * Filter products that are below minimum stock level (low stock items)
     * SRS 1.2: Low stock detection
     * 
     * @return List of products below their minimum stock threshold
     */
    public List<Product> filterLowStockProducts() throws SQLException {
        return productDAO.getLowStockProducts();
    }
    
    /**
     * Advanced search with multiple criteria
     * Combines name search with category and stock level filters
     * 
     * @param name Product name to search for (can be null)
     * @param category Category to filter by (can be null)
     * @param minStock Minimum stock level (use -1 to ignore)
     * @param maxStock Maximum stock level (use -1 to ignore)
     * @return List of products matching all specified criteria
     */
    public List<Product> advancedSearch(String name, String category, int minStock, int maxStock) 
            throws SQLException {
        List<Product> results = productDAO.getAllProducts();
        
        // Filter by name if provided
        if (name != null && !name.trim().isEmpty()) {
            String searchTerm = name.trim().toLowerCase();
            results = filterList(results, product -> 
                product.getName().toLowerCase().contains(searchTerm));
        }
        
        // Filter by category if provided
        if (category != null && !category.trim().isEmpty()) {
            String categoryFilter = category.trim();
            results = filterList(results, product -> 
                product.getCategory().equalsIgnoreCase(categoryFilter));
        }
        
        // Filter by stock level if provided
        if (minStock >= 0 && maxStock >= 0 && minStock <= maxStock) {
            results = filterList(results, product -> 
                product.getQuantity() >= minStock && product.getQuantity() <= maxStock);
        }
        
        return results;
    }
    
    /**
     * Search across multiple fields (name, category, supplier)
     * Useful for global search functionality
     * 
     * @param query Search query
     * @return List of products matching the query in any field
     */
    public List<Product> globalSearch(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchTerm = query.trim().toLowerCase();
        List<Product> allProducts = productDAO.getAllProducts();
        List<Product> results = new ArrayList<>();
        
        for (Product product : allProducts) {
            // Search in name
            if (product.getName().toLowerCase().contains(searchTerm)) {
                results.add(product);
                continue;
            }
            
            // Search in category
            if (product.getCategory().toLowerCase().contains(searchTerm)) {
                results.add(product);
                continue;
            }
            
            // Search in product ID
            if (String.valueOf(product.getProductId()).contains(searchTerm)) {
                results.add(product);
            }
        }
        
        return results;
    }
    
    /**
     * Get all active products (default list)
     * 
     * @return List of all active products
     */
    public List<Product> getAllActiveProducts() throws SQLException {
        return productDAO.getAllProducts();
    }
    
    /**
     * Helper method to filter a list based on a predicate
     */
    private List<Product> filterList(List<Product> products, ProductPredicate predicate) {
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (predicate.test(product)) {
                filtered.add(product);
            }
        }
        return filtered;
    }
    
    /**
     * Functional interface for filtering products
     */
    @FunctionalInterface
    private interface ProductPredicate {
        boolean test(Product product);
    }
}
