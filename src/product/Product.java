package product;

public class Product {
    private int productId;
    private String name;
    private String category;
    private Integer supplierId;
    private double price;
    private int quantity;
    private int minStock;
    private boolean active = true;

    public int getProductId() { return productId; }
    public void setProductId(int id) { this.productId = id; }

    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer id) { this.supplierId = id; }

    public double getPrice() { return price; }
    public void setPrice(double p) { this.price = p; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }

    public int getMinStock() { return minStock; }
    public void setMinStock(int m) { this.minStock = m; }

    public double getLineTotal() {
        return quantity * price;
    }

    public Product(int productId, String productName, int quantity, double unitPrice) {
        this.productId = productId;
        this.name = productName;
        this.quantity = quantity;
        this.price = unitPrice;
    }

    public Product(int productId, String productName, int quantity, double unitPrice, int minStock) {
        this.productId = productId;
        this.name = productName;
        this.quantity = quantity;
        this.price = unitPrice;
        this.minStock = minStock;
    }

    public Product() {}
    @Override
    public String toString() {
        return productId + " | " + name + " | " + category + " | Qty: " + quantity;
    }

    // Compatibility methods used across the codebase
    public int getCurrentQuantity() {
        return quantity;
    }

    public int getMinStockLevel() {
        return minStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
