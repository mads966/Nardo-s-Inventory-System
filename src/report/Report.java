package report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Report {
    private int reportId;
    private InventoryReportType reportType;
    private LocalDate generatedDate;
    private int generatedById;
    private String data;
    private String title;
    private String summary;


    public Report() {}

    // Use this method when the system automatically makes a report
    public Report(int reportId, int generatedById, InventoryReportType reportType, String data, String title, String summary) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.generatedDate = LocalDate.now();
        this.generatedById = generatedById;
        this.data = data;
        this.title = title;
        this.summary = summary;
    }

    // Getters and setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }
    
    public InventoryReportType getReportType() { return reportType; }
    public void setReportType(InventoryReportType reportType) { this.reportType = reportType; }
    
    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }
    
    public int getGeneratedById() { return generatedById; }
    public void setGeneratedById(int generatedById) { this.generatedById = generatedById; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return generatedDate.format(formatter);
    }

    public String getShortDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return generatedDate.format(formatter);
    }
}