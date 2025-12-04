package sale;

public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int productId;
    private String productName;
    private String productCategory;
    private int quantity;
    private double unitPrice;
    private double lineTotal;
    
    // Constructors
    public SaleItem() {}
    
    public SaleItem(int productId, String productName, String productCategory, 
                   int quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }
    
    // Factory method
    public static SaleItem create(int productId, String productName, String category, 
                                 int quantity, double price) {
        return new SaleItem(productId, productName, category, quantity, price);
    }
    
    // Getters and Setters
    public int getSaleItemId() { return saleItemId; }
    public void setSaleItemId(int saleItemId) { this.saleItemId = saleItemId; }
    
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        calculateLineTotal();
    }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }
    
    public double getLineTotal() { return lineTotal; }
    void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
    
    // Business methods
    private void calculateLineTotal() {
        this.lineTotal = this.quantity * this.unitPrice;
    }
    
    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
        calculateLineTotal();
    }
    
    public void increaseQuantity(int amount) {
        this.quantity += amount;
        calculateLineTotal();
    }
    
    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            calculateLineTotal();
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s x%d @ $%.2f = $%.2f", 
            productName, quantity, unitPrice, lineTotal);
    }
    
    // For table display
    public Object[] toTableRow() {
        return new Object[] {
            productId,
            productName,
            productCategory,
            quantity,
            String.format("$%.2f", unitPrice),
            String.format("$%.2f", lineTotal)
        };
    }
}