package si.banka.korisnicki_servis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.korisnicki_servis.controller.response_forms.ChangePasswordForm;
import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.controller.response_forms.ResetPasswordForm;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(value = "UserControllerAPI", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    @ApiOperation("Gets all users")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<List<User>>getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/user/create")
    @ApiOperation("Create user with username, ime, prezime, email, jmbg, br_telefona, password, aktivan, pozicija")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<User>createUser(@RequestBody CreateUserForm createUserForm) {
        return ResponseEntity.ok().body(userService.createUser(createUserForm));
    }

    @DeleteMapping("/user/delete/{id}")
    @ApiOperation("Delete user with specific id")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<?>deleteUser(@PathVariable long id) {
        Optional<User> user= userService.getUserById(id);
        if(user.get() == null){ResponseEntity.badRequest().build();}
        userService.deleteUser(user.get());
        return ResponseEntity.ok().body(user.get().getUsername() + " disabled");
    }

    @PostMapping("/user/edit/{id}")
    @ApiOperation("Edit user with specific id,text fields are with existing data, the user can change them")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<?>editUser(@PathVariable long id, @RequestHeader("Authorization") String token) {
        String username = userService.getUserById(id).get().getUsername();
        if(username.equalsIgnoreCase("admin")) return ResponseEntity.badRequest().build();

        userService.editUser(userService.getUser(username), token.substring("Bearer ".length()));
        return ResponseEntity.ok().body(username + " edited");
    }

    @PostMapping("/user/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordForm resetPasswordForm){
        if(!userService.resetPassword(resetPasswordForm.getEmail())){
            return ResponseEntity.badRequest().body("Mail failed to send");
        }
        return ResponseEntity.ok().body("Mail send to: " + resetPasswordForm.getEmail());
    }

    @PostMapping("/user/change-password/{token}")
    public ResponseEntity<?> changePassword(@PathVariable String token, @RequestBody ChangePasswordForm changePasswordForm){
        if(!userService.setNewPassword(changePasswordForm.getNewPassword(), token)){
            return ResponseEntity.badRequest().body("Invalid token!");
        }
        return ResponseEntity.ok().body("New password!");
    }

}

