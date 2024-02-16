package thainguyen.detailproduct;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.utility.core.ResponseComponent;
import thainguyen.product.ProductService;
import thainguyen.size.SizeService;
import thainguyen.utility.mapping.ObjectMapperUtil;
import thainguyen.utility.validation.ValidateUtil;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/detailproducts", produces = "application/json")
@Slf4j
@AllArgsConstructor
public class DetailProductController {

    private final DetailProductService detailProductService;
    private final ProductService productService;
    private final SizeService sizeService;
    private final ValidateUtil validateUtil;
    private final ObjectMapperUtil objectMapperUtil;

    @GetMapping(value = "/{id}")
    private ResponseEntity<ResponseComponent<DetailProduct>> findById(@PathVariable Long id) {
        DetailProduct detailProduct = detailProductService.findById(id);
        ResponseComponent<DetailProduct> response = ResponseComponent
                .<DetailProduct>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(detailProduct)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping
    private ResponseEntity<ResponseComponent<List<DetailProduct>>> findAll() {
        List<DetailProduct> detailProducts = detailProductService.findAll();
        ResponseComponent<List<DetailProduct>> response = ResponseComponent
                .<List<DetailProduct>>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(detailProducts)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    private ResponseEntity<ResponseComponent<Void>> createDeatailProduct(@RequestBody @Valid DetailProduct detailProduct,
                                             UriComponentsBuilder ucb) {

        detailProduct = detailProductService.create(detailProduct);
        // chua check
        ResponseComponent<Void> response = ResponseComponent
                .<Void>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Create Detail Product success")
                .build();
        URI locationCreated = ucb.path("/api/detailproducts/{id}")
                .buildAndExpand(detailProduct.getId()).toUri();
        return ResponseEntity
                .created(locationCreated)
                .body(response);
    }


    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<ResponseComponent<DetailProduct>> updateDetailProduct(@PathVariable Long id,
                                                                                 @RequestBody Map<String , Object> detailProductMap)
                                                                        throws MethodArgumentNotValidException {
        validateUtil.validate(validateUtil, DetailProduct.class);
        DetailProduct detailProduct = (DetailProduct) objectMapperUtil.convertMapToEntity(detailProductMap, DetailProduct.class);
        detailProduct = detailProductService.updateDetailProduct(id, detailProduct);
        ResponseComponent<DetailProduct> response = ResponseComponent
                .<DetailProduct>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Create Detail Product success")
                .data(detailProduct)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

}
