package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private OrderDataMapper orderDataMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;

    private final UUID CUSTOMER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID RESTAURANT_ID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    private final UUID PRODUCT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final UUID ORDER_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private final BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeAll
    public void init(){

        createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street_1")
                        .postalCode("100AB")
                        .city("Paris")
                        .build())
                .price(PRICE)
                .items(List.of(OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subtotal(new BigDecimal("50.00"))
                        .build(), OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subtotal(new BigDecimal("150.00"))
                        .build()))
                .build();

        createOrderCommandWrongPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street_1")
                        .postalCode("100AB")
                        .city("Paris")
                        .build())
                .price(new BigDecimal("250.00"))
                .items(List.of(OrderItem.builder()
                        .productId(PRODUCT_ID)
                        .quantity(1)
                        .price(new BigDecimal("50.00"))
                        .subtotal(new BigDecimal("50.00"))
                        .build(), OrderItem.builder()
                        .productId(PRODUCT_ID)
                        .quantity(3)
                        .price(new BigDecimal("50.00"))
                        .subtotal(new BigDecimal("150.00"))
                        .build()))
                .build();

        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street_1")
                        .postalCode("100AB")
                        .city("Paris")
                        .build())
                .price(new BigDecimal("210.00"))
                .items(List.of(OrderItem.builder()
                        .productId(PRODUCT_ID)
                        .quantity(1)
                        .price(new BigDecimal("60.00"))
                        .subtotal(new BigDecimal("60.00"))
                        .build(), OrderItem.builder()
                        .productId(PRODUCT_ID)
                        .quantity(3)
                        .price(new BigDecimal("50.00"))
                        .subtotal(new BigDecimal("150.00"))
                        .build()))
                .build();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse =  Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(new Product(new ProductId(PRODUCT_ID),"product-1",new Money(new BigDecimal("50.00")))
                ,new Product(new ProductId(PRODUCT_ID),"product-2",new Money(new BigDecimal("50.00")))))
                .active(true)
                .build();

        Order order = orderDataMapper.createOrderComaandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));

        when(orderRepository.save(any(Order.class))).thenReturn(order);

    }

    @Test
    public void testCreateOrder(){
        CreateOrderResponse createOrderResponse = orderApplicationService.creteOrder(createOrderCommand);
        assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
        assertEquals("order created successfully",createOrderResponse.getMessage());
        assertNotNull(createOrderResponse.getOrderTrackingId());
    }

    @Test
    public void testCreateOrderWithWrongPrice(){
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.creteOrder(createOrderCommandWrongPrice));
        assertEquals("total price : 250.00 is not equal to Order Items price : 200.00",orderDomainException.getMessage());

    }

    @Test
    public void testCreateOrderWithWrongProductPrice(){
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.creteOrder(createOrderCommandWrongProductPrice));
        assertEquals("Order Item price : 60.00 : is not valid for product " + PRODUCT_ID,orderDomainException.getMessage());

    }

    @Test
    public void testCreateOrderWithPassiveRestaurant(){
        Restaurant restaurantResponse = Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(new Product(new ProductId(PRODUCT_ID),"product-1",new Money(new BigDecimal("50.00")))
                        ,new Product(new ProductId(PRODUCT_ID),"product-2",new Money(new BigDecimal("50.00")))))
                .active(false)
                .build();

        when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));

        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.creteOrder(createOrderCommand));

        assertEquals("restaurant with id: " + RESTAURANT_ID + " is not Active!",orderDomainException.getMessage());

    }
}