package stock;

import java.time.LocalDateTime;

public class LowStockAlert {
    private int alertId;
    private int productId;
    private int currentQuantity;
    private int minStockLevel;
    private LocalDateTime alertDate;
    private boolean isResolved;

    public LowStockAlert() {}

    public LowStockAlert(int alertId, int productId, int currentQuantity, int minStockLevel, LocalDateTime alertDate, boolean isResolved) {
        this.alertId = alertId;
        this.productId = productId;
        this.currentQuantity = currentQuantity;
        this.minStockLevel = minStockLevel;
        this.alertDate = alertDate;
        this.isResolved = isResolved;
    }

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(int currentQuantity) { this.currentQuantity = currentQuantity; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public LocalDateTime getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDateTime alertDate) { this.alertDate = alertDate; }

    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { isResolved = resolved; }

    public void triggerAlert() {
        this.alertDate = LocalDateTime.now();
        this.isResolved = false;
    }

    public void resolveAlert() {
        this.isResolved = true;
    }
}
