package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemServiceImpl implements ItemService {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    public ItemServiceImpl(UserService userService, ItemRequestService itemRequestService) {
        this.userService = userService;
        this.itemRequestService = itemRequestService;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        User owner = UserMapper.toUser(userService.getUserById(ownerId));

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {

            ItemRequestDto requestDto = itemRequestService.getRequestById(itemDto.getRequestId());

            User requestor = UserMapper.toUser(userService.getUserById(requestDto.getRequestorId()));

            request = ItemRequestMapper.toItemRequest(requestDto, requestor);
        }

        Item item = ItemMapper.toItem(itemDto, owner, request);
        item.setId(idCounter.getAndIncrement());
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Item not found with id: " + itemId);
        }

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new ValidationException("User is not the owner of the item");
        }

        if (itemDto.getName() != null) existingItem.setName(itemDto.getName());
        if (itemDto.getDescription() != null) existingItem.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) existingItem.setAvailable(itemDto.getAvailable());

        return ItemMapper.toItemDto(existingItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) throw new NotFoundException("Item not found with id: " + itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText))
                )
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Item getItemEntityById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Item not found with id: " + itemId);
        }
        return item;
    }
}
