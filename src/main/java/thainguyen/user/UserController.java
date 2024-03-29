package thainguyen.user;

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
@RequestMapping(value = "/api/users", produces = "application/json")
@Slf4j
@AllArgsConstructor
public class UserController {

    private final UserService service;
    private final ValidateUtil validateUtil;
    private final ObjectMapperUtil objectMapperUtil;

    @GetMapping
    private ResponseEntity<ResponseComponent<List<User>>> findAll() {
        List<User> users = service.findAll();
        ResponseComponent<List<User>> response = ResponseComponent
                .<List<User>>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(users)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<ResponseComponent<User>> findById(@PathVariable  Long id) {
        User user = service.findById(id);
        ResponseComponent<User> response = ResponseComponent
                .<User>builder()
                .success(true)
                .status(HttpStatus.OK)
                .data(user)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<ResponseComponent<Void>> createUser(@RequestBody @Valid User user,
                                               UriComponentsBuilder ucb)
            throws SQLIntegrityConstraintViolationException {

        user = service.create(user);
        URI locationOfNewUser = ucb.path("/api/users/{id}").buildAndExpand(user.getId()).toUri();
        ResponseComponent<Void> response = ResponseComponent
                .<Void>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Create User success")
                .build();
        return ResponseEntity.created(locationOfNewUser).body(response);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<ResponseComponent<User>> patchProduct(@PathVariable Long id,
                                                                 @RequestBody Map<String, Object> userMap)
            throws MethodArgumentNotValidException {

        validateUtil.validate(userMap, User.class);
        User user = (User) objectMapperUtil.convertMapToEntity(userMap, User.class);
        User updatedUser = service.updateUser(id, user);
        ResponseComponent<User> response = ResponseComponent
                .<User>builder()
                .success(true)
                .status(HttpStatus.OK)
                .message("Update User success")
                .data(updatedUser)
                .build();
        return new ResponseEntity<>(response, response.getStatus());
    }

}