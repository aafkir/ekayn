package com.aafkir.tifssi.auth.api;

import com.aafkir.tifssi.auth.api.AuthRequests.*;
import com.aafkir.tifssi.auth.application.UserAccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/users") @Tag(name="Users")
public class UserAccountController {
    private final UserAccountService service; public UserAccountController(UserAccountService service){this.service=service;}
    @GetMapping public List<UserAccountResponse> all(){return service.findAll();}
    @GetMapping("/{id}") public UserAccountResponse get(@PathVariable Long id){return service.toResponse(service.get(id));}
    @PostMapping public ResponseEntity<UserAccountResponse> create(@Valid @RequestBody CreateUserRequest r){var u=service.create(r);return ResponseEntity.status(HttpStatus.CREATED).body(service.toResponse(u));}
    @PatchMapping("/{id}") public UserAccountResponse patch(@PathVariable Long id,@RequestBody PatchUserRequest r){return service.toResponse(service.patch(id,r));}
    @PatchMapping("/{id}/enabled") public UserAccountResponse enabled(@PathVariable Long id,@Valid @RequestBody EnabledRequest r){return service.toResponse(service.setEnabled(id,r.enabled()));}
    @PatchMapping("/{id}/password") @ResponseStatus(HttpStatus.NO_CONTENT) public void password(@PathVariable Long id,@Valid @RequestBody PasswordRequest r){service.resetPassword(id,r.password());}
}
