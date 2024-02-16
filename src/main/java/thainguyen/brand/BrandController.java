package thainguyen.brand;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.utility.core.ResponseComponent;
import thainguyen.utility.mapping.ObjectMapperUtil;
import thainguyen.utility.validation.ValidateUtil;

import java.net.URI;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

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
        ResponseComponent<Brand> response = ResponseComponent
                .<Brand>builder().status(HttpStatus.OK)
                .success(true)
                .status(HttpStatus.OK)
                .data(service.findById(id))
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping
    private ResponseEntity<ResponseComponent<List<Brand>>> findAll() {
        List<Brand> brands = service.findAll();
        ResponseComponent<List<Brand>> response = ResponseComponent
                .<List<Brand>>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(brands)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    private ResponseEntity<ResponseComponent<Void>> createBrand(@RequestBody @Valid Brand brand, UriComponentsBuilder ucb)
            throws SQLIntegrityConstraintViolationException {

        Brand createdBrand = service.create(brand);
        URI locationOfNewBrand = ucb.path("/api/brands/{id}")
                .buildAndExpand(createdBrand.getId()).toUri();
        ResponseComponent<Void> response = ResponseComponent
                .<Void>builder()
                .success(true)
                .status(HttpStatus.CREATED)
                .message("Create brand success")
                .build();
        return ResponseEntity
                .created(locationOfNewBrand)
                .body(response);
    }


    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<ResponseComponent<Brand>> updateBrand(@PathVariable Long id,
                                                                 @RequestBody Map<String, Object> brandPatch)
            throws MethodArgumentNotValidException {

        System.out.println("udpate " + brandPatch);

        validateUtil.validate(brandPatch, Brand.class);
        Brand brand = (Brand) objectMapperUtil.convertMapToEntity(brandPatch, Brand.class);

        Brand updatedBrand = service.updateBrand(id, brand);
        ResponseComponent<Brand> response = ResponseComponent
                .<Brand>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Update brand success")
                .data(updatedBrand)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

}
