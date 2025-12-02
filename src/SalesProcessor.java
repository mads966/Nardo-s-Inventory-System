import java.util.ArrayList;
import java.util.List;

public final class SalesProcessor {
    private ProductDAO productDAO;
    private SaleDAO saleDAO;
    private AlertManager alertManager;
    
    // SRS 1.3: Automatically adjust inventory when sale is recorded
    public SalesProcessor() {
        this.productDAO = new ProductDAO();
        this.saleDAO = new SaleDAO();
        this.alertManager = new AlertManager();
    }
    
    public void processSale(List<SaleItem> items, int userId) {
        // SRS 1.3: Real-time inventory deduction
        // SRS 3.1: Processed and confirmed under 3 seconds
        
        // Create new sale
        Sale sale = new Sale();
        sale.setUserId(userId);
        
        for (SaleItem item : items) {
            sale.addItem(item);
            
            // SRS 1.3: Reduce stock immediately
            productDAO.updateProductQuantity(item.getProductId(), -item.getQuantity());
            
            // SRS 1.3: Trigger low stock alert if needed
            alertManager.checkStockLevel(item.getProductId());
        }
        
        // Save sale to database
        int saleId = saleDAO.saveSale(sale);
        sale.setSaleId(saleId);
        
        // Log transaction (SRS 1.5)
        saleDAO.logTransaction(sale, userId, "SALE_PROCESSED");
        
        // Generate receipt
        generateReceipt(saleId);
    }
    
    public double calculateTotal(List<SaleItem> items) {
        double total = 0;
        for (SaleItem item : items) {
            total += item.getLineTotal();
        }
        return total;
    }
    
    public void updateInventory(int productId, int quantityChange) {
        // SRS 1.3: Link sales records to inventory records
        productDAO.updateProductQuantity(productId, quantityChange);
        
        // Check if low stock after update
        alertManager.checkStockLevel(productId);
    }
    
    public void generateReceipt(int saleId) {
        // SRS 1.3: Eliminate manual adjustments
        Sale sale = saleDAO.getSaleById(saleId);
        if (sale != null) {
            System.out.println("=== NARDO'S ONE STOP SHOP ===");
            System.out.println("Receipt #: " + sale.getSaleId());
            System.out.println("Date: " + sale.getSaleDate());
            System.out.println("-------------------------");
            for (SaleItem item : sale.getItems()) {
                System.out.printf("%s x%d @ $%.2f = $%.2f%n", 
                    item.getProductName(), item.getQuantity(), 
                    item.getUnitPrice(), item.getLineTotal());
            }
            System.out.println("-------------------------");
            System.out.printf("TOTAL: $%.2f%n", sale.getTotalAmount());
            System.out.println("Thank you for shopping with us!");
        }
    }
}