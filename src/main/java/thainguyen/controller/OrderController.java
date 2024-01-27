package thainguyen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.Order;
import thainguyen.domain.Shipment;
import thainguyen.dto.order.OrderDto;
import thainguyen.dto.order.OrderMapper;
import thainguyen.service.order.OrderService;
import thainguyen.service.shipment.ShipmentService;

import java.net.URI;
import java.security.Principal;
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
            , Principal principal) {
        Order order = orderMapper.convertOrderDtoToEntity(orderDto);
        Order orderSaved = orderService.create(order, principal.getName());
        Shipment shipment = shipmentService.createShipmentFromGhtk(orderSaved);
        if (shipment == null) return ResponseEntity.notFound().build();
        order.setShipment(shipment);
        Order savedOrder = orderService.create(order);
        System.out.println("order.getId() " + order.getId());
        System.out.println("getTotalPrice " + order.getTotalPrice());
        System.out.println("getDiscounts 1 " + order.getDiscounts().get(0).getCode());
        System.out.println("getDiscounts 2 " + order.getDiscounts().get(1).getCode());
        System.out.println("order.getAddress().getProvince() " + order.getAddress().getProvince());
        System.out.println("order.getLineItems().get(0).getTotalPrice() " + order.getLineItems().get(0).getTotalPrice());
        URI locationOfNewOrder = ucb.path("/api/orders/{id}")
                .buildAndExpand(savedOrder.getId()).toUri();
        return ResponseEntity.created(locationOfNewOrder).build();
    }

}
