public class SaleItem {
    private int productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double lineTotal;
    
    public SaleItem(int productId, String productName, int quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }
    
    // Getters
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return lineTotal; }
}