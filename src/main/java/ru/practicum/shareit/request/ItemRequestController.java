package ru.practicum.shareit.request;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestService requestService;

    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(
            @RequestBody ItemRequestDto requestDto,
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return requestService.createRequest(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllRequestsForUser(
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return requestService.getAllRequestsForUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return requestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return requestService.getRequestById(requestId);
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception ex) {
        return ex.getMessage();
    }
}
