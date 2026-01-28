package vnua.fita.tthieu.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vnua.fita.tthieu.springboot.dto.MonthlyRevenueDTO;
import vnua.fita.tthieu.springboot.dto.TopBookDTO;
import vnua.fita.tthieu.springboot.service.RevenueService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/revenue")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RevenueController {

    @Autowired
    private RevenueService revenueService;

    /**
     * Lấy doanh thu theo tháng trong năm
     * GET /api/revenue/monthly?year=2025
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getMonthlyRevenue(@RequestParam int year) {
        try {
            List<MonthlyRevenueDTO> revenue = revenueService.getMonthlyRevenue(year);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy doanh thu theo năm
     * GET /api/revenue/yearly?startYear=2020&endYear=2025
     */
    @GetMapping("/yearly")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getYearlyRevenue(
            @RequestParam int startYear,
            @RequestParam int endYear) {
        try {
            Map<Integer, Double> revenue = revenueService.getYearlyRevenue(startYear, endYear);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Top sách bán chạy theo tháng
     * GET /api/revenue/top-books?month=12&year=2025&limit=10
     */
    @GetMapping("/top-books")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getTopBooks(
            @RequestParam(required = false) Integer month,
            @RequestParam int year,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<TopBookDTO> topBooks = revenueService.getTopBooks(month, year, limit);
            return ResponseEntity.ok(topBooks);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Thống kê tổng quan
     * GET /api/revenue/summary
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getSummary() {
        try {
            Map<String, Object> summary = revenueService.getSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
}