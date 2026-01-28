package vnua.fita.tthieu.springboot.dto;

public class TopBookDTO {
    private Long bookId;
    private String tenSach;
    private Integer totalQuantity;
    private Double totalRevenue;

    public TopBookDTO() {}

    public TopBookDTO(Long bookId, String tenSach, Integer totalQuantity, Double totalRevenue) {
        this.bookId = bookId;
        this.tenSach = tenSach;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

    // Getters & Setters
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
}