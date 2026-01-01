package vnua.fita.tthieu.springboot.dto;
// Chứa nhiều sách trong 1 order từ req user
public class OrderItemRequest {
    private Long bookId;
    private Integer soLuong;

    public OrderItemRequest() {}
    public OrderItemRequest(Long bookId, Integer soLuong) {
        this.bookId = bookId;
        this.soLuong = soLuong;
    }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
}
