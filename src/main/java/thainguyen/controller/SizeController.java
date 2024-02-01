package thainguyen.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.Brand;
import thainguyen.domain.Category;
import thainguyen.domain.Size;
import thainguyen.service.brand.BrandService;
import thainguyen.service.category.CategoryService;
import thainguyen.service.size.SizeService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/sizes", produces = "application/json")
@Slf4j
public class SizeController {

    private final SizeService sizeService;
    private final BrandService brandService;
    private final CategoryService categoryService;

    public SizeController(SizeService sizeService, BrandService brandService,
                            CategoryService categoryService) {
        this.sizeService = sizeService;
        this.brandService = brandService;
        this.categoryService = categoryService;
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<Size> findById(@PathVariable Long id) {
        Optional<Size> sizeOptional = sizeService.findById(id);
        return sizeOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    private ResponseEntity<List<Size>> findAll() {
        List<Size> sizes = sizeService.findAll();
        if (sizes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sizes);
    }

    @PostMapping
    private ResponseEntity<Void> createSize(@RequestBody Size size, UriComponentsBuilder ucb) {
        if (size.getCategory() == null || size.getBrand() == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        size = givingCategoryAndBrand(size);
        if (size == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        size = sizeService.create(size);
        if (size == null) {
            return ResponseEntity.notFound().build();
        }
        URI locationCreated = ucb.path("/api/sizes/{id}")
                .buildAndExpand(size.getId()).toUri();
        return ResponseEntity.created(locationCreated).build();
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Size> putSize(@PathVariable Long id, @RequestBody Size size) {
        if (size.getBrand() == null || size.getCategory() == null)
            return ResponseEntity.badRequest().build();

        size = givingCategoryAndBrand(size);
        if (size == null) {
            return ResponseEntity.unprocessableEntity().build();
        }
        size = sizeService.updateByPut(id, size);
        if (size == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(size);
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Size> patchSize(@PathVariable Long id, @RequestBody Size size) {
        size = givingCategoryAndBrand(size);
        if (size == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        size = sizeService.updateByPatch(id, size);
        if (size == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(size);
    }

    public Size givingCategoryAndBrand(Size size) {
        if (size.getBrand() != null && size.getCategory() != null ) {
            Optional<Brand> brandOpt = brandService.findById(size.getBrand().getId());
            Optional<Category> categoryOpt = categoryService.findById(size.getCategory().getId());
            // non consistency
            if (brandOpt.isEmpty() || categoryOpt.isEmpty()) {
                return null;
            }
            size.setBrand(brandOpt.get());
            size.setCategory(categoryOpt.get());
        }
        else if (size.getBrand() != null ) {
            Optional<Brand> brandOfProduct = brandService.findById(size.getBrand().getId());
            if (brandOfProduct.isEmpty()) {
                return null;
            } else {
                size.setBrand(brandOfProduct.get());
            }
        } else if (size.getCategory() != null ) {
            Optional<Category> categoryOfProduct = categoryService.findById(size.getCategory().getId());
            if (categoryOfProduct.isEmpty()) {
                return null;
            } else {
                size.setCategory(categoryOfProduct.get());
            }
        }
        return size;
    }
}
