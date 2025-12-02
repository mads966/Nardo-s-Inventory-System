public class AlertManager {
    // SRS 1.2: Notify staff when product quantity is low
    // SRS 3.1: Alerts triggered within 10 minutes
    
    public void checkStockLevel(int productId) {
        // Check if product is below minimum stock level
        ProductDAO productDAO = new ProductDAO();
        Product product = productDAO.getProductById(productId);
        
        if (product != null && product.getCurrentQuantity() < product.getMinStockLevel()) {
            triggerLowStockAlert(product);
        }
    }
    
    private void triggerLowStockAlert(Product product) {
        System.out.println("LOW STOCK ALERT: " + product.getName() + 
                          " has only " + product.getCurrentQuantity() + 
                          " left (minimum: " + product.getMinStockLevel() + ")");
        // In actual implementation, this would:
        // 1. Send notification to staff
        // 2. Log the alert
        // 3. Potentially notify suppliers
    }
}