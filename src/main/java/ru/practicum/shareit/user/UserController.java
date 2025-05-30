package ru.practicum.shareit.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("POST /users - Creating new user: {}", userDto.getEmail());
        UserDto createdUser = userService.createUser(userDto);
        log.debug("Created user: ID={}, Email={}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(
            @PathVariable Long userId,
            @RequestBody UserDto userDto
    ) {
        log.info("PATCH /users/{} - Updating user", userId);
        UserDto updatedUser = userService.updateUser(userId, userDto);
        log.debug("Updated user: ID={}", userId);
        return updatedUser;
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("GET /users/{} - Fetching user", userId);
        UserDto user = userService.getUserById(userId);
        log.debug("Fetched user: ID={}, Email={}", user.getId(), user.getEmail());
        return user;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("GET /users - Fetching all users");
        List<UserDto> users = userService.getAllUsers();
        log.debug("Fetched {} users", users.size());
        return users;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /users/{} - Deleting user", userId);
        userService.deleteUser(userId);
        log.debug("Deleted user: ID={}", userId);
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex) {
        log.warn("Not found error: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler({DuplicateEmailException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternalError(Exception ex) {
        log.error("Internal server error", ex);
        return "Internal server error";
    }
}
