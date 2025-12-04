package stock;

import database.DBManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockMovementDAO {
    private Connection connection;
    
    public StockMovementDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean saveStockMovement(StockMovement movement) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, related_id, movement_type, " +
                "quantity_changed, previous_quantity, new_quantity, reason, user_id, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DBManager.getConnection();
             PreparedStatement pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, movement.getProductId());
            if (movement.getRelatedId() != null) {
                pstmt.setInt(2, movement.getRelatedId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, movement.getMovementType());
            pstmt.setInt(4, movement.getQuantityChanged());
            pstmt.setInt(5, movement.getPreviousQuantity());
            pstmt.setInt(6, movement.getNewQuantity());
            pstmt.setString(7, movement.getReason());
            pstmt.setInt(8, movement.getUserId());
            pstmt.setTimestamp(9, Timestamp.valueOf(movement.getTimestamp()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        movement.setMovementId(rs.getInt(1));
                        return true;
                    }
                }
            }
            throw new SQLException("Failed to save stock movement, no ID obtained");
        }
    }

    public List<StockMovement> getMovementsByProduct(int productId) throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT * FROM stock_movements WHERE product_id = ? ORDER BY timestamp DESC";
        try (Connection c = DBManager.getConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movements.add(mapResultSetToMovement(rs));
                }
            }
        }
        return movements;
    }

    public List<StockMovement> getMovementsByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT * FROM stock_movements WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        try (Connection c = DBManager.getConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movements.add(mapResultSetToMovement(rs));
                }
            }
        }
        return movements;
    }

    public List<StockMovement> getMovementsByUser(int userId) throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT * FROM stock_movements WHERE user_id = ? ORDER BY timestamp DESC";
        try (Connection c = DBManager.getConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movements.add(mapResultSetToMovement(rs));
                }
            }
        }
        return movements;
    }

    public List<StockMovement> getAllMovements() throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String sql = "SELECT * FROM stock_movements ORDER BY timestamp DESC";
        try (Connection c = DBManager.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                movements.add(mapResultSetToMovement(rs));
            }
        }
        return movements;
    }

    public int getTotalMovementsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock_movements";
        try (Connection c = DBManager.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private StockMovement mapResultSetToMovement(ResultSet rs) throws SQLException {
        StockMovement movement = new StockMovement();
        movement.setMovementId(rs.getInt("movement_id"));
        movement.setProductId(rs.getInt("product_id"));
        movement.setRelatedId(rs.getObject("related_id") != null ? rs.getInt("related_id") : null);
        movement.setMovementType(rs.getString("movement_type"));
        movement.setQuantityChanged(rs.getInt("quantity_changed"));
        movement.setPreviousQuantity(rs.getInt("previous_quantity"));
        movement.setNewQuantity(rs.getInt("new_quantity"));
        movement.setReason(rs.getString("reason"));
        movement.setUserId(rs.getInt("user_id"));
        movement.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        return movement;
    }

    public boolean createStockMovement(StockMovement movement) throws SQLException {
        return saveStockMovement(movement);
    }
}