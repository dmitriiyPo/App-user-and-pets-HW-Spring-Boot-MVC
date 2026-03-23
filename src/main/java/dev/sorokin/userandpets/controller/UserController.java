package dev.sorokin.userandpets.controller;

import dev.sorokin.userandpets.converter.PetDtoConverter;
import dev.sorokin.userandpets.converter.UserDtoConverter;
import dev.sorokin.userandpets.dto.UserDto;
import dev.sorokin.userandpets.model.User;
import dev.sorokin.userandpets.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDtoConverter userDtoConverter;
    private final PetDtoConverter petDtoConverter;

    @Autowired
    public UserController(
            UserService userService,
            UserDtoConverter userDtoConverter,
            PetDtoConverter petDtoConverter
    ) {
        this.userService = userService;
        this.userDtoConverter = userDtoConverter;
        this.petDtoConverter = petDtoConverter;
    }


    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto userDto) {
        User newUser = userService.createUser(userDtoConverter.toUser(userDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(userDtoConverter.toDto(newUser));
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("id") Long id, @RequestBody @Valid UserDto userDto) {
        User userToUpdate = new User(id, userDto.name(), userDto.email(), userDto.age(),
                userDto.pets().stream().map(petDtoConverter::toPet).toList());
        User updateUser = userService.updateUser(userToUpdate);
        return ResponseEntity.ok(userDtoConverter.toDto(updateUser));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findUserById(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return ResponseEntity.status(HttpStatus.FOUND).body(userDtoConverter.toDto(user));
    }


    @GetMapping
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(value = "name",required = false) String name,
            @RequestParam(value = "age",required = false) Integer age
    ) {
        List<User> users = userService.searchUsers(name, age);
        return ResponseEntity.status(HttpStatus.FOUND).body(users);
    }

}
