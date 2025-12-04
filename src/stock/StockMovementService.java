// File: StockMovementService.java
package stock;

import product.ProductDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StockMovementService {
    private StockMovementDAO stockMovementDAO;
    private ProductDAO productDAO;
    private Connection connection;

    public StockMovementService(Connection connection) {
        this.stockMovementDAO = new StockMovementDAO(connection);
        this.productDAO = new ProductDAO();
    }

    public StockMovement recordSaleMovement(int productId, int userId, int quantitySold,
                                            int saleId, String reason) throws SQLException {
        // Get current quantity
        product.Product product = productDAO.getProductById(productId);
        if (product == null) {
            throw new SQLException("Product not found with ID: " + productId);
        }

        int currentQuantity = product.getQuantity();
        int newQuantity = currentQuantity - quantitySold;

        if (newQuantity < 0) {
            throw new SQLException("Insufficient stock. Available: " + currentQuantity + ", Requested: " + quantitySold);
        }

        StockMovement movement = new StockMovement(
                productId,
                saleId,
                "SALE",
                -quantitySold,
                currentQuantity,
                reason
        );

        movement.setUserId(userId);

        // Update product quantity first
        productDAO.updateProductQuantity(productId, -quantitySold);

        // Then record the movement
        stockMovementDAO.createStockMovement(movement);

        return movement;
    }

    public StockMovement recordRestockMovement(int productId, int userId, int quantityAdded,
                                               int supplierId, String reason) throws SQLException {
        product.Product product = productDAO.getProductById(productId);
        if (product == null) {
            throw new SQLException("Product not found with ID: " + productId);
        }

        int currentQuantity = product.getQuantity();
        int newQuantity = currentQuantity + quantityAdded;

        StockMovement movement = new StockMovement(
                productId,
                supplierId,
                "RESTOCK",
                quantityAdded,
                currentQuantity,
                reason
        );

        movement.setUserId(userId);

        // Update product quantity
        productDAO.updateProductQuantity(productId, quantityAdded);

        // Record the movement
        stockMovementDAO.createStockMovement(movement);

        return movement;
    }

    public StockMovement recordAdjustmentMovement(int productId, int userId, int adjustment,
                                                  String reason) throws SQLException {
        product.Product product = productDAO.getProductById(productId);
        if (product == null) {
            throw new SQLException("Product not found with ID: " + productId);
        }

        int currentQuantity = product.getQuantity();
        int newQuantity = currentQuantity + adjustment;

        if (newQuantity < 0) {
            throw new SQLException("Adjustment would result in negative stock");
        }

        StockMovement movement = new StockMovement(
                productId,
                0,
                "ADJUSTMENT",
                adjustment,
                currentQuantity,
                reason
        );

        movement.setUserId(userId);

        // Update product quantity
        productDAO.updateProductQuantity(productId, adjustment);

        // Record the movement
        stockMovementDAO.createStockMovement(movement);

        return movement;
    }

    public List<StockMovement> getProductMovementHistory(int productId) throws SQLException {
        return stockMovementDAO.getMovementsByProduct(productId);
    }

    public List<StockMovement> getUserMovementHistory(int userId) throws SQLException {
        return stockMovementDAO.getMovementsByUser(userId);
    }

    public List<StockMovement> getAllMovementHistory() throws SQLException {
        return stockMovementDAO.getAllMovements();
    }

    public int getTotalMovementsCount() throws SQLException {
        return stockMovementDAO.getTotalMovementsCount();
    }

    public String generateMovementReport(java.time.LocalDate startDate, java.time.LocalDate endDate) throws SQLException {
        List<StockMovement> movements = stockMovementDAO.getMovementsByDateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        StringBuilder report = new StringBuilder();
        report.append("STOCK MOVEMENT REPORT\n");
        report.append("=====================\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        report.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n\n");

        if (movements.isEmpty()) {
            report.append("No stock movements in the specified period.\n");
            return report.toString();
        }

        int totalAdded = 0;
        int totalRemoved = 0;

        report.append(String.format("%-20s %-25s %-15s %-10s %-10s %-10s %-20s\n",
                "Timestamp", "Product ID", "Type", "Change", "From", "To", "Reason"));
        report.append("-".repeat(120)).append("\n");

        for (StockMovement movement : movements) {
            String productName = getProductName(movement.getProductId());
            report.append(String.format("%-20s %-25s %-15s %-10d %-10d %-10d %-20s\n",
                    movement.getTimestamp().toString().substring(0, 19),
                    truncate(productName, 23),
                    movement.getMovementType(),
                    movement.getQuantityChanged(),
                    movement.getPreviousQuantity(),
                    movement.getNewQuantity(),
                    truncate(movement.getReason(), 18)
            ));

            if (movement.getQuantityChanged() > 0) {
                totalAdded += movement.getQuantityChanged();
            } else {
                totalRemoved += Math.abs(movement.getQuantityChanged());
            }
        }

        report.append("\nSUMMARY:\n");
        report.append("--------\n");
        report.append("Total Movements: ").append(movements.size()).append("\n");
        report.append("Total Items Added: ").append(totalAdded).append("\n");
        report.append("Total Items Removed: ").append(totalRemoved).append("\n");
        report.append("Net Change: ").append(totalAdded - totalRemoved).append("\n");

        return report.toString();
    }

    private String getProductName(int productId) {
        try {
            product.Product product = productDAO.getProductById(productId);
            return product != null ? product.getName() : "Product ID: " + productId;
        } catch (SQLException e) {
            return "Product ID: " + productId;
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
}