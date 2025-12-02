import javax.swing.*;
import java.awt.*;

public class SalesPanel extends JPanel {
    // SRS 3.3.1: Main dashboard for quick sales processing
    private SalesProcessingForm salesForm;
    private JLabel quickStats;
    
    public SalesPanel() {
        setLayout(new BorderLayout());
        
        // Quick stats at top
        quickStats = new JLabel("Today's Sales: $0.00 | Transactions: 0", SwingConstants.CENTER);
        quickStats.setFont(new Font("Arial", Font.BOLD, 14));
        add(quickStats, BorderLayout.NORTH);
        
        // Sales processing form in center
        salesForm = new SalesProcessingForm();
        add(salesForm, BorderLayout.CENTER);
        
        // Quick actions at bottom
        JPanel quickActions = new JPanel(new FlowLayout());
        JButton quickSaleButton = new JButton("Quick Sale (3 Clicks)");
        JButton viewSalesReportButton = new JButton("View Sales Report");
        
        quickActions.add(quickSaleButton);
        quickActions.add(viewSalesReportButton);
        add(quickActions, BorderLayout.SOUTH);
        
        // SRS 3.3.1: 3-click sales from dashboard
        quickSaleButton.addActionListener(e -> {
            // Simulate quick sale
            JOptionPane.showMessageDialog(this, 
                "Quick sale mode activated!\n1. Select product\n2. Enter quantity\n3. Confirm sale",
                "Quick Sale", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    public void updateQuickStats(double todayTotal, int transactionCount) {
        quickStats.setText(String.format("Today's Sales: $%.2f | Transactions: %d", 
                                       todayTotal, transactionCount));
    }
}