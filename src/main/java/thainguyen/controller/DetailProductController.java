package thainguyen.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.*;
import thainguyen.service.detailproduct.DetailProductService;
import thainguyen.service.product.ProductService;
import thainguyen.service.size.SizeService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/detailproducts", produces = "application/json")
@Slf4j
public class DetailProductController {

    private final DetailProductService detailProductService;
    private final ProductService productService;
    private final SizeService sizeService;

    public DetailProductController(DetailProductService detailProductService,
                                   ProductService productService,
                                   SizeService sizeService) {
        this.detailProductService = detailProductService;
        this.productService = productService;
        this.sizeService = sizeService;
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<DetailProduct> findById(@PathVariable Long id) {
        Optional<DetailProduct> dpOpt = detailProductService.findById(id);
        return dpOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    private ResponseEntity<List<DetailProduct>> findAll() {
        List<DetailProduct> detailProducts = detailProductService.findAll();
        if (detailProducts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detailProducts);
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    private ResponseEntity<Void> createDeatailProduct(@RequestBody DetailProduct detailProduct,
                                             UriComponentsBuilder ucb) {
        if (detailProduct.getSize() == null || detailProduct.getProduct() == null) {
            return ResponseEntity.badRequest().build();
        }

        detailProduct = givingProductAndSizeToDetailProduct(detailProduct);
        if (detailProduct == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        detailProduct = detailProductService.create(detailProduct);
        if (detailProduct == null) {
            return ResponseEntity.notFound().build();
        }
        URI locationCreated = ucb.path("/api/detailproducts/{id}")
                .buildAndExpand(detailProduct.getId()).toUri();
        return ResponseEntity.created(locationCreated).build();
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<DetailProduct> putSize(@PathVariable Long id, @RequestBody DetailProduct detailProduct) {
        if (detailProduct.getSize() == null || detailProduct.getProduct() == null) {
            return ResponseEntity.badRequest().build();
        }

        detailProduct = givingProductAndSizeToDetailProduct(detailProduct);
        if (detailProduct == null) {
            return ResponseEntity.unprocessableEntity().build();
        }
        detailProduct = detailProductService.updateByPut(id, detailProduct);
        if (detailProduct == null) {
            return ResponseEntity.notFound().build();
        }
        System.out.println(detailProduct);

        return ResponseEntity.ok(detailProduct);
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<DetailProduct> patchSize(@PathVariable Long id, @RequestBody DetailProduct detailProduct) {

        detailProduct = givingProductAndSizeToDetailProduct(detailProduct);

        if (detailProduct == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        detailProduct = detailProductService.updateByPatch(id, detailProduct);
        if (detailProduct == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detailProduct);
    }

    public DetailProduct givingProductAndSizeToDetailProduct(DetailProduct detailProduct) {
        if (detailProduct.getProduct() != null && detailProduct.getSize() != null ) {
            Optional<Product> productOpt = productService.findById(detailProduct.getProduct().getId());
            Optional<Size> sizeOpt = sizeService.findById(detailProduct.getSize().getId());
            // non consistency
            if (productOpt.isEmpty() || sizeOpt.isEmpty()) {
                return null;
            }
            detailProduct.setProduct(productOpt.get());
            detailProduct.setSize(sizeOpt.get());
        }
        else if (detailProduct.getProduct() != null ) {
            Optional<Product> productOpt = productService.findById(detailProduct.getProduct().getId());
            if (productOpt.isEmpty()) {
                return null;
            } else {
                detailProduct.setProduct(productOpt.get());
            }
        } else if (detailProduct.getSize() != null ) {
            Optional<Size> sizeOpt = sizeService.findById(detailProduct.getSize().getId());
            if (sizeOpt.isEmpty()) {
                return null;
            } else {
                detailProduct.setSize(sizeOpt.get());
            }
        }
        return detailProduct;
    }
}
