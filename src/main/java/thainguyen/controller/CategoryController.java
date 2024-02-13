package thainguyen.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.controller.custom.ResponseComponent;
import thainguyen.domain.Category;
import thainguyen.service.category.CategoryService;
import thainguyen.utilities.ObjectMapperUtil;
import thainguyen.utilities.ValidateUtil;

import java.net.URI;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/categories", produces = "application/json")
@Slf4j
@AllArgsConstructor
public class CategoryController {

    private final CategoryService service;
    private final ValidateUtil validateUtil;
    private final ObjectMapperUtil objectMapperUtil;

    @GetMapping(value = "/{id}")
    private ResponseEntity<ResponseComponent<Category>> findById(@PathVariable Long id) {
        ResponseComponent<Category> response = ResponseComponent
                .<Category>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(service.findById(id))
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping
    private ResponseEntity<ResponseComponent<List<Category>>> findAll() {
        ResponseComponent<List<Category>> response = ResponseComponent.<List<Category>>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(service.findAll())
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    private ResponseEntity<ResponseComponent<Void>> createCategory(@RequestBody @Valid Category category
            , UriComponentsBuilder ucb) throws SQLIntegrityConstraintViolationException, IllegalArgumentException {

        Category createdCategory = service.create(category);
        URI locationOfNewCategory = ucb.path("/api/categories/{id}")
                .buildAndExpand(createdCategory.getId()).toUri();
        ResponseComponent<Void> response = ResponseComponent
                .<Void>builder()
                .success(true)
                .status(HttpStatus.CREATED)
                .message("Create category success")
                .build();
        return ResponseEntity.created(locationOfNewCategory)
                .body(response);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<ResponseComponent<Category>> patchCategory(@PathVariable Long id,
                                                                      @RequestBody Map<String, Object> categoryMap)
            throws MethodArgumentNotValidException {

        validateUtil.validate(categoryMap, Category.class);
        Category category = (Category) objectMapperUtil.convertMapToEntity(categoryMap, Category.class);

        category = service.updateCategory(id, category);
        ResponseComponent<Category> response = ResponseComponent
                .<Category>builder()
                .status(HttpStatus.OK)
                .message("Update category success")
                .data(category)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

}
