package vnua.fita.tthieu.springboot.dto;

public class MonthlyRevenueDTO {
    private Integer month;
    private Integer year;
    private Double revenue;
    private Integer totalOrders;

    public MonthlyRevenueDTO() {}

    public MonthlyRevenueDTO(Integer month, Integer year, Double revenue, Integer totalOrders) {
        this.month = month;
        this.year = year;
        this.revenue = revenue;
        this.totalOrders = totalOrders;
    }

    // Getters & Setters
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
}