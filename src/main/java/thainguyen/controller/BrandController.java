package thainguyen.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.Brand;
import thainguyen.service.brand.BrandService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/brands", produces = "application/json")
@Slf4j
public class BrandController {

    private final BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<Brand> findById(@PathVariable Long id) {
        Optional<Brand> brandOptional = service.findById(id);
        return brandOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    private ResponseEntity<List<Brand>> findAll() {
        List<Brand> brands = service.findAll();
        if (brands.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brands);
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    private ResponseEntity<Void> createBrand(@RequestBody Brand brand, UriComponentsBuilder ucb) {
        Brand createdBrand = service.create(brand);
        URI locationOfNewBrand = ucb.path("/api/brands/{id}")
                .buildAndExpand(createdBrand.getId()).toUri();
        return ResponseEntity.created(locationOfNewBrand).build();
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Brand> putBrand(@PathVariable Long id, @RequestBody Brand brand) {
        Brand updatedBrand = service.updateByPut(id, brand);
        if (updatedBrand != null) {
            return ResponseEntity.ok(updatedBrand);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Brand> patchBrand(@PathVariable Long id, @RequestBody Brand brand) {
        Brand updatedBrand = service.updateByPatch(id, brand);
        if (updatedBrand != null) {
            return ResponseEntity.ok(updatedBrand);
        }
        return ResponseEntity.notFound().build();
    }

}
