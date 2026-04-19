package com.rainbowforest.payment_service.controller;

import com.rainbowforest.payment_service.dto.PaymentDto.ApiResponse;
import com.rainbowforest.payment_service.model.Payment;
import com.rainbowforest.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Endpoints xem doanh thu:
 *
 * GET /api/revenue/summary → Tổng doanh thu toàn thời gian
 * GET /api/revenue/today → Doanh thu hôm nay
 * GET /api/revenue/by-day?date=2024-01-15 → Theo ngày cụ thể
 * GET /api/revenue/by-month?year=2024&month=1 → Theo tháng
 * GET /api/revenue/by-year?year=2024 → Theo năm
 * GET /api/revenue/range?from=...&to=... → Theo khoảng thời gian
 * GET /api/revenue/chart/daily?year=2024&month=1 → Dữ liệu biểu đồ theo ngày
 * trong tháng
 * GET /api/revenue/chart/monthly?year=2024 → Dữ liệu biểu đồ theo tháng trong
 * năm
 */
@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RevenueController {

    private final PaymentRepository paymentRepository;

    // ── Tổng doanh thu toàn thời gian ────────────────────────────

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        List<Payment> successPayments = getSuccessPayments();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalRevenue", sumAmount(successPayments));
        data.put("totalTransactions", successPayments.size());
        data.put("averageOrderValue", average(successPayments));

        // Thống kê theo phương thức thanh toán
        Map<String, BigDecimal> byMethod = successPayments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMethod().name(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));
        data.put("revenueByMethod", byMethod);

        return ResponseEntity.ok(ApiResponse.success("Tổng doanh thu", data));
    }

    // ── Doanh thu hôm nay ────────────────────────────────────────

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getToday() {
        LocalDate today = LocalDate.now();
        return getByDay(today);
    }

    // ── Doanh thu theo ngày cụ thể ───────────────────────────────

    @GetMapping("/by-day")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByDayParam(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return getByDay(date);
    }

    // ── Doanh thu theo tháng ─────────────────────────────────────

    @GetMapping("/by-month")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByMonth(
            @RequestParam int year,
            @RequestParam int month) {

        LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime to = from.plusMonths(1).minusSeconds(1);

        List<Payment> payments = getSuccessPaymentsBetween(from, to);

        Map<String, Object> data = buildRevenueData(payments);
        data.put("period", year + "-" + String.format("%02d", month));

        return ResponseEntity.ok(ApiResponse.success("Doanh thu tháng " + month + "/" + year, data));
    }

    // ── Doanh thu theo năm ───────────────────────────────────────

    @GetMapping("/by-year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByYear(@RequestParam int year) {
        LocalDateTime from = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime to = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        List<Payment> payments = getSuccessPaymentsBetween(from, to);

        Map<String, Object> data = buildRevenueData(payments);
        data.put("period", String.valueOf(year));

        return ResponseEntity.ok(ApiResponse.success("Doanh thu năm " + year, data));
    }

    // ── Doanh thu theo khoảng thời gian ──────────────────────────

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<Payment> payments = getSuccessPaymentsBetween(from, to);

        Map<String, Object> data = buildRevenueData(payments);
        data.put("from", from.toString());
        data.put("to", to.toString());

        return ResponseEntity.ok(ApiResponse.success("Doanh thu theo khoảng thời gian", data));
    }

    // ── Biểu đồ theo ngày trong tháng ────────────────────────────

    @GetMapping("/chart/daily")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChartDaily(
            @RequestParam int year,
            @RequestParam int month) {

        LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime to = from.plusMonths(1).minusSeconds(1);

        List<Payment> payments = getSuccessPaymentsBetween(from, to);

        // Group theo ngày
        Map<Integer, BigDecimal> byDay = new TreeMap<>();
        for (Payment p : payments) {
            int day = p.getCreatedAt().getDayOfMonth();
            byDay.merge(day, p.getAmount(), BigDecimal::add);
        }

        // Điền đủ tất cả các ngày trong tháng (ngày không có thì = 0)
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        List<Map<String, Object>> chart = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", d);
            item.put("label", String.format("%02d/%02d", d, month));
            item.put("revenue", byDay.getOrDefault(d, BigDecimal.ZERO));
            chart.add(item);
        }

        return ResponseEntity.ok(ApiResponse.success("Biểu đồ doanh thu theo ngày", chart));
    }

    // ── Biểu đồ theo tháng trong năm ─────────────────────────────

    @GetMapping("/chart/monthly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChartMonthly(@RequestParam int year) {
        LocalDateTime from = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime to = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        List<Payment> payments = getSuccessPaymentsBetween(from, to);

        // Group theo tháng
        Map<Integer, BigDecimal> byMonth = new TreeMap<>();
        for (Payment p : payments) {
            int m = p.getCreatedAt().getMonthValue();
            byMonth.merge(m, p.getAmount(), BigDecimal::add);
        }

        String[] monthNames = { "", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
                "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
                "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12" };

        List<Map<String, Object>> chart = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", m);
            item.put("label", monthNames[m]);
            item.put("revenue", byMonth.getOrDefault(m, BigDecimal.ZERO));
            chart.add(item);
        }

        return ResponseEntity.ok(ApiResponse.success("Biểu đồ doanh thu theo tháng năm " + year, chart));
    }

    // ── Private helpers ───────────────────────────────────────────

    private ResponseEntity<ApiResponse<Map<String, Object>>> getByDay(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(23, 59, 59);

        List<Payment> payments = getSuccessPaymentsBetween(from, to);

        Map<String, Object> data = buildRevenueData(payments);
        data.put("date", date.toString());

        return ResponseEntity.ok(ApiResponse.success("Doanh thu ngày " + date, data));
    }

    private List<Payment> getSuccessPayments() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS)
                .collect(Collectors.toList());
    }

    private List<Payment> getSuccessPaymentsBetween(LocalDateTime from, LocalDateTime to) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS)
                .filter(p -> p.getCreatedAt() != null
                        && !p.getCreatedAt().isBefore(from)
                        && !p.getCreatedAt().isAfter(to))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildRevenueData(List<Payment> payments) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalRevenue", sumAmount(payments));
        data.put("totalTransactions", payments.size());
        data.put("averageOrderValue", average(payments));

        Map<String, BigDecimal> byMethod = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMethod().name(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));
        data.put("revenueByMethod", byMethod);

        return data;
    }

    private BigDecimal sumAmount(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal average(List<Payment> payments) {
        if (payments.isEmpty())
            return BigDecimal.ZERO;
        return sumAmount(payments)
                .divide(BigDecimal.valueOf(payments.size()), 2, java.math.RoundingMode.HALF_UP);
    }
}