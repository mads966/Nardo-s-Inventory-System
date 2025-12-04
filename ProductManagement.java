package com.nardos.inventory;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProductManagement extends JFrame {

private DefaultTableModel tableModel;
private JTable table;

public static void main(String[] args) {
    try {
        new ProductManagement();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public ProductManagement() throws SQLException {
    tableModel = new DefaultTableModel();
    table = new JTable(tableModel);

    setTitle("Product Management");
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel panel1 = new JPanel(new BorderLayout());
    add(panel1);

    
    JPanel topPanel = new JPanel(new FlowLayout());
    topPanel.add(new JLabel("Search"));
    JTextField searchField = new JTextField(30);
    topPanel.add(searchField);
    
    
    add(topPanel, BorderLayout.NORTH);

    
    JPanel bottomPanel = new JPanel(new GridLayout(1, 4));

    JButton addButton = new JButton("Add");
    JButton deleteButton = new JButton("Delete");
    JButton updateButton = new JButton("Update");
    JButton searchButton = new JButton("Search");

    bottomPanel.add(addButton);
    bottomPanel.add(deleteButton);
    bottomPanel.add(updateButton);
    topPanel.add(searchButton);

    add(bottomPanel, BorderLayout.SOUTH);

    
    String[] columnNames = { "Product ID", "Name", "Category", "Supplier", "Price", "Quantity", "MinStock" };
    tableModel.setColumnIdentifiers(columnNames);

    JScrollPane sp = new JScrollPane(table);
    panel1.add(sp, BorderLayout.CENTER);
    tableModel.setRowCount(0);

    
    ProductDAO dao = new ProductDAO();
    List<Product> products = dao.searchByName("searchTerm");
    for (Product p : products) {
        tableModel.addRow(new Object[]{
            p.getProductId(),
            p.getName(),
            p.getCategory(),
            p.getSupplierId(),
            p.getPrice(),
            p.getQuantity(),
            p.getMinStock()
        });
        setVisible(true);
    }

    
    addButton.addActionListener(e -> {
        ProductForm form = new ProductForm(this);
        form.setVisible(true);
    });
    

    

   
    deleteButton.addActionListener(e -> {
        
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product");
            return;
        }

        Object value = table.getValueAt(selectedRow, 0);
        int productId = Integer.parseInt(value.toString());

        try {
            ProductDAO daoDelete = new ProductDAO();
            daoDelete.deleteProduct(productId);
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Product deleted");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    });

    
    updateButton.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update");
            return;
        }
        
        ProductForm form = new ProductForm(this);
        form.setVisible(true);
    });
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText();

                try {
                    ProductDAO daoSearch = new ProductDAO();
                    List<Product> result = daoSearch.searchByName(searchText);

                   tableModel.setRowCount(0); 
            for (Product p : result) {
                tableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    p.getCategory(),
                    p.getSupplierId(),
                    p.getPrice(),
                    p.getQuantity(),
                    p.getMinStock()
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(ProductManagement.this, "Unable to search");
        }
    }
});

        setVisible(true);
    }

    


public void addProductToTable(Product p) {
    tableModel.addRow(new Object[]{
        p.getProductId(),
        p.getName(),
        p.getCategory(),
        p.getSupplierId(),
        p.getPrice(),
        p.getQuantity(),
        p.getMinStock()
    });
}


}

























