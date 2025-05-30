package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public UserDto createUser(UserDto userDto) {
        // Проверка уникальности email
        if (isEmailExists(userDto.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + userDto.getEmail());
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        // Проверка уникальности нового email
        if (userDto.getEmail() != null &&
                !userDto.getEmail().equals(existingUser.getEmail()) &&
                isEmailExists(userDto.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + userDto.getEmail());
        }

        if (userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getEmail() != null) existingUser.setEmail(userDto.getEmail());

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) throw new NotFoundException("User not found with id: " + userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        users.remove(userId);
    }

    private boolean isEmailExists(String email) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equals(email));
    }

    @Override
    public User getUserEntityById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        return user;
    }
}
