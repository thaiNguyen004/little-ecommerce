package thainguyen.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.utility.core.ResponseComponent;
import thainguyen.discount.Discount;
import thainguyen.lineitem.LineItem;
import thainguyen.shipment.Shipment;
import thainguyen.valuetype.Status;
import thainguyen.shipment.ShipmentService;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/orders", produces = "application/json")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    @GetMapping(value = "/{id}")
    private ResponseEntity<ResponseComponent<Order>> findById(@PathVariable UUID id, Principal principal) {
        Order order = orderService.findByIdAndOwner(id, principal.getName());
        ResponseComponent<Order> response = ResponseComponent
                .<Order>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(order)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping
    private ResponseEntity<ResponseComponent<List<Order>>> findAll(Principal principal) {
        List<Order> orders = orderService.findByOwner(principal.getName());
        ResponseComponent<List<Order>> response = ResponseComponent
                .<List<Order>>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(orders)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<ResponseComponent<Void>> postEntity(@RequestBody @Valid Order order
            , UriComponentsBuilder ucb
            , Principal principal) throws JsonProcessingException {

        order = orderService.create(order, principal.getName());
        Integer totalPriceOfGoods = calcTotalPriceOfGoods(order.getLineItems());

        order.setTotalPriceBeforeDiscount(totalPriceOfGoods);

        try {
            Shipment shipment = shipmentService.createShipment(order);
            order.setShipment(shipment);
            order.setStatus(Status.PENDING);

            Integer fee = shipment.getFee();
            // calc total price include delivery fee to totalPriceBeforeDiscount field
            order.setTotalPriceBeforeDiscount(fee + totalPriceOfGoods);

            // after discount
            order = applyDiscount(order, totalPriceOfGoods, fee);
            order = orderService.create(order);

            URI locationOfNewOrder = ucb.path("/api/orders/{id}")
                    .buildAndExpand(order.getId()).toUri();
            ResponseComponent<Void> response = ResponseComponent
                    .<Void>builder()
                    .success(true)
                    .status(HttpStatus.CREATED)
                    .message("Create Order success")
                    .build();

            return ResponseEntity.created(locationOfNewOrder)
                    .body(response);
        } catch (GhtkCreateOrderFailedException ex) {
            log.error(ex.getMessage());
            ResponseComponent response = ResponseComponent
                    .<Void>builder()
                    .success(false)
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .message(ex.getMessage())
                    .build();
            return new ResponseEntity<>(response, response.getStatus());
        }
    }

    private Order applyDiscount(Order order, Integer totalPriceOfGoods, Integer feeDelivery) {
        // init total price
        int totalPrice = 0;

        List<Discount> discounts = order.getDiscounts();
        if (discounts == null || discounts.isEmpty()) {
            totalPrice = totalPriceOfGoods + feeDelivery;
        } else {
            for (Discount discount : discounts) {
                if (discount.getKind().equals(Discount.Kind.NORMAL)) {
                    if (discount.getType().equals(Discount.Type.AMOUNT)) {
                        totalPriceOfGoods = totalPriceOfGoods - discount.getValue();
                    } else {
                        totalPriceOfGoods = totalPriceOfGoods - (totalPriceOfGoods * discount.getValue() / 100);
                    }
                    // discount fee delivery
                } else {
                    if (discount.getType().equals(Discount.Type.AMOUNT)) {
                        feeDelivery = feeDelivery - discount.getValue();
                    } else {
                        feeDelivery = feeDelivery - (feeDelivery * discount.getValue() / 100);
                    }
                }
            }
        }
        totalPrice = totalPriceOfGoods + feeDelivery;
        order.setTotalPriceAfterDiscount(totalPrice);
        return order;
    }

    private Integer calcTotalPriceOfGoods(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty())
            return 0;
        return lineItems.stream().map(LineItem::getTotalPrice)
                .reduce(0, Integer::sum);
    }

    @PutMapping(value = "/cancel")
    private ResponseEntity<ResponseComponent<Void>> cancelOrder(@RequestParam("order_id") UUID orderId, Principal principal) {
        Status status = orderService.getStatus(orderId, principal.getName());
        if (status == null) {
            ResponseComponent<Void> response = ResponseComponent
                    .<Void>builder()
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .message(new StringBuilder().append("Order with id ").append(orderId)
                            .append(" not exist").toString())
                    .build();
            return new ResponseEntity<>(response, response.getStatus());
        }
        if (status.equals(Status.SUCCESS)) {
            ResponseComponent<Void> response = ResponseComponent
                    .<Void>builder()
                    .success(false)
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .message(new StringBuilder().append("Order with id ").append(orderId)
                            .append(" can't cancellable because order has delivered successfully").toString())
                    .build();
            return new ResponseEntity<>(response, response.getStatus());

        } else if (status.equals(Status.DELIVERY)) {
            ResponseComponent<Void> response = ResponseComponent
                    .<Void>builder()
                    .success(false)
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .message(new StringBuilder().append("Order with id ").append(orderId)
                            .append(" can't cancellable because order is delivery processing").toString())
                    .build();
            return new ResponseEntity<>(response, response.getStatus());
        } else if (status.equals(Status.CANCEL)) {
            ResponseComponent<Void> response = ResponseComponent
                    .<Void>builder()
                    .success(false)
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .message(new StringBuilder().append("Order with id ").append(orderId)
                            .append(" has cancelled before").toString())
                    .build();
            return new ResponseEntity<>(response, response.getStatus());
        } else if (status.equals(Status.PENDING)) {
            try {
                orderService.cancel(orderId);
                orderService.updateStatus(orderId, Status.CANCEL);
                ResponseComponent<Void> response = ResponseComponent
                        .<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK)
                        .message(new StringBuilder().append("Order with id ").append(orderId)
                                .append(" cancel success").toString())
                        .build();
                return new ResponseEntity<>(response, response.getStatus());
            } catch (GhtkCreateOrderFailedException ex) {
                ResponseComponent<Void> response = ResponseComponent
                        .<Void>builder()
                        .success(false)
                        .status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .message(ex.getMessage())
                        .build();
                return new ResponseEntity<>(response, response.getStatus());
            }
        } else {
            ResponseComponent<Void> response = ResponseComponent
                    .<Void>builder()
                    .success(false)
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .message(new StringBuilder().append("not yet supported with status order ")
                            .append(status.name()).toString())
                    .build();
            return new ResponseEntity<>(response, response.getStatus());
        }
    }

}
