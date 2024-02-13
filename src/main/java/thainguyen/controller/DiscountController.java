package thainguyen.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.controller.custom.ResponseComponent;
import thainguyen.controller.exception.DiscountInvalidException;
import thainguyen.domain.Discount;
import thainguyen.service.discount.DiscountService;
import thainguyen.utilities.ObjectMapperUtil;
import thainguyen.utilities.ValidateUtil;

import java.net.URI;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/discounts", produces = "application/json")
@AllArgsConstructor
public class DiscountController {

    private final DiscountService discountService;
    private final ValidateUtil validateUtil;
    private final ObjectMapperUtil objectMapperUtil;

    @GetMapping
    private ResponseEntity<ResponseComponent<List<Discount>>> findAll() {
        List<Discount> discounts = discountService.findAll();
        ResponseComponent<List<Discount>> response = ResponseComponent
                .<List<Discount>>builder()
                .status(HttpStatus.OK)
                .success(true)
                .data(discounts)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping("/{id}")
    private ResponseEntity<ResponseComponent<Discount>> findById(@PathVariable Long id) {
        Discount discount = discountService.findById(id);
        ResponseComponent<Discount> response = ResponseComponent
                .<Discount>builder()
                .status(HttpStatus.OK)
                .success(true)
                .data(discount)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<ResponseComponent<Void>> createDiscount(@RequestBody @Valid Discount discount,
                                                                   UriComponentsBuilder ucb)
            throws SQLIntegrityConstraintViolationException, DiscountInvalidException {

        discountService.checkDiscount(discount);
        discount = discountService.create(discount);
        URI locationOfNew = ucb.path("/api/discounts/{id}")
                .buildAndExpand(discount.getId())
                .toUri();
        ResponseComponent<Void> response = ResponseComponent
                .<Void>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Create Discount success")
                .build();
        return ResponseEntity
                .created(locationOfNew)
                .body(response);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<ResponseComponent<Discount>> patchDiscount (@PathVariable Long id,
                                                                       @RequestBody Map<String, Object> discountMap)
            throws Exception {

        validateUtil.validate(discountMap, Discount.class);
        Discount discount = (Discount) objectMapperUtil.convertMapToEntity(discountMap, Discount.class);
        discount = discountService.updateDiscount(id, discount);
        ResponseComponent<Discount> response = ResponseComponent
                .<Discount>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Update discount success")
                .data(discount)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }



}
