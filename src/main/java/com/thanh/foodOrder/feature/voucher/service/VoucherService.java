package com.thanh.foodorder.feature.voucher.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.cart.domain.Cart;
import com.thanh.foodorder.feature.cart.domain.CartDetail;
import com.thanh.foodorder.feature.cart.repository.CartDetailRepository;
import com.thanh.foodorder.feature.cart.repository.CartRepository;
import com.thanh.foodorder.feature.order.domain.Order;
import com.thanh.foodorder.feature.order.repository.OrderRepository;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;
import com.thanh.foodorder.feature.voucher.domain.Voucher;
import com.thanh.foodorder.feature.voucher.dto.ApplyVoucherRequest;
import com.thanh.foodorder.feature.voucher.dto.ApplyVoucherResponse;
import com.thanh.foodorder.feature.voucher.repository.VoucherRepository;

@Service
@Log4j2
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;

    public VoucherService(VoucherRepository voucherRepository, OrderRepository orderRepository,
            UserService userService, CartDetailRepository cartDetailRepository, CartRepository cartRepository) {
        this.voucherRepository = voucherRepository;
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartDetailRepository = cartDetailRepository;
        this.cartRepository = cartRepository;
    }

    public void saveVoucher(Voucher voucher) {
        this.voucherRepository.save(voucher);
    }

    public Voucher getVoucherById(Long id) {
        return this.voucherRepository.findById(id).orElseThrow(() -> {
            log.warn("Voucher with id: {} not found", id);
            return new CommonException("Voucher with id " + id + " not found");
        });
    }

    public Voucher getVoucherByCode(String code) {
        return this.voucherRepository.findByCode(code).orElseThrow(() -> {
            log.warn("Voucher with code: {} not found", code);
            return new CommonException("Voucher with code " + code + " not found");
        });
    }

    public boolean checkVoucherExistsByCode(String code) {
        return this.voucherRepository.existsByCode(code);
    }

    public Voucher createVoucher(Voucher voucher) {
        log.info("Creating voucher with code: {}", voucher.getCode());

        if (checkVoucherExistsByCode(voucher.getCode())) {
            log.warn("Voucher  {} already exists", voucher.getCode());
            throw new CommonException("Voucher " + voucher.getCode() + " already exists");
        }
        log.info("Voucher created successfully with code: {}", voucher.getCode());

        return this.voucherRepository.save(voucher);
    }

    public Voucher updatVoucher(Voucher voucher) {
        Voucher voucherDb = this.getVoucherById(voucher.getId());

        if (!voucher.getCode().equals(voucherDb.getCode())) {

            if (checkVoucherExistsByCode(voucher.getCode())) {
                log.warn("Voucher  {} already exists", voucher.getCode());
                throw new CommonException("Voucher " + voucher.getCode() + " already exists");
            }
        }
        if (voucher.getExpiration().isBefore(LocalDate.now())) {
            throw new CommonException("Expiration date must be in the future");
        }

        voucherDb.setCode(voucher.getCode());
        voucherDb.setExpiration(voucher.getExpiration());
        voucherDb.setPercentDiscount(voucher.getPercentDiscount());
        voucherDb.setMaxDiscount(voucher.getMaxDiscount());
        return this.voucherRepository.save(voucherDb);
    }

    public void delteVoucherById(Long id) {
        Voucher voucher = getVoucherById(id);
        this.voucherRepository.delete(voucher);
    }

    public ResultPaginationDTO getAllVouchers(int page, int size, String code) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Voucher> voucherPage;

        if (code == null || code.isBlank()) {
            voucherPage = this.voucherRepository.findAll(pageable);
        } else {
            voucherPage = this.voucherRepository.findByCodeContainingIgnoreCase(code, pageable);
        }

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(voucherPage.getTotalPages());
        meta.setTotalElements(voucherPage.getTotalElements());

        rs.setMeta(meta);

        List<Voucher> vouchers = voucherPage.getContent().stream()
                .map(voucher -> new Voucher(voucher.getId(), voucher.getCode(), voucher.getPercentDiscount(),
                        voucher.getMaxDiscount(), voucher.getExpiration(), voucher.getCreatedBy(),
                        voucher.getUpdatedBy(), voucher.getCreatedAt(), voucher.getUpdatedAt(),
                        voucher.getUsageLimit()))
                .collect(Collectors.toList());
        rs.setResults(vouchers);

        return rs;

    }

    private boolean checkVoucherExpired(Voucher voucher) {
        LocalDate today = LocalDate.now();
        return today.isAfter(voucher.getExpiration());
    }

    private boolean checkUsageVoucher(Voucher voucher) {

        return voucher.getUsageLimit() == 0;
    }

    public void checkVoucherBeforeApply(Voucher voucher, User user) {
        if (checkVoucherExpired(voucher)) {
            throw new CommonException("Voucher " + voucher.getCode() + " is expired");

        }
        if (checkUsageVoucher(voucher)) {
            throw new CommonException("Voucher " + voucher.getCode() + " has been fully used.");

        }
        if (orderRepository.existsByUserAndVoucher(user, voucher)) {
            throw new CommonException(
                    "Voucher " + voucher.getCode() + " has already been used by this user.");
        }
    }

    public boolean checkVoucherUsedByUser(User user, Voucher voucher) {

        return this.orderRepository.existsByUserAndVoucher(user, voucher);

    }

    public ApplyVoucherResponse applyVoucher(
            ApplyVoucherRequest request) {

        String email = JwtUtil.getCurrentUserLogin().orElse("");

        User user = userService.getUserByEmail(email);

        Cart cart = cartRepository.findByUserId(user.getId());

        List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);

        if (cartDetails.isEmpty()) {
            throw new CommonException("Giỏ hàng đang trống");
        }

        // Tính tổng tiền từ DB
        BigDecimal originalTotal = BigDecimal.ZERO;

        for (CartDetail cartDetail : cartDetails) {

            BigDecimal price = cartDetail.getProduct().getPrice();

            BigDecimal quantity = BigDecimal.valueOf(
                    cartDetail.getQuantity());

            originalTotal = originalTotal.add(
                    price.multiply(quantity));
        }

        Voucher voucher = voucherRepository
                .findByCode(request.getCode())
                .orElseThrow(() -> new CommonException("Voucher không tồn tại"));

        // Kiểm tra hạn
        if (voucher.getExpiration() != null
                && voucher.getExpiration().isBefore(LocalDate.now())) {

            throw new CommonException("Voucher đã hết hạn");
        }

        // Tính discount
        BigDecimal discount = originalTotal
                .multiply(
                        BigDecimal.valueOf(
                                voucher.getPercentDiscount()))
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP);

        // Không giảm quá maxDiscount
        if (voucher.getMaxDiscount() != null
                && discount.compareTo(voucher.getMaxDiscount()) > 0) {

            discount = voucher.getMaxDiscount();
        }

        BigDecimal finalTotal = originalTotal.subtract(discount);

        return ApplyVoucherResponse.builder()
                .code(voucher.getCode())
                .originalTotal(originalTotal)
                .discountAmount(discount)
                .finalTotal(finalTotal)
                .build();
    }

}
