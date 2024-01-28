package thainguyen.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.Discount;
import thainguyen.domain.LineItem;
import thainguyen.domain.Order;
import thainguyen.domain.Shipment;
import thainguyen.domain.valuetypes.Price;
import thainguyen.domain.valuetypes.Status;
import thainguyen.dto.order.OrderDto;
import thainguyen.dto.order.OrderMapper;
import thainguyen.service.order.OrderService;
import thainguyen.service.shipment.ShipmentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.security.Principal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/orders", produces = "application/json")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ShipmentService shipmentService;

    public OrderController(OrderService orderService, ShipmentService shipmentService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.shipmentService = shipmentService;
        this.orderMapper = orderMapper;
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<Order> findById(@PathVariable Long id, Principal principal) {
        Optional<Order> orderOptional = orderService.findByIdAndOwner(id, principal.getName());
        return orderOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    private ResponseEntity<List<Order>> findAll(Principal principal) {
        List<Order> orders = orderService.findByOwner(principal.getName());
        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orders);
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<Void> postEntity (@RequestBody OrderDto orderDto
            , UriComponentsBuilder ucb
            , Principal principal) throws JsonProcessingException {
        Order order = orderMapper.convertOrderDtoToEntity(orderDto);

        if (order == null) return ResponseEntity.unprocessableEntity().build();

        Order orderSaved = orderService.create(order, principal.getName());
        Price totalPriceOfGoods = calcTotalPriceOfGoods(order.getLineItems());

        // luc nay chua co thong tin phi giao hang
        order.setTotalPriceBeforeDiscount(totalPriceOfGoods);

        Shipment shipment = shipmentService.createShipment(orderSaved);

        if (shipment == null) return ResponseEntity.unprocessableEntity().build();

        order.setShipment(shipment);
        Price fee = shipment.getFee();
        // calc total price include delivery fee and update to field totalPriceBeforeDiscount
        order.setTotalPriceBeforeDiscount(new Price(fee.getValue().add(totalPriceOfGoods.getValue()), Currency.getInstance("VND")));

        // after discount
        Order orderAfterDiscount = applyDiscount(order, totalPriceOfGoods, fee);

        order.setStatus(Status.PENDING);
        Order savedOrder = orderService.create(orderAfterDiscount);

        URI locationOfNewOrder = ucb.path("/api/orders/{id}")
                .buildAndExpand(savedOrder.getId()).toUri();
        return ResponseEntity.created(locationOfNewOrder).build();
    }

    private Order applyDiscount(Order order, Price totalPriceOfGoods, Price feeDelivery) {
        // init total price
        Price totalPrice = new Price();
        totalPrice.setCurrency(Currency.getInstance("VND"));

        BigDecimal totalPriceOfGoodsValue = totalPriceOfGoods.getValue();
        BigDecimal feeDeliveryValue = feeDelivery.getValue();

        List<Discount> discounts = order.getDiscounts();
        if (discounts == null || discounts.isEmpty()) {
            totalPrice.setValue(totalPriceOfGoods.getValue().add(feeDelivery.getValue()));
        }
        else {
            for (Discount discount : discounts) {
                if (discount.getKind().equals(Discount.Kind.NORMAL)) {
                    if (discount.getType().equals(Discount.Type.AMOUNT)) {
                        totalPriceOfGoodsValue = totalPriceOfGoodsValue.subtract(BigDecimal.valueOf(discount.getValue()));
                    }
                    else {
                        totalPriceOfGoodsValue = totalPriceOfGoodsValue.subtract(
                                totalPriceOfGoodsValue.multiply(BigDecimal.valueOf(discount.getValue()))
                                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
                        );
                    }
                    // discount fee delivery
                } else {
                    if (discount.getType().equals(Discount.Type.AMOUNT)) {
                        feeDeliveryValue = feeDeliveryValue.subtract(BigDecimal.valueOf(discount.getValue()));
                    }
                    else {
                        feeDeliveryValue = feeDeliveryValue.subtract(
                                feeDeliveryValue.multiply(BigDecimal.valueOf(discount.getValue()))
                                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
                        );
                    }
                }
            }
        }

        totalPrice.setValue(totalPriceOfGoodsValue.add(feeDeliveryValue));
        order.setTotalPriceAfterDiscount(totalPrice);
        return order;
    }

    private Price calcTotalPriceOfGoods(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty())
            return new Price(BigDecimal.valueOf(0), Currency.getInstance("VND"));

        BigDecimal totalValue =  lineItems.stream().map(lineItem -> lineItem.getTotalPrice().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Price(totalValue, Currency.getInstance("VND"));
    }

}
