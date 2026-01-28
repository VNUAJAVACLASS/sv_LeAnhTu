package vnua.fita.tthieu.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vnua.fita.tthieu.springboot.dto.MonthlyRevenueDTO;
import vnua.fita.tthieu.springboot.dto.TopBookDTO;
import vnua.fita.tthieu.springboot.entity.OrderHistory;
import vnua.fita.tthieu.springboot.repository.OrderHistoryRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RevenueService {

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    /**
     * Doanh thu theo tháng trong năm
     */
    public List<MonthlyRevenueDTO> getMonthlyRevenue(int year) {
        List<OrderHistory> orders = orderHistoryRepository.findAll();

        // Khởi tạo 12 tháng với doanh thu = 0
        List<MonthlyRevenueDTO> monthlyRevenue = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            monthlyRevenue.add(new MonthlyRevenueDTO(month, year, 0.0, 0));
        }

        // Tính doanh thu theo tháng
        for (OrderHistory order : orders) {
            LocalDate ngayNhan = order.getNgayNhan();
            if (ngayNhan != null && ngayNhan.getYear() == year) {
                int month = ngayNhan.getMonthValue();
                MonthlyRevenueDTO dto = monthlyRevenue.get(month - 1);
                
                double revenue = order.getSoLuong() * order.getPriceAtOrder();
                dto.setRevenue(dto.getRevenue() + revenue);
                dto.setTotalOrders(dto.getTotalOrders() + 1);
            }
        }

        return monthlyRevenue;
    }

    /**
     * Doanh thu theo năm
     */
    public Map<Integer, Double> getYearlyRevenue(int startYear, int endYear) {
        List<OrderHistory> orders = orderHistoryRepository.findAll();
        
        Map<Integer, Double> yearlyRevenue = new LinkedHashMap<>();
        for (int year = startYear; year <= endYear; year++) {
            yearlyRevenue.put(year, 0.0);
        }

        for (OrderHistory order : orders) {
            LocalDate ngayNhan = order.getNgayNhan();
            if (ngayNhan != null) {
                int year = ngayNhan.getYear();
                if (year >= startYear && year <= endYear) {
                    double revenue = order.getSoLuong() * order.getPriceAtOrder();
                    yearlyRevenue.put(year, yearlyRevenue.get(year) + revenue);
                }
            }
        }

        return yearlyRevenue;
    }

    /**
     * Top sách bán chạy
     */
    public List<TopBookDTO> getTopBooks(Integer month, int year, int limit) {
        List<OrderHistory> orders = orderHistoryRepository.findAll();

        // Lọc theo tháng/năm
        List<OrderHistory> filteredOrders = orders.stream()
            .filter(order -> {
                LocalDate ngayNhan = order.getNgayNhan();
                if (ngayNhan == null) return false;
                if (ngayNhan.getYear() != year) return false;
                if (month != null && ngayNhan.getMonthValue() != month) return false;
                return true;
            })
            .collect(Collectors.toList());

        // Nhóm theo bookId và tính tổng
        Map<Long, TopBookDTO> bookMap = new HashMap<>();
        
        for (OrderHistory order : filteredOrders) {
            Long bookId = order.getBookId();
            TopBookDTO dto = bookMap.getOrDefault(bookId, 
                new TopBookDTO(bookId, order.getTenSach(), 0, 0.0));
            
            dto.setTotalQuantity(dto.getTotalQuantity() + order.getSoLuong());
            dto.setTotalRevenue(dto.getTotalRevenue() + 
                (order.getSoLuong() * order.getPriceAtOrder()));
            
            bookMap.put(bookId, dto);
        }

        // Sắp xếp và lấy top
        return bookMap.values().stream()
            .sorted((a, b) -> Integer.compare(b.getTotalQuantity(), a.getTotalQuantity()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Thống kê tổng quan
     */
    public Map<String, Object> getSummary() {
        List<OrderHistory> orders = orderHistoryRepository.findAll();
        
        double totalRevenue = orders.stream()
            .mapToDouble(o -> o.getSoLuong() * o.getPriceAtOrder())
            .sum();
        
        int totalOrders = orders.size();
        
        int totalBooks = orders.stream()
            .mapToInt(OrderHistory::getSoLuong)
            .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalOrders", totalOrders);
        summary.put("totalBooks", totalBooks);
        
        return summary;
    }
}