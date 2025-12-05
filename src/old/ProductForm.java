package com.nardos.inventory;

import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ProductForm extends JFrame {

    private JTextField field1;   
    private JComboBox<String> box;       
    private JComboBox<String> box1;      
    private JTextField field2;   
    private JTextField field3;  
    private JTextField field4;
    private ProductManagement productManagement;

    public static void main(String[] args) throws SQLException{
       
        new ProductForm(new ProductManagement());
    }

    
    public ProductForm(ProductManagement pm) {
        this.productManagement = pm;

        setTitle("Product Form");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label1 = new JLabel("Product Name");
        field1 = new JTextField();

        JLabel label2 = new JLabel("Category");
        box = new JComboBox<>();
        box.addItem("FOOD");
        box.addItem("BEVERAGES");
        box.addItem("SNACKS");
        box.addItem("ESSENTIALS");
        box.addItem("COMBO_MEALS");

        JLabel label3 = new JLabel("Supplier");
        box1 = new JComboBox<>();
        box1.addItem("1");
        box1.addItem("2");
        box1.addItem("3");
        box1.addItem("4");

        JLabel label4 = new JLabel("Price");
        field2 = new JTextField();

        JLabel label5 = new JLabel("Quantity");
        field3 = new JTextField();

        JLabel label6 = new JLabel("Min Stock");
        field4 = new JTextField();

        JButton addButton = new JButton("Save");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        panel.add(label1);
        panel.add(field1);

        panel.add(label2);
        panel.add(box);

        panel.add(label3);
        panel.add(box1);

        panel.add(label4);
        panel.add(field2);

        panel.add(label5);
        panel.add(field3);

        panel.add(label6);
        panel.add(field4);

        panel.add(addButton);

        add(panel);

        addButton.addActionListener(e -> {
            try {
                String name = field1.getText().trim();
                String category = box.getSelectedItem().toString();
                String supplierText = box1.getSelectedItem().toString();
                String priceText = field2.getText().trim();
                String quantityText = field3.getText();
                String minStockText = field4.getText();
                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(quantityText);
                int minStock = Integer.parseInt(minStockText);
                int supplier_id = Integer.parseInt(supplierText);

                if (name.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() || minStockText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Enter Information in all Fields!");
                    return;
                
                }
                ProductDAO dao = new ProductDAO();

                
                
                if (dao.isDuplicateInList(name, supplier_id)) {
            JOptionPane.showMessageDialog(this, "Product Already Exists!");
            return;
        }
                
      
                Product p = new Product();
                p.setName(name);
                p.setCategory(category);
                p.setSupplierId(supplier_id);
                p.setPrice(price);
                p.setQuantity(quantity);
                p.setMinStock(minStock);

                
                dao.saveProduct(p);

              
                productManagement.addProductToTable(p);

                JOptionPane.showMessageDialog(this, "Product Saved Successfully!");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Unable to save product!");
            }
        });

        setVisible(true);
    }
}