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
import thainguyen.domain.Size;
import thainguyen.service.size.SizeService;
import thainguyen.utilities.ObjectMapperUtil;
import thainguyen.utilities.ValidateUtil;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/sizes", produces = "application/json")
@Slf4j
@AllArgsConstructor
public class SizeController {

    private final SizeService sizeService;
    private final ValidateUtil validateUtil;
    private final ObjectMapperUtil objectMapperUtil;

    @GetMapping(value = "/{id}")
    private ResponseEntity<ResponseComponent<Size>> findById(@PathVariable Long id) {
        Size size = sizeService.findById(id);
        ResponseComponent<Size> response = ResponseComponent
                .<Size>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(size).build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping
    private ResponseEntity<ResponseComponent<List<Size>>> findAll() {
        List<Size> sizes = sizeService.findAll();
        ResponseComponent<List<Size>> response = ResponseComponent
                .<List<Size>>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(sizes).build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping
    private ResponseEntity<ResponseComponent<Void>> createSize(@RequestBody @Valid Size size,
                                                               UriComponentsBuilder ucb) {
        size = sizeService.create(size);
        URI locationCreated = ucb.path("/api/sizes/{id}")
                .buildAndExpand(size.getId()).toUri();
        ResponseComponent<Void> response = ResponseComponent
                .<Void>builder()
                .success(true)
                .status(HttpStatus.CREATED)
                .message("Create Size success")
                .build();
        return ResponseEntity
                .created(locationCreated)
                .body(response);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<ResponseComponent<Size>> putSize(@PathVariable Long id,
                                                            @RequestBody Map<String, Object> sizeMap)
            throws MethodArgumentNotValidException {

        validateUtil.validate(sizeMap, Size.class);
        Size size = (Size) objectMapperUtil.convertMapToEntity(sizeMap, Size.class);
        size = sizeService.updateSize(id, size);
        ResponseComponent<Size> response = ResponseComponent
                .<Size>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Update Size success")
                .data(size)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

}
