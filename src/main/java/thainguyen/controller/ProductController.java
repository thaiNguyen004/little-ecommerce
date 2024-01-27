package thainguyen.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.Brand;
import thainguyen.domain.Category;
import thainguyen.domain.Product;
import thainguyen.service.brand.BrandService;
import thainguyen.service.category.CategoryService;
import thainguyen.service.product.ProductService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/products", produces = "application/json")

public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ModelMapper modelMapper;

    public ProductController(ProductService productService, CategoryService categoryService
            , BrandService brandService, ModelMapper modelMapper) {

        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    private ResponseEntity<List<Product>> findAll() {
        List<Product> products = productService.findAll();
        if (products.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<Product> findById(@PathVariable  Long id) {
        return productService.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<Void> createProduct(@RequestBody Product product,
                                               UriComponentsBuilder ucb) {
        if (product.getBrand() == null || product.getCategory() == null) {
            // update body later
            return ResponseEntity.badRequest().build();
        }
        Optional<Brand> brandOfProduct = brandService.findById(product.getBrand().getId());
        Optional<Category> categoryProduct = categoryService.findById(product.getCategory().getId());
        if (brandOfProduct.isPresent() && categoryProduct.isPresent()) {
            product.setBrand(brandOfProduct.get());
            product.setCategory(categoryProduct.get());
            product = productService.create(product);
            URI location = ucb.path("/api/products/{id}")
                    .buildAndExpand(product.getId()).toUri();
            return ResponseEntity.created(location).build();
        }
        else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Product> putProduct(@PathVariable Long id,
                                               @RequestBody Product product) {

        // Logic hiện tại là put sẽ làm null các thuộc tính quan trọng
        // Ví dụ put mà lại không truyền id brand hoặc id cateogory thì logic sai nhưng không có lỗi
        // vì hiện tại updateCategoryAndBrand() chỉ check lỗi ở phần isEmpty
        if (product.getBrand() == null || product.getCategory() == null) {
            // update body later
            return ResponseEntity.badRequest().build();
        }

        product = updateCategoryAndBrand(product);
        if (product == null) {
            return ResponseEntity.unprocessableEntity().build();
        }
        Product updatedCategory = productService.updateByPut(id, product);
        if (updatedCategory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<Product> patchProduct(@PathVariable Long id, @RequestBody Product product) {
        product = updateCategoryAndBrand(product);
        if (product == null) {
            return ResponseEntity.unprocessableEntity().build();
        }
        Product productSaved = productService.updateByPatch(id, product);
        if (productSaved == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productSaved);
    }

    public Product updateCategoryAndBrand(Product product) {
        if (product.getBrand() != null && product.getCategory() != null ) {
            Optional<Brand> brandOpt = brandService.findById(product.getBrand().getId());
            Optional<Category> categoryOpt = categoryService.findById(product.getCategory().getId());
            // non consistency
            if (brandOpt.isEmpty() || categoryOpt.isEmpty()) {
                return null;
            }
            product.setBrand(brandOpt.get());
            product.setCategory(categoryOpt.get());
        }
        else if (product.getBrand() != null ) {
            Optional<Brand> brandOfProduct = brandService.findById(product.getBrand().getId());
            if (brandOfProduct.isEmpty()) {
                return null;
            } else {
                product.setBrand(brandOfProduct.get());
            }
        } else if (product.getCategory() != null ) {
            Optional<Category> categoryOfProduct = categoryService.findById(product.getCategory().getId());
            if (categoryOfProduct.isEmpty()) {
                return null;
            } else {
                product.setCategory(categoryOfProduct.get());
            }
        }

        return product;
    }
}
