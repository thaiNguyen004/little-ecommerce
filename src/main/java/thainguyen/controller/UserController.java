package thainguyen.controller;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import thainguyen.domain.User;
import thainguyen.service.user.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/users", produces = "application/json")
@Slf4j
public class UserController {

    private UserService service;
    private final ModelMapper modelMapper;

    public UserController(UserService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    private ResponseEntity<List<User>> findAll() {
        List<User> users = service.findAll();
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<User> findById(@PathVariable  Long id) {
        return service.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = "application/json")
    private ResponseEntity<Void> createUser(@RequestBody User user,
                                               UriComponentsBuilder ucb) {
        user = service.create(user);
        URI location = ucb.path("/api/users/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<User> putProduct(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = service.updateByPut(id, user);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    private ResponseEntity<User> patchProduct(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = service.updateByPatch(id, user);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

}
