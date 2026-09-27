package com.thanh.foodorder.feature.voucher.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.feature.cart.domain.Cart;
import com.thanh.foodorder.feature.cart.domain.CartDetail;
import com.thanh.foodorder.feature.cart.repository.CartDetailRepository;
import com.thanh.foodorder.feature.cart.repository.CartRepository;
import com.thanh.foodorder.feature.order.repository.OrderRepository;
import com.thanh.foodorder.feature.product.domain.Product;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;
import com.thanh.foodorder.feature.voucher.domain.Voucher;
import com.thanh.foodorder.feature.voucher.dto.ApplyVoucherRequest;
import com.thanh.foodorder.feature.voucher.enums.VoucherStatus;
import com.thanh.foodorder.feature.voucher.repository.VoucherRepository;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartDetailRepository cartDetailRepository;

    @InjectMocks
    private VoucherService voucherService;

    private Voucher voucher;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setId(1L);
        voucher.setCode("DISCOUNT20");
        voucher.setPercentDiscount(20);
        voucher.setMaxDiscount(BigDecimal.valueOf(50000));
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setExpiration(LocalDate.now().plusDays(10));
        voucher.setUsageLimit(10);
    }

    @Nested
    @DisplayName("updateVoucherStatus Tests")
    class UpdateVoucherStatusTests {

        @Test
        @DisplayName("updateVoucherStatus - With explicit INACTIVE status")
        void updateVoucherStatus_ExplicitInactive() {
            when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
            when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Voucher result = voucherService.updateVoucherStatus(1L, VoucherStatus.INACTIVE);

            assertEquals(VoucherStatus.INACTIVE, result.getStatus());
            verify(voucherRepository, times(1)).save(voucher);
        }

        @Test
        @DisplayName("updateVoucherStatus - With explicit ACTIVE status")
        void updateVoucherStatus_ExplicitActive() {
            voucher.setStatus(VoucherStatus.INACTIVE);
            when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
            when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Voucher result = voucherService.updateVoucherStatus(1L, VoucherStatus.ACTIVE);

            assertEquals(VoucherStatus.ACTIVE, result.getStatus());
            verify(voucherRepository, times(1)).save(voucher);
        }

        @Test
        @DisplayName("updateVoucherStatus - Toggle from ACTIVE to INACTIVE when status is null")
        void updateVoucherStatus_ToggleActiveToInactive() {
            voucher.setStatus(VoucherStatus.ACTIVE);
            when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
            when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Voucher result = voucherService.updateVoucherStatus(1L, null);

            assertEquals(VoucherStatus.INACTIVE, result.getStatus());
            verify(voucherRepository, times(1)).save(voucher);
        }

        @Test
        @DisplayName("updateVoucherStatus - Toggle from INACTIVE to ACTIVE when status is null")
        void updateVoucherStatus_ToggleInactiveToActive() {
            voucher.setStatus(VoucherStatus.INACTIVE);
            when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
            when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Voucher result = voucherService.updateVoucherStatus(1L, null);

            assertEquals(VoucherStatus.ACTIVE, result.getStatus());
            verify(voucherRepository, times(1)).save(voucher);
        }

        @Test
        @DisplayName("updateVoucherStatus - Voucher not found throws CommonException")
        void updateVoucherStatus_NotFound() {
            when(voucherRepository.findById(99L)).thenReturn(Optional.empty());

            CommonException ex = assertThrows(CommonException.class, () -> {
                voucherService.updateVoucherStatus(99L, VoucherStatus.INACTIVE);
            });

            assertEquals("Voucher with id 99 not found", ex.getMessage());
            verify(voucherRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("createVoucher Tests")
    class CreateVoucherTests {

        @Test
        @DisplayName("createVoucher - Defaults status to ACTIVE when null")
        void createVoucher_DefaultsStatusToActive() {
            Voucher newVoucher = new Voucher();
            newVoucher.setCode("NEWCODE");
            newVoucher.setStatus(null);

            when(voucherRepository.existsByCode("NEWCODE")).thenReturn(false);
            when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Voucher created = voucherService.createVoucher(newVoucher);

            assertEquals(VoucherStatus.ACTIVE, created.getStatus());
            verify(voucherRepository, times(1)).save(newVoucher);
        }

        @Test
        @DisplayName("createVoucher - Voucher code already exists throws CommonException")
        void createVoucher_AlreadyExists() {
            when(voucherRepository.existsByCode("DISCOUNT20")).thenReturn(true);

            assertThrows(CommonException.class, () -> {
                voucherService.createVoucher(voucher);
            });

            verify(voucherRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("checkVoucherBeforeApply Tests")
    class CheckVoucherBeforeApplyTests {

        @Test
        @DisplayName("checkVoucherBeforeApply - Inactive voucher throws CommonException")
        void checkVoucherBeforeApply_Inactive_ThrowsException() {
            voucher.setStatus(VoucherStatus.INACTIVE);
            User user = new User();

            CommonException ex = assertThrows(CommonException.class, () -> {
                voucherService.checkVoucherBeforeApply(voucher, user);
            });

            assertEquals("Voucher DISCOUNT20 is inactive", ex.getMessage());
        }

        @Test
        @DisplayName("checkVoucherBeforeApply - Active voucher proceeds without exception")
        void checkVoucherBeforeApply_Active_Success() {
            User user = new User();
            when(orderRepository.existsByUserAndVoucher(user, voucher)).thenReturn(false);

            voucherService.checkVoucherBeforeApply(voucher, user);
        }
    }

    @Nested
    @DisplayName("applyVoucher Tests")
    class ApplyVoucherTests {

        @Test
        @DisplayName("applyVoucher - Inactive voucher throws CommonException")
        void applyVoucher_Inactive_ThrowsException() {
            voucher.setStatus(VoucherStatus.INACTIVE);

            User user = new User();
            user.setId(1L);

            Cart cart = new Cart();
            cart.setId(1L);

            Product product = new Product();
            product.setPrice(BigDecimal.valueOf(100000));

            CartDetail cartDetail = new CartDetail();
            cartDetail.setProduct(product);
            cartDetail.setQuantity(1);

            try (MockedStatic<JwtUtil> mockedJwt = Mockito.mockStatic(JwtUtil.class)) {
                mockedJwt.when(JwtUtil::getCurrentUserLogin).thenReturn(Optional.of("user@example.com"));
                when(userService.getUserByEmail("user@example.com")).thenReturn(user);
                when(cartRepository.findByUserId(1L)).thenReturn(cart);
                when(cartDetailRepository.findByCart(cart)).thenReturn(List.of(cartDetail));
                when(voucherRepository.findByCode("DISCOUNT20")).thenReturn(Optional.of(voucher));

                ApplyVoucherRequest request = new ApplyVoucherRequest();
                request.setCode("DISCOUNT20");

                CommonException ex = assertThrows(CommonException.class, () -> {
                    voucherService.applyVoucher(request);
                });

                assertEquals("Voucher không còn hoạt động", ex.getMessage());
            }
        }
    }
}
