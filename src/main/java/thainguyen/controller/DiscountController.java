package thainguyen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.Discount;
import thainguyen.service.discount.DiscountService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/discounts", produces = "application/json")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    private ResponseEntity<List<Discount>> findAll() {
        List<Discount> discounts = discountService.findAll();
        if (discounts.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/{id}")
    private ResponseEntity<Discount> findById(@PathVariable Long id) {
        Optional<Discount> discountOpt = discountService.findById(id);
        return discountOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<Void> createDiscount(@RequestBody Discount discount, UriComponentsBuilder ucb) {
        if (discount.getType() == null
                || discount.getCode() == null
                || discount.getValue() == null
                || discount.getQuantity() == null
                || discount.getStart() == null
                || discount.getEnd() == null
                || discount.getKind() == null ) {
            return ResponseEntity.badRequest().build();
        }
        discount = validateDiscount(discount);
        if (discount == null) return ResponseEntity.unprocessableEntity().build();
        discount = discountService.create(discount);
        URI locationOfNew = ucb.path("/api/discounts/{id}")
                .buildAndExpand(discount.getId())
                .toUri();
        return ResponseEntity.created(locationOfNew).build();
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Discount> putDiscount(@PathVariable Long id, @RequestBody Discount discount) {
        if (discount.getType() == null
                || discount.getCode() == null
                || discount.getValue() == null
                || discount.getQuantity() == null
                || discount.getStart() == null
                || discount.getEnd() == null
                || discount.getKind() == null ) {
            return ResponseEntity.badRequest().build();
        }
        discount = validateDiscount(discount);
        if (discount == null) return ResponseEntity.unprocessableEntity().build();
        discount = discountService.updateByPut(id, discount);
        if (discount == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(discount);
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Discount> patchDiscount (@PathVariable Long id, @RequestBody Discount discount) {
        if (discount.getType() != null && discount.getValue() != null) {
            discount = validateDiscount(discount);
            if (discount == null) return ResponseEntity.unprocessableEntity().build();
        }
        discount = discountService.updateByPatch(id, discount);
        if (discount == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(discount);
    }


    private Discount validateDiscount(Discount discount){
        Discount.Type type = discount.getType();
        int value = discount.getValue();
        if (type.equals(Discount.Type.PERCENTAGE) && (value > 100 || value < 1)) {
            return null;
        }
        if (type.equals(Discount.Type.AMOUNT) && (value < 1)) {
            return null;
        }
        return discount;
    }

}
