
package alert;

import stock.LowStockAlert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import database.DBManager;

public class AlertDAO {
    public AlertDAO() {}

    public boolean createAlert(LowStockAlert alert) throws SQLException {
        String query = "INSERT INTO low_stock_alerts (product_id, current_quantity, min_stock_level, alert_date, is_resolved) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBManager.getConnection();
             PreparedStatement stmt = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, alert.getProductId());
            stmt.setInt(2, alert.getCurrentQuantity());
            stmt.setInt(3, alert.getMinStockLevel());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(5, false);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        alert.setAlertId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean resolveAlert(int alertId) throws SQLException {
        String query = "UPDATE low_stock_alerts SET is_resolved = TRUE, resolved_at = ? WHERE alert_id = ?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, alertId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<LowStockAlert> getActiveAlerts() throws SQLException {
        List<LowStockAlert> alerts = new ArrayList<>();
        String query = "SELECT * FROM low_stock_alerts WHERE is_resolved = FALSE ORDER BY alert_date DESC";
        try (Connection c = DBManager.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                alerts.add(mapResultSetToAlert(rs));
            }
        }
        return alerts;
    }

    public List<LowStockAlert> getAlertsByProduct(int productId) throws SQLException {
        List<LowStockAlert> alerts = new ArrayList<>();
        String query = "SELECT * FROM low_stock_alerts WHERE product_id = ? ORDER BY alert_date DESC";
        try (Connection c = DBManager.getConnection();
             PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }
        }
        return alerts;
    }

    public boolean hasUnresolvedAlert(int productId) throws SQLException {
        String query = "SELECT COUNT(*) FROM low_stock_alerts WHERE product_id = ? AND is_resolved = FALSE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int getActiveAlertCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM low_stock_alerts WHERE is_resolved = FALSE";
        try (Connection c = DBManager.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void deleteResolvedAlerts() throws SQLException {
        String query = "DELETE FROM low_stock_alerts WHERE is_resolved = TRUE";
        try (Connection c = DBManager.getConnection();
             Statement stmt = c.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    private LowStockAlert mapResultSetToAlert(ResultSet rs) throws SQLException {
        LowStockAlert alert = new LowStockAlert(
                rs.getInt("alert_id"),
                rs.getInt("product_id"),
                rs.getInt("current_quantity"),
                rs.getInt("min_stock_level"),
                rs.getTimestamp("alert_date").toLocalDateTime(),
                rs.getBoolean("is_resolved")
        );
        return alert;
    }
}