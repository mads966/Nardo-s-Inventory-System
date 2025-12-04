@echo off
echo Running Nardo's Inventory Search & Filter GUI...

javac --module-path "javafx/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.fxml -cp "lib/mysql-connector-j-9.5.0.jar;src" -d out src/com/nardos/inventory/*.java

java --module-path "javafx/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.fxml -cp "out;lib/mysql-connector-j-9.5.0.jar" com.nardos.inventory.SearchFilterUI

pause
