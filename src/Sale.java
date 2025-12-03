import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int saleId;
    private int userId;
    private List<SaleItem> items;
    private double totalAmount;
    private LocalDate saleDate;
    
    // SRS 1.3: Automatically reduce stock when sale is made
    public Sale() {
        this.items = new ArrayList<>();
        this.saleDate = LocalDate.now();
    }
    
    public Sale(int saleId, int userId, LocalDate saleDate) {
        this.saleId = saleId;
        this.userId = userId;
        this.saleDate = saleDate;
        this.items = new ArrayList<>();
    }
    
    // SRS 1.3: Process sales and deduct inventory
    public void processSale() {
        // In actual implementation, this would:
        // 1. Save sale to database
        // 2. Update inventory quantities
        // 3. Check for low stock alerts
        // 4. Log transaction
        
        System.out.println("Processing sale #" + saleId);
        calculateTotal();
        // Additional processing logic here
    }
    
    // Calculate total amount
    public void calculateTotal() {
        totalAmount = 0;
        for (SaleItem item : items) {
            totalAmount += item.getLineTotal();
        }
    }
    
    public void addItem(SaleItem item) {
        items.add(item);
        calculateTotal();
    }
    
    public void removeItem(int productId) {
        items.removeIf(item -> item.getProductId() == productId);
        calculateTotal();
    }
    
    // Getters and setters
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { 
        this.items = items; 
        calculateTotal();
    }
    
    public double getTotalAmount() { return totalAmount; }
    
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
}