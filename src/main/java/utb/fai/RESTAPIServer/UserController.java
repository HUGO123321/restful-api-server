package utb.fai.RESTAPIServer;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<MyUser>> getAllUsers() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MyUser>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        List<MyUser> result = repository.findAll().stream()
                .filter(user -> containsIgnoreCase(user.getName(), name))
                .filter(user -> containsIgnoreCase(user.getEmail(), email))
                .filter(user -> contains(user.getPhoneNumber(), phoneNumber))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MyUser> getUser(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MyUser> createUser(@RequestBody MyUser user) {
        if (user == null || !user.isUserDataValid()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            user.setId(null);
            MyUser saved = repository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MyUser> updateUser(@PathVariable Long id, @RequestBody MyUser input) {
        Optional<MyUser> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (input == null || !input.isUserDataValid()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            MyUser user = existing.get();
            user.setName(input.getName());
            user.setEmail(input.getEmail());
            user.setPhoneNumber(input.getPhoneNumber());
            return ResponseEntity.ok(repository.save(user));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MyUser> deleteUser(@PathVariable Long id) {
        Optional<MyUser> found = repository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.delete(found.get());
            return ResponseEntity.ok(found.get());
        } catch (Exception exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<List<MyUser>> deleteAllUsers() {
        try {
            repository.deleteAll();
            return ResponseEntity.ok(List.of());
        } catch (Exception exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean containsIgnoreCase(String value, String query) {
        return query == null || query.isBlank() ||
                (value != null && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)));
    }

    private boolean contains(String value, String query) {
        return query == null || query.isBlank() || (value != null && value.contains(query));
    }
}
