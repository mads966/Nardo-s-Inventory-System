package com.nardos.inventory;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class SearchFilterUI extends Application {

    private SearchFilterService service = new SearchFilterService();

    @Override
    public void start(Stage stage) {

        // --- UI CONTROLS ---
        TextField nameField = new TextField();
        nameField.setPromptText("Search by product name...");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
                "FOOD", "BEVERAGES", "SNACKS", "ESSENTIALS", "COMBO MEALS"
        );
        categoryBox.setPromptText("Category");

        TextField supplierField = new TextField();
        supplierField.setPromptText("Supplier ID");

        ComboBox<String> stockCompareBox = new ComboBox<>();
        stockCompareBox.getItems().addAll("BELOW", "EQUAL", "ABOVE");
        stockCompareBox.setPromptText("Stock Comparison");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        Button searchBtn = new Button("Search");

        // --- TABLE TO DISPLAY RESULTS ---
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        table.getColumns().addAll(idCol, nameCol, catCol, qtyCol);

        // --- SEARCH BUTTON ACTION ---
        searchBtn.setOnAction(e -> {

            String name = nameField.getText().trim();
            String category = categoryBox.getValue();
            Integer supplierID = null;
            String comparison = stockCompareBox.getValue();
            Integer qty = null;

            if (!supplierField.getText().isBlank()) {
                supplierID = Integer.parseInt(supplierField.getText());
            }
            if (!quantityField.getText().isBlank()) {
                qty = Integer.parseInt(quantityField.getText());
            }

            // Perform combined search
            List<Product> results = service.combineSearchAndFilter(
                    name.isEmpty() ? null : name,
                    category,
                    supplierID,
                    comparison,
                    qty
            );

            ObservableList<Product> data = FXCollections.observableArrayList(results);
            table.setItems(data);
        });

        // --- LAYOUT ---
        HBox filters = new HBox(10, nameField, categoryBox, supplierField, stockCompareBox, quantityField, searchBtn);
        filters.setPadding(new Insets(15));

        VBox root = new VBox(10, filters, table);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 900, 500);

        stage.setTitle("Nardo's Inventory Search & Filtering");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
