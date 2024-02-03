package thainguyen.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.controller.conf.ResponseComponent;
import thainguyen.domain.Brand;
import thainguyen.service.brand.BrandService;
import thainguyen.utilities.ObjectMapperUtil;
import thainguyen.utilities.ValidateUtil;

import java.net.URI;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/brands", produces = "application/json")
@Slf4j
@AllArgsConstructor
public class BrandController {

    private final BrandService service;
    private final ValidateUtil validateUtil;
    private final ObjectMapperUtil objectMapperUtil;


    @GetMapping(value = "/{id}")
    private ResponseEntity<ResponseComponent<Brand>> findById(@PathVariable Long id) {
        Optional<Brand> brandOptional = service.findById(id);
        ResponseComponent<Brand> response = new ResponseComponent<>();
        response.setSuccess(true);

        if (brandOptional.isEmpty()) {
            response.setData(null);
            response.setMessage("Invalid brand ID, brand not found");
        } else {
            response.setData(brandOptional.get());
        }
        return brandOptional.map(brand -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(response, HttpStatus.NOT_FOUND));
    }

    @GetMapping
    private ResponseEntity<ResponseComponent<List<Brand>>> findAll() {
        List<Brand> brands = service.findAll();
        ResponseComponent<List<Brand>> response = new ResponseComponent<>();
        response.setData(brands);
        response.setSuccess(true);
        if (brands.isEmpty()) {
            response.setMessage("Brand not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    private ResponseEntity<Void> createBrand(@RequestBody @Valid Brand brand, UriComponentsBuilder ucb)
            throws SQLIntegrityConstraintViolationException {

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
    private ResponseEntity<Brand> patchBrand(@PathVariable Long id, @RequestBody Map<String, Object> brandPatch)
            throws MethodArgumentNotValidException {

        validateUtil.validate(brandPatch, Brand.class);
        Brand brand = (Brand) objectMapperUtil.convertMapToEntity(brandPatch, Brand.class);

        Brand updatedBrand = service.updateByPatch(id, brand);
        if (updatedBrand != null) {
            return ResponseEntity.ok(updatedBrand);
        }
        return ResponseEntity.notFound().build();
    }

}
