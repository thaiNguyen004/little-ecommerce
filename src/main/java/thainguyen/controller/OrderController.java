package thainguyen.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.controller.conf.CustomResponse;
import thainguyen.controller.exception.GhtkCreateOrderFailedException;
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

        // Bad request due must not be null field required
        if (orderDto.getLineItems() == null || orderDto.getLineItems().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Order order = orderMapper.convertOrderDtoToEntity(orderDto);
        // Unprocessable because discount and detail product not found
        if (order == null) return ResponseEntity.unprocessableEntity().build();

        order = orderService.create(order, principal.getName());
        Price totalPriceOfGoods = calcTotalPriceOfGoods(order.getLineItems());

        // luc nay chua co thong tin phi giao hang
        order.setTotalPriceBeforeDiscount(totalPriceOfGoods);

        try {
            Shipment shipment = shipmentService.createShipment(order);
            order.setShipment(shipment);
            order.setStatus(Status.PENDING);

            Price fee = shipment.getFee();
            // calc total price include delivery fee and update to field totalPriceBeforeDiscount
            order.setTotalPriceBeforeDiscount(new Price(fee.getValue().add(totalPriceOfGoods.getValue())
                    , Currency.getInstance("VND")));

            // after discount
            order = applyDiscount(order, totalPriceOfGoods, fee);
            order = orderService.create(order);

            URI locationOfNewOrder = ucb.path("/api/orders/{id}")
                    .buildAndExpand(order.getId()).toUri();
            return ResponseEntity.created(locationOfNewOrder).build();
        } catch (GhtkCreateOrderFailedException ex) {
            CustomResponse customResponse = new CustomResponse();
            if (ex.getMessage().equals("")) {

            }
            customResponse.setMessage(ex.getMessage());
            customResponse.setStatus_code("422");
            customResponse.setSuccess("false");

            order.setStatus(Status.FAILED);
            orderService.create(order);

            return new ResponseEntity(customResponse, HttpStatus.UNPROCESSABLE_ENTITY);
        }
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

    @PutMapping(value = "/cancel")
    private ResponseEntity<CustomResponse> cancelOrder(@RequestParam("order_id") Long orderId, Principal principal) {
        CustomResponse customResponse = new CustomResponse();

        Status status = orderService.getStatus(orderId, principal.getName());
        if (status == null) {
            customResponse.setSuccess("false");
            customResponse.setMessage(new StringBuilder().append("Order with id = ").append(orderId)
                    .append(" not exist").toString());
            customResponse.setStatus_code("404");
            return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
        }
        if (status.equals(Status.SUCCESS)) {
            customResponse.setSuccess("false");
            customResponse.setMessage(new StringBuilder().append("Order with id ").append(orderId)
                    .append(" can't cancellable because order has delivered successfully").toString());
            customResponse.setStatus_code("422");
            return new ResponseEntity<>(customResponse, HttpStatus.UNPROCESSABLE_ENTITY);

        } else if (status.equals(Status.DELIVERY)) {
            customResponse.setSuccess("false");
            customResponse.setMessage(new StringBuilder().append("Order with id ").append(orderId)
                    .append(" can't cancellable because order is delivery processing").toString());
            customResponse.setStatus_code("422");
            return new ResponseEntity<>(customResponse, HttpStatus.UNPROCESSABLE_ENTITY);
        } else if (status.equals(Status.CANCEL)) {
            customResponse.setSuccess("false");
            customResponse.setMessage(new StringBuilder().append("Order with id ").append(orderId)
                    .append(" has cancelled before").toString());
            customResponse.setStatus_code("422");
            return new ResponseEntity<>(customResponse, HttpStatus.UNPROCESSABLE_ENTITY);
        } else if (status.equals(Status.PENDING)) {
            boolean isCancel = orderService.cancel(orderId);
            if (isCancel) {
                customResponse.setSuccess("true");
                customResponse.setMessage(new StringBuilder().append("Order with id ").append(orderId)
                        .append(" cancel success").toString());
                customResponse.setStatus_code("200");
                return ResponseEntity.ok(customResponse);
            }
            customResponse.setSuccess("false");
            customResponse.setMessage(new StringBuilder().append("Order with id ").append(orderId)
                    .append(" face unexpected error").toString());
            customResponse.setStatus_code("422");
            return new ResponseEntity<>(customResponse, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        else {
            customResponse.setSuccess("false");
            customResponse.setMessage(new StringBuilder().append("not yet supported with status order ")
                    .append(status.name()).toString());
            customResponse.setStatus_code("422");
            return new ResponseEntity<>(customResponse, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

}
