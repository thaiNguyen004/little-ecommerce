package thainguyen.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.Category;
import thainguyen.service.category.CategoryService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/categories", produces = "application/json")
@Slf4j
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<Category> findById(@PathVariable Long id) {
        Optional<Category> categoryOptional = service.findById(id);
        return categoryOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    private ResponseEntity<List<Category>> findAll() {
        List<Category> Categorys = service.findAll();
        if (Categorys.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Categorys);
    }

    @PostMapping(consumes = "application/json")
    @ResponseBody
    private ResponseEntity<Void> createCategory(@RequestBody Category category
            , UriComponentsBuilder ucb) {

        Category parent = category.getParent();
        if (parent != null) {
            Optional<Category> parentOpt = service.findById(parent.getId());
            parentOpt.ifPresent(category::setParent);

            if (parentOpt.isEmpty()) {
                return ResponseEntity.unprocessableEntity().build();
            }
        }
        Category createdCategory = service.create(category);
        URI locationOfNewCategory = ucb.path("/api/categories/{id}")
                .buildAndExpand(createdCategory.getId()).toUri();
        return ResponseEntity.created(locationOfNewCategory).build();
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Category> putCategory(@PathVariable Long id,
                                                 @RequestBody Category category) {
        category = getParent(category);
        if (category == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        Category updatedCategory = service.updateByPut(id, category);
        if (updatedCategory != null) {
            return ResponseEntity.ok(updatedCategory);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Category> patchCategory(@PathVariable Long id, @RequestBody Category category) {

        category = getParent(category);
        if (category == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        Category updatedCategory = service.updateByPatch(id, category);
        if (updatedCategory != null) {
            return ResponseEntity.ok(updatedCategory);
        }
        return ResponseEntity.notFound().build();
    }

    private Category getParent(Category category) {
        if (category.getParent() != null) {
            Optional<Category> categoryOpt = service.findById(category.getParent().getId());
            if (categoryOpt.isPresent()) {
                category.setParent(categoryOpt.get());
            }
            else {
                return null;
            }
        }
        return category;
    }
}
