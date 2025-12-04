package report;

public enum InventoryReportType {
    INVENTORY_SUMMARY("Inventory Summary"),
    TRANSACTION_HISTORY("Transaction History"),
    LOW_STOCK("Low Stock Report"),
    SALES_REPORT("Sales Report"),
    CUSTOMER_TRANSACTIONS("Customer Transactions"),
    SUPPLIER_REPORT("Supplier Report");

    private final String displayName;

    InventoryReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
