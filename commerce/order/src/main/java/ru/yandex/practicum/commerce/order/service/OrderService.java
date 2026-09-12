package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.client.PaymentClient;
import ru.yandex.practicum.commerce.interaction.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderState;
import ru.yandex.practicum.commerce.interaction.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interaction.dto.shoppingcart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final ShoppingCartClient shoppingCartClient;

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {

        OrderEntity order = orderMapper.toEntity(request);
        order.setState(OrderState.NEW);

        OrderEntity savedOrder = orderRepository.save(order);

        log.info(
                "Создан заказ orderId={}, shoppingCartId={}, state={}",
                savedOrder.getOrderId(),
                savedOrder.getShoppingCartId(),
                savedOrder.getState()
        );

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        log.info(
                "Получен адрес склада для orderId={}",
                savedOrder.getOrderId()
        );

        DeliveryDto deliveryDto =
                orderMapper.toDeliveryDto(savedOrder, warehouseAddress);

        DeliveryDto savedDelivery =
                deliveryClient.planDelivery(deliveryDto);

        savedOrder.setDeliveryId(savedDelivery.getDeliveryId());

        OrderEntity updatedOrder = orderRepository.save(savedOrder);

        log.info(
                "Для заказа orderId={} создана доставка deliveryId={}",
                updatedOrder.getOrderId(),
                updatedOrder.getDeliveryId()
        );

        return orderMapper.toDto(updatedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {

        ShoppingCartDto shoppingCart =
                shoppingCartClient.getShoppingCart(username);

        List<OrderEntity> orders =
                orderRepository.findAllByShoppingCartId(
                        shoppingCart.getShoppingCartId()
                );

        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {

        log.info("Начало сборки orderId={}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);

        AssemblyProductsForOrderRequest request =
                AssemblyProductsForOrderRequest.builder()
                        .orderId(order.getOrderId())
                        .products(order.getProducts())
                        .build();

        BookedProductsDto bookedProducts =
                warehouseClient.assemblyProductsForOrder(request);

        order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        order.setFragile(bookedProducts.getFragile());

        order.setState(OrderState.ASSEMBLED);

        OrderEntity savedOrder =
                orderRepository.save(order);

        log.info(
                "Заказ orderId={} собран, state={}, weight={}, volume={}, fragile={}",
                savedOrder.getOrderId(),
                savedOrder.getState(),
                savedOrder.getDeliveryWeight(),
                savedOrder.getDeliveryVolume(),
                savedOrder.getFragile()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {

        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.ASSEMBLY_FAILED);

        OrderEntity savedOrder = orderRepository.save(order);

        log.warn(
                "Сборка заказа завершилась ошибкой orderId={}, state={}",
                savedOrder.getOrderId(),
                savedOrder.getState()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {

        OrderEntity order = getOrderOrThrow(orderId);

        if (order.getState() == OrderState.ON_PAYMENT) {

            order.setState(OrderState.PAID);

            OrderEntity savedOrder = orderRepository.save(order);

            log.info(
                    "Заказ orderId={} успешно оплачен, paymentId={}, state={}",
                    orderId,
                    savedOrder.getPaymentId(),
                    savedOrder.getState()
            );

            return orderMapper.toDto(savedOrder);
        }

        OrderDto orderDto = orderMapper.toDto(order);

        PaymentDto payment = paymentClient.payment(orderDto);

        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);

        OrderEntity savedOrder = orderRepository.save(order);

        log.info(
                "Для заказа orderId={} создан платёж paymentId={}, state={}",
                orderId,
                savedOrder.getPaymentId(),
                savedOrder.getState()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {

        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.PAYMENT_FAILED);

        OrderEntity savedOrder = orderRepository.save(order);

        log.warn(
                "Ошибка оплаты orderId={}, state={}",
                orderId,
                savedOrder.getState()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {

        log.info("Расчёт стоимости доставки orderId={}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);

        OrderDto orderDto = orderMapper.toDto(order);

        BigDecimal deliveryCost =
                deliveryClient.deliveryCost(orderDto);

        order.setDeliveryPrice(deliveryCost);

        OrderEntity savedOrder =
                orderRepository.save(order);

        log.info(
                "Стоимость доставки orderId={}, deliveryPrice={}",
                orderId,
                deliveryCost
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {

        log.info("Расчёт полной стоимости orderId={}", orderId);

        OrderEntity order = getOrderOrThrow(orderId);

        OrderDto orderDto = orderMapper.toDto(order);

        BigDecimal productCost =
                paymentClient.productCost(orderDto);

        order.setProductPrice(productCost);

        OrderDto orderWithProductPrice =
                orderMapper.toDto(order);

        BigDecimal totalCost =
                paymentClient.getTotalCost(orderWithProductPrice);

        order.setTotalPrice(totalCost);

        OrderEntity savedOrder =
                orderRepository.save(order);

        log.info(
                "Стоимость заказа orderId={}: productPrice={}, deliveryPrice={}, totalPrice={}",
                orderId,
                savedOrder.getProductPrice(),
                savedOrder.getDeliveryPrice(),
                savedOrder.getTotalPrice()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {

        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.DELIVERED);

        OrderEntity savedOrder = orderRepository.save(order);

        log.info(
                "Заказ доставлен orderId={}, state={}",
                orderId,
                savedOrder.getState()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {

        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.DELIVERY_FAILED);

        OrderEntity savedOrder = orderRepository.save(order);

        log.warn(
                "Ошибка доставки orderId={}, state={}",
                orderId,
                savedOrder.getState()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {

        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.COMPLETED);

        OrderEntity savedOrder = orderRepository.save(order);

        log.info(
                "Заказ завершён orderId={}, state={}",
                orderId,
                savedOrder.getState()
        );

        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {

        log.info(
                "Возврат товаров для orderId={}, products={}",
                request.getOrderId(),
                request.getProducts()
        );

        OrderEntity order = getOrderOrThrow(request.getOrderId());

        warehouseClient.acceptReturn(request.getProducts());

        order.setState(OrderState.PRODUCT_RETURNED);

        OrderEntity savedOrder = orderRepository.save(order);

        log.info(
                "Возврат завершён orderId={}, state={}",
                savedOrder.getOrderId(),
                savedOrder.getState()
        );

        return orderMapper.toDto(savedOrder);
    }

    private OrderEntity getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }
}
