package stock;

import java.time.LocalDateTime;

public class StockMovement {
    private int movementId;
    private int productId;
    private Integer relatedId;
    private String movementType;
    private int quantityChanged;
    private int previousQuantity;
    private int newQuantity;
    private String reason;
    private int userId;
    private LocalDateTime timestamp;
    
    // Constructors
    public StockMovement() {
        this.timestamp = LocalDateTime.now();
    }
    
    public StockMovement(int productId, Integer relatedId, String movementType, 
                        int quantityChanged, int previousQuantity, String reason) {
        this();
        this.productId = productId;
        this.relatedId = relatedId;
        this.movementType = movementType;
        this.quantityChanged = quantityChanged;
        this.previousQuantity = previousQuantity;
        this.newQuantity = previousQuantity + quantityChanged;
        this.reason = reason;
    }
    
    // Getters and Setters
    public int getMovementId() { return movementId; }
    public void setMovementId(int movementId) { this.movementId = movementId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public Integer getRelatedId() { return relatedId; }
    public void setRelatedId(Integer relatedId) { this.relatedId = relatedId; }
    
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    
    public int getQuantityChanged() { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged) { 
        this.quantityChanged = quantityChanged;
        this.newQuantity = this.previousQuantity + quantityChanged;
    }
    
    public int getPreviousQuantity() { return previousQuantity; }
    public void setPreviousQuantity(int previousQuantity) { 
        this.previousQuantity = previousQuantity;
        this.newQuantity = previousQuantity + this.quantityChanged;
    }
    
    public int getNewQuantity() { return newQuantity; }
    public void setNewQuantity(int newQuantity) { this.newQuantity = newQuantity; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}