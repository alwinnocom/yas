package com.yas.order.service;

import com.yas.order.OrderApplication;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.order.OrderItemPostVm;
import com.yas.order.viewmodel.order.OrderPostVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(classes = OrderApplication.class)
class OrderServiceTest {

    @MockBean
    private ProductService productService;
    @MockBean
    private CartService cartService;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderService orderService;
    private OrderItemPostVm orderItemPostVm;
    private OrderAddressPostVm orderAddressPostVm;
    private OrderPostVm orderPostVm;


    @BeforeEach
    void setUp() {
        orderItemPostVm = OrderItemPostVm.builder()
                .productId(1L).productName("abc")
                .quantity(1).productPrice(BigDecimal.TEN)
                .discountAmount(BigDecimal.ONE).taxAmount(BigDecimal.ONE).taxPercent(BigDecimal.ONE)
                .build();
        orderAddressPostVm = OrderAddressPostVm.builder()
                .contactName("contactName").phone("phone").addressLine1("addressLine1").addressLine2("addressLine2")
                .city("city").zipCode("zipCode").districtId(1L).districtName("districtName")
                .stateOrProvinceId(1L).stateOrProvinceName("stateOrProvinceName")
                .countryId(1L).countryName("countryName")
                .build();

        orderPostVm = OrderPostVm.builder()
                .checkoutId("1").email("abc@gmail.com")
                .orderItemPostVms(Arrays.asList(orderItemPostVm))
                .billingAddressPostVm(orderAddressPostVm)
                .shippingAddressPostVm(orderAddressPostVm)
                .build();
    }

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrder_successful() {

        OrderVm orderVm = orderService.createOrder(orderPostVm);

        Optional<Order> orderOptional = orderRepository.findById(orderVm.id());
        assertTrue(orderOptional.isPresent());
        Order orderDB = orderOptional.get();
        assertEquals("abc@gmail.com", orderDB.getEmail());
        assertEquals(1, orderDB.getOrderItems().size());
        assertEquals("abc", orderDB.getOrderItems().stream().findFirst().get().getProductName());
    }

    @Test
    void testCreateOrder_RemoteServiceThrowsException_RollbackOrder() {
        doThrow(new RuntimeException()).when(productService).subtractProductStockQuantity(any(OrderVm.class));
        try {
            orderService.createOrder(orderPostVm);
        } catch (Exception e) {

        }
        List<Order> orders = orderRepository.findAll();
        assertEquals(0, orders.size());
        List<OrderItem> orderItems = orderItemRepository.findAll();
        assertEquals(0, orderItems.size());
    }

}
