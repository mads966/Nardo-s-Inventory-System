package sale;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int saleId;
    private LocalDateTime saleDateTime;
    private int userId;
    private String userName;
    private List<SaleItem> items;
    private double subtotal;
    private double taxAmount;
    private double discountAmount;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private String receiptNumber;
    private boolean isCompleted;
    
    // Constants
    public static final double TAX_RATE = 0.10; // 10% tax
    public static final String[] PAYMENT_METHODS = {"CASH", "CARD", "MOBILE", "OTHER"};
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    // Constructors
    public Sale() {
        this.items = new ArrayList<>();
        this.saleDateTime = LocalDateTime.now();
        this.paymentMethod = "CASH";
        this.paymentStatus = STATUS_COMPLETED;
        this.isCompleted = false;
        this.taxAmount = 0;
        this.discountAmount = 0;
    }
    
    public Sale(int userId, String userName) {
        this();
        this.userId = userId;
        this.userName = userName;
        generateReceiptNumber();
    }
    
    // Factory method
    public static Sale createNewSale(int userId, String userName) {
        return new Sale(userId, userName);
    }
    
    // Getters and Setters
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    
    public LocalDateTime getSaleDateTime() { return saleDateTime; }
    public void setSaleDateTime(LocalDateTime saleDateTime) { this.saleDateTime = saleDateTime; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { 
        this.items = items;
        calculateTotals();
    }
    
    public double getSubtotal() { return subtotal; }
    
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { 
        this.taxAmount = taxAmount;
        calculateTotal();
    }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { 
        this.discountAmount = discountAmount;
        calculateTotal();
    }
    
    public double getTotalAmount() { return totalAmount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    // Business methods
    public void addItem(SaleItem item) {
        // Check if item already exists
        for (SaleItem existing : items) {
            if (existing.getProductId() == item.getProductId()) {
                existing.increaseQuantity(item.getQuantity());
                calculateTotals();
                return;
            }
        }
        
        item.setSaleId(this.saleId);
        items.add(item);
        calculateTotals();
    }
    
    public void removeItem(int productId) {
        items.removeIf(item -> item.getProductId() == productId);
        calculateTotals();
    }
    
    public void updateItemQuantity(int productId, int newQuantity) {
        for (SaleItem item : items) {
            if (item.getProductId() == productId) {
                if (newQuantity <= 0) {
                    removeItem(productId);
                } else {
                    item.setQuantity(newQuantity);
                }
                calculateTotals();
                return;
            }
        }
    }
    
    public void clearItems() {
        items.clear();
        calculateTotals();
    }
    
    public int getTotalItems() {
        return items.stream().mapToInt(SaleItem::getQuantity).sum();
    }
    
    private void calculateTotals() {
        // Calculate subtotal
        subtotal = items.stream()
                       .mapToDouble(SaleItem::getLineTotal)
                       .sum();
        
        // Calculate tax
        taxAmount = subtotal * TAX_RATE;
        
        // Calculate total
        calculateTotal();
    }
    
    private void calculateTotal() {
        totalAmount = subtotal + taxAmount - discountAmount;
        if (totalAmount < 0) totalAmount = 0;
    }
    
    public void applyDiscount(double discountPercent) {
        if (discountPercent >= 0 && discountPercent <= 100) {
            discountAmount = subtotal * (discountPercent / 100);
            calculateTotal();
        }
    }
    
    public void applyFixedDiscount(double amount) {
        if (amount >= 0 && amount <= subtotal) {
            discountAmount = amount;
            calculateTotal();
        }
    }
    
    private void generateReceiptNumber() {
        // Format: NAR-YYYYMMDD-XXXXX
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%05d", (int)(Math.random() * 100000));
        this.receiptNumber = "NAR-" + date + "-" + random;
    }
    
    // SRS 1.3: Check if sale will trigger low stock
    public boolean hasLowStockItems(int minStockLevel) {
        // This would check against actual inventory
        // For now, we'll just return false
        return false;
    }
    
    // Print receipt
    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=".repeat(40)).append("\n");
        receipt.append("        NARDO'S ONE STOP SHOP\n");
        receipt.append("       University of the West Indies\n");
        receipt.append("            Mona Campus\n");
        receipt.append("=".repeat(40)).append("\n");
        receipt.append("Receipt #: ").append(receiptNumber).append("\n");
        receipt.append("Date: ").append(saleDateTime.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        receipt.append("Cashier: ").append(userName).append("\n");
        receipt.append("-".repeat(40)).append("\n");
        receipt.append(String.format("%-20s %4s %8s %8s\n", 
            "Item", "Qty", "Price", "Total"));
        receipt.append("-".repeat(40)).append("\n");
        
        for (SaleItem item : items) {
            receipt.append(String.format("%-20s %4d %8.2f %8.2f\n",
                truncate(item.getProductName(), 20),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()));
        }
        
        receipt.append("-".repeat(40)).append("\n");
        receipt.append(String.format("%-32s $%8.2f\n", "Subtotal:", subtotal));
        receipt.append(String.format("%-32s $%8.2f\n", "Tax (10%):", taxAmount));
        if (discountAmount > 0) {
            receipt.append(String.format("%-32s -$%7.2f\n", "Discount:", discountAmount));
        }
        receipt.append(String.format("%-32s $%8.2f\n", "TOTAL:", totalAmount));
        receipt.append("=".repeat(40)).append("\n");
        receipt.append("Payment Method: ").append(paymentMethod).append("\n");
        receipt.append("Status: ").append(paymentStatus).append("\n");
        if (notes != null && !notes.isEmpty()) {
            receipt.append("Notes: ").append(notes).append("\n");
        }
        receipt.append("\nThank you for shopping with us!\n");
        receipt.append("=".repeat(40)).append("\n");
        
        return receipt.toString();
    }
    
    private String truncate(String str, int length) {
        if (str == null || str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
    
    @Override
    public String toString() {
        return String.format("Sale #%d - %s - $%.2f", 
            saleId, receiptNumber, totalAmount);
    }
}