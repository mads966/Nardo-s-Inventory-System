package sale;

public class SalesStatistics {
    private int totalSales;
    private double totalRevenue;
    private double averageSale;
    private int totalItems;
    
    // Getters and setters
    public int getTotalSales() { return totalSales; }
    public void setTotalSales(int totalSales) { this.totalSales = totalSales; }
    
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public double getAverageSale() { return averageSale; }
    public void setAverageSale(double averageSale) { this.averageSale = averageSale; }
    
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
}