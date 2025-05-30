package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final UserService userService;

    public ItemRequestServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemRequestDto createRequest(ItemRequestDto requestDto, Long userId) {
        User requestor = UserMapper.toUser(userService.getUserById(userId));

        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, requestor);
        request.setId(idCounter.getAndIncrement());
        request.setCreated(LocalDateTime.now());
        requests.put(request.getId(), request);

        return ItemRequestMapper.toItemRequestDto(request);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId) {
        ItemRequest request = requests.get(requestId);
        if (request == null) throw new NotFoundException("Request not found with id: " + requestId);
        return ItemRequestMapper.toItemRequestDto(request);
    }

    @Override
    public List<ItemRequestDto> getAllRequestsForUser(Long userId) {
        userService.getUserById(userId); // Проверка существования пользователя

        return requests.values().stream()
                .filter(r -> r.getRequestor().getId().equals(userId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Invalid pagination parameters");
        }

        return requests.values().stream()
                .filter(r -> !r.getRequestor().getId().equals(userId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .skip(from)
                .limit(size)
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }
}
