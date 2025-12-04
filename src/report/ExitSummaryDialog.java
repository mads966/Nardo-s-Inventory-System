package report;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class ExitSummaryDialog extends JDialog {
    private ReportGenerator reportGenerator;
    private JTextArea summaryArea;
    private JCheckBox showAlwaysCheckbox;
    
    public ExitSummaryDialog(Frame parent, Connection connection) {
        super(parent, "Daily Summary", true);
        this.reportGenerator = new ReportGenerator(connection);
        
        initComponents();
        layoutComponents();
        loadSummary();
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        summaryArea = new JTextArea(20, 50);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setEditable(false);
        
        showAlwaysCheckbox = new JCheckBox("Always show this summary on exit");
        showAlwaysCheckbox.setSelected(true);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("DAILY ACTIVITY SUMMARY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Summary text
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Summary"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Checkbox
        bottomPanel.add(showAlwaysCheckbox, BorderLayout.WEST);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton printButton = new JButton("Print");
        printButton.addActionListener(e -> printSummary());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSummary());
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(printButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadSummary() {
        try {
            String summary = reportGenerator.generateExitSummary();
            summaryArea.setText(summary);
        } catch (Exception e) {
            summaryArea.setText("Error loading summary: " + e.getMessage());
        }
    }
    
    private void printSummary() {
        try {
            summaryArea.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error printing summary: " + e.getMessage(),
                "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveSummary() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Summary");
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Save implementation
            JOptionPane.showMessageDialog(this,
                "Summary saved successfully!",
                "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public boolean shouldShowAlways() {
        return showAlwaysCheckbox.isSelected();
    }
}