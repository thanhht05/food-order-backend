package com.thanh.foodorder.feature.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.thanh.foodorder.core.response.ResultPaginationDTO;
import com.thanh.foodorder.core.util.exception.CommonException;
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.category.domain.Category;
import com.thanh.foodorder.feature.category.service.CategoryService;
import com.thanh.foodorder.feature.product.domain.Product;
import com.thanh.foodorder.feature.product.domain.ProductImage;
import com.thanh.foodorder.feature.product.dto.ProductRequestDTO;
import com.thanh.foodorder.feature.product.dto.ProductUpdateRequestDTO;
import com.thanh.foodorder.feature.product.dto.ResponseProductDTO;
import com.thanh.foodorder.feature.product.enums.ProductStatus;
import com.thanh.foodorder.feature.product.repository.ProductRepository;
import com.thanh.foodorder.feature.user.domain.Role;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.user.service.UserService;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
        @Mock
        private ProductRepository productRepository;
        @Mock
        private CategoryService categoryService;
        @Mock

        private UserService userService;

        @InjectMocks
        private ProductService productService;

        private Product product;
        private Category cate;

        @BeforeEach
        void setUp() {
                cate = new Category();
                cate.setId(1L);
                cate.setName("testCate");

                product = new Product();
                product.setId(1L);
                product.setName("cafe");
                product.setPrice(BigDecimal.valueOf(12000));
                product.setQuantity(100);
                product.setSold(10);
                product.setStatus(ProductStatus.ACTIVE);
                product.setCategory(cate);

                ProductImage img = new ProductImage();
                img.setId(1L);
                img.setImgName("testimg.png");
                img.setProduct(product);
                img.set_primary(true);

                product.setLstImg(new ArrayList<>(List.of(img)));
        }

        @Test
        void createProduct_duplicateProduct_shouldThrowException() {

                ProductRequestDTO dto = new ProductRequestDTO();

                dto.setName("cafe");
                dto.setCategoryName("testCate");
                dto.setPrice(BigDecimal.valueOf(12000));
                dto.setQuantity(100);

                // categoryService trả về cate
                when(categoryService.getCategoryByName("testCate"))
                                .thenReturn(cate);

                // Product đã tồn tại
                when(productRepository.existsByNameAndCategory(
                                eq("cafe"),
                                any(Category.class)))
                                .thenReturn(true);

                assertThrows(
                                RuntimeException.class,
                                () -> productService.createProduct(dto));

                verify(productRepository)
                                .existsByNameAndCategory(
                                                eq("cafe"),
                                                eq(cate));
        }

        @Test
        void createProductSuccessfully() {
                ProductRequestDTO dto = new ProductRequestDTO();

                dto.setName("cafe");
                dto.setCategoryName("testCate");
                dto.setPrice(BigDecimal.valueOf(12000));
                dto.setQuantity(100);
                String img = "testimg.png";
                List<String> imgs = List.of(img);

                dto.setLstImg(imgs);

                // categoryService trả về cate
                when(categoryService.getCategoryByName("testCate"))
                                .thenReturn(cate);

                // Product đã tồn tại
                when(productRepository.existsByNameAndCategory(
                                eq("cafe"),
                                any(Category.class)))
                                .thenReturn(false);

                when(productRepository.save(any(Product.class))).thenReturn(product);

                ResponseProductDTO res = productService.createProduct(dto);

                assertEquals("cafe", res.getName());
                assertEquals(100, res.getQuantity());

        }

        @Test
        void getUserById_notFound_shouldThrowException() {
                when(productRepository.findById(1L)).thenReturn(Optional.empty());

                assertThrows(CommonException.class, () -> productService.getProductById(1L));

                verify(productRepository).findById(1L);
        }

        @Test
        void getProductById_successfully() {
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));

                Product p = productService.getProductById(1L);

                assertEquals("cafe", p.getName());
                verify(productRepository).findById(1L);

        }

        @Test
        void admin_shouldGetAllProductSuccessfully() {
                Role role = new Role();
                role.setId(1L);
                role.setName("ADMIN");

                User user = new User();
                user.setId(1L);

                user.setRole(role);
                List<Product> productList = List.of(product);
                Pageable pageable = PageRequest.of(0, 10);

                Page<Product> page = new PageImpl<>(
                                productList,
                                pageable,
                                1);

                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));

                        when(userService.getUserByEmail("test@gmail.com")).thenReturn(user);

                        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                                        .thenReturn(page);

                        ResultPaginationDTO result = productService.search(null, null, null, null, 1, 10, null);

                        assertEquals(1, result.getMeta().getPage());
                        assertEquals(10, result.getMeta().getPageSize());
                        assertEquals(1, result.getMeta().getPages());
                        assertEquals(1, result.getMeta().getTotalElements());

                        List<ResponseProductDTO> results = (List<ResponseProductDTO>) result.getResults();

                        assertEquals("cafe", results.get(0).getName());
                        assertEquals(BigDecimal.valueOf(12000), results.get(0).getPrice());

                }

        }

        @Test
        void user_shouldGetAllProductSuccessfully() {
                Role role = new Role();
                role.setId(2L);
                role.setName("USER");

                User user = new User();
                user.setId(1L);
                user.setFullName("test");
                user.setEmail("test@gmail.com");
                user.setPassword("123456");
                user.setRole(role);

                Product activeProduct = new Product();
                activeProduct.setId(1L);
                activeProduct.setName("cafe");
                activeProduct.setPrice(BigDecimal.valueOf(12000));
                activeProduct.setStatus(ProductStatus.ACTIVE);
                activeProduct.setCategory(cate);

                List<Product> productList = List.of(activeProduct);
                Pageable pageable = PageRequest.of(0, 10);
                Page<Product> page = new PageImpl<>(
                                productList,
                                pageable,
                                1);

                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));

                        when(userService.getUserByEmail("test@gmail.com")).thenReturn(user);

                        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                                        .thenReturn(page);

                        ResultPaginationDTO result = productService.search(null, null, null, null, 1, 10, null);

                        assertEquals(1, result.getMeta().getPage());
                        assertEquals(10, result.getMeta().getPageSize());
                        assertEquals(1, result.getMeta().getPages());
                        assertEquals(1, result.getMeta().getTotalElements());

                        List<ResponseProductDTO> results = (List<ResponseProductDTO>) result.getResults();

                        assertEquals("cafe", results.get(0).getName());
                        assertEquals("ACTIVE", results.get(0).getProductStatus().toString());
                        assertEquals(BigDecimal.valueOf(12000), results.get(0).getPrice());

                }

        }

        @Test
        void user_shouldGetProductByNameSuccessfullt() {
                Role role = new Role();
                role.setId(2L);
                role.setName("USER");

                User user = new User();
                user.setId(1L);
                user.setRole(role);

                Product product = new Product();
                product.setId(1L);
                product.setName("testName");
                product.setQuantity(999);
                product.setCategory(cate);

                List<Product> productList = List.of(product);
                Pageable pageable = PageRequest.of(0, 10);

                Page<Product> page = new PageImpl<>(
                                productList,
                                pageable,
                                1);

                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));
                        when(userService.getUserByEmail("test@gmail.com")).thenReturn(user);

                        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                                        .thenReturn(page);

                        ResultPaginationDTO result = productService.search("testName", null, null, null, 1, 10, null);
                        List<ResponseProductDTO> results = (List<ResponseProductDTO>) result.getResults();

                        assertEquals("testName", results.get(0).getName());
                        assertEquals(999, results.get(0).getQuantity());
                }

                verify(productRepository)
                                .findAll(
                                                any(Specification.class),
                                                any(Pageable.class));
        }

        @Test
        void user_shouldeGetProductBy_name_category_successfuully() {
                Role role = new Role();
                role.setId(2L);
                role.setName("USER");

                User user = new User();
                user.setId(1L);
                user.setRole(role);

                Product product = new Product();
                product.setId(1L);
                product.setName("testName");
                product.setQuantity(999);
                product.setCategory(cate);

                List<Product> productList = List.of(product);
                Pageable pageable = PageRequest.of(0, 10);

                Page<Product> page = new PageImpl<>(
                                productList,
                                pageable,
                                1);

                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));
                        when(userService.getUserByEmail("test@gmail.com")).thenReturn(user);

                        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                                        .thenReturn(page);

                        ResultPaginationDTO result = productService.search("testName", List.of("testCate"), null, null,
                                        1, 10,
                                        null);
                        List<ResponseProductDTO> results = (List<ResponseProductDTO>) result.getResults();

                        assertEquals("testName", results.get(0).getName());
                        assertEquals(999, results.get(0).getQuantity());
                        assertEquals("testCate", results.get(0).getProductCate().getName());
                }

                verify(productRepository)
                                .findAll(
                                                any(Specification.class),
                                                any(Pageable.class));
        }

        @Test
        void user_shouldGetProductSortedBySoldDescending() {

                // Arrange
                Role role = new Role();
                role.setId(2L);
                role.setName("USER");

                User user = new User();
                user.setId(1L);
                user.setRole(role);

                Product product = new Product();
                product.setId(1L);
                product.setName("testName");
                product.setQuantity(999);
                product.setSold(0);
                product.setCategory(cate);

                Product product2 = new Product();
                product2.setId(2L);
                product2.setName("testName2");
                product2.setQuantity(999);
                product2.setSold(100);
                product2.setCategory(cate);

                // Repository giả lập kết quả đã được sort
                List<Product> productList = List.of(
                                product2, // sold = 100
                                product // sold = 0
                );

                Pageable pageable = PageRequest.of(0, 10);

                Page<Product> page = new PageImpl<>(
                                productList,
                                pageable,
                                2);

                ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

                try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {

                        jwtUtil.when(JwtUtil::getCurrentUserLogin)
                                        .thenReturn(Optional.of("test@gmail.com"));

                        when(userService.getUserByEmail("test@gmail.com"))
                                        .thenReturn(user);

                        when(productRepository.findAll(
                                        any(Specification.class),
                                        pageableCaptor.capture())).thenReturn(page);

                        // Act
                        ResultPaginationDTO result = productService.search(
                                        null,
                                        null,
                                        null,
                                        null,
                                        1,
                                        10,
                                        "sold,desc");

                        // Assert kết quả
                        List<ResponseProductDTO> results = (List<ResponseProductDTO>) result.getResults();

                        assertEquals(2, results.size());

                        assertEquals("testName2", results.get(0).getName());
                        assertEquals(100, results.get(0).getSold());

                        assertEquals("testName", results.get(1).getName());
                        assertEquals(0, results.get(1).getSold());

                        // Assert Pageable
                        Pageable actualPageable = pageableCaptor.getValue();

                        assertEquals(0, actualPageable.getPageNumber());
                        assertEquals(10, actualPageable.getPageSize());

                        assertEquals(
                                        "sold",
                                        actualPageable.getSort()
                                                        .iterator()
                                                        .next()
                                                        .getProperty());

                        assertEquals(
                                        Sort.Direction.DESC,
                                        actualPageable.getSort()
                                                        .iterator()
                                                        .next()
                                                        .getDirection());
                }

                verify(productRepository).findAll(
                                any(Specification.class),
                                any(Pageable.class));
        }

        @Test
        void checkQuantityProductBeforeAddToCart_shouldThrowException() {
                int quantityInCart = 1001;

                assertThrows(
                                CommonException.class,
                                () -> productService.checkQuantityProductBeforeAddToCart(product, quantityInCart));

        }

        @Test
        void SshouldDeleteProductSuccessfully() {
                when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

                when(productRepository.countByCategory_Id(product.getCategory().getId()))
                                .thenReturn(0L);
                productService.handleDeleteProduct(product.getId());

                verify(productRepository, times(1)).delete(product);
                verify(productRepository, times(1)).countByCategory_Id(product.getCategory().getId());
                verify(categoryService, times(1)).deleteCategory(product.getCategory().getId());

        }

        @Test
        void shouldDeleteProductButKeepCategoryIfProductsRemain() {

                when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

                // Simulate that 5 products are still in this category
                when(productRepository.countByCategory_Id(product.getCategory().getId())).thenReturn(5L);

                // Act
                productService.handleDeleteProduct(product.getId());

                // Verify
                verify(productRepository, times(1)).delete(product);
                verify(productRepository, times(1)).countByCategory_Id(product.getCategory().getId());

                // VERIFY category is NOT deleted
                verify(categoryService, never()).deleteCategory(anyLong());
        }

        @Test
        void shouldUpdateProductStatusToInActiveSuccessfully() {

                when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

                productService.updateProductStatus(product.getId());

                assertEquals(ProductStatus.INACTIVE, product.getStatus());

                verify(productRepository, times(1)).save(product);

        }

        @Test
        void shouldUpdateProductStatusToActiveSuccessfully() {
                product.setStatus(ProductStatus.INACTIVE);

                when(productRepository.findById(product.getId()))
                                .thenReturn(Optional.of(product));

                productService.updateProductStatus(product.getId());

                assertEquals(ProductStatus.ACTIVE, product.getStatus());

                verify(productRepository, times(1)).save(product);
        }

        @Test
        void shouldUpdateProductSuccesfully() {
                ProductUpdateRequestDTO req = new ProductUpdateRequestDTO();
                req.setId(1L);
                req.setName("testNameUpdate");
                req.setQuantity(333);
                req.setSold(1);

                String img = "imgUpdate";
                List<String> lstimg = List.of(img);

                req.setLstImg(lstimg);

                when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

                productService.updateProduct(req);

                assertEquals("testNameUpdate", product.getName());
                assertEquals("imgUpdate", product.getLstImg().get(0).getImgName());
                assertEquals(333, product.getQuantity());
                assertEquals(1, product.getSold());
                verify(productRepository).save(product);

        }
}
