package thainguyen.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.controller.custom.ResponseComponent;
import thainguyen.domain.Product;
import thainguyen.service.brand.BrandService;
import thainguyen.service.category.CategoryService;
import thainguyen.service.product.ProductService;
import thainguyen.utilities.ObjectMapperUtil;
import thainguyen.utilities.ValidateUtil;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/products", produces = "application/json")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ValidateUtil validateUtil;
    private final ObjectMapperUtil objectMapperUtil;

    @GetMapping
    private ResponseEntity<ResponseComponent<List<Product>>> findAll() {
        List<Product> products = productService.findAll();
        ResponseComponent<List<Product>> response = ResponseComponent
                .<List<Product>>builder()
                .status(HttpStatus.OK)
                .success(true)
                .data(products)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<ResponseComponent<Product>> findById(@PathVariable Long id) {
        Product product = productService.findById(id);
        ResponseComponent<Product> response = ResponseComponent
                .<Product>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(product)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<ResponseComponent<Void>> createProduct(@RequestBody @Valid Product product,
                                               UriComponentsBuilder ucb) {

        Product productSaved = productService.create(product);

        URI locationOfNewProduct = ucb.path("/api/products/{id}")
                .buildAndExpand(productSaved.getId()).toUri();
        ResponseComponent<Void> response = ResponseComponent
                .<Void>builder()
                .success(true)
                .status(HttpStatus.CREATED)
                .message("Create Product success")
                .build();
        return ResponseEntity
                .created(locationOfNewProduct)
                .body(response);
    }


    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<ResponseComponent<Product>> patchProduct(@PathVariable Long id, @RequestBody Map<String, Object> productMap)
            throws MethodArgumentNotValidException {

        validateUtil.validate(productMap, Product.class);
        Product product = (Product) objectMapperUtil.convertMapToEntity(productMap, Product.class);

        Product productSaved = productService.updateProduct(id, product);
        ResponseComponent<Product> response = ResponseComponent
                .<Product>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Update Product success")
                .data(productSaved)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

}
