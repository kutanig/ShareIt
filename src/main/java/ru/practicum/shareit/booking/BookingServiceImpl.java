package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final UserService userService;
    private final ItemService itemService;

    public BookingServiceImpl(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Long bookerId) {
        User booker = userService.getUserEntityById(bookerId);
        Item item = itemService.getItemEntityById(bookingDto.getItemId());

        // Проверка доступности вещи
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new UnavailableItemException("Item is not available for booking");
        }

        // Проверка, что владелец не бронирует свою вещь
        if (item.getOwner().getId().equals(bookerId)) {
            throw new SelfBookingException("Owner cannot book their own item");
        }

        // Проверка дат
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Invalid booking dates");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setId(idCounter.getAndIncrement());
        booking.setStatus(BookingStatus.WAITING);
        bookings.put(booking.getId(), booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new NotFoundException("Booking not found with id: " + bookingId);
        }

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("User is not the owner of the item");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking is not in waiting status");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new NotFoundException("Booking not found with id: " + bookingId);
        }

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("User not authorized to view this booking");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsForUser(Long userId, String state) {
        userService.getUserById(userId); // Проверка существования пользователя

        List<Booking> userBookings = bookings.values().stream()
                .filter(b -> b.getBooker().getId().equals(userId))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());

        return filterBookingsByState(userBookings, state);
    }

    @Override
    public List<BookingDto> getAllBookingsForOwner(Long ownerId, String state) {
        userService.getUserById(ownerId); // Проверка существования пользователя

        List<Booking> ownerBookings = bookings.values().stream()
                .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());

        return filterBookingsByState(ownerBookings, state);
    }

    private List<BookingDto> filterBookingsByState(List<Booking> bookings, String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }

        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(booking -> {
                    switch (bookingState) {
                        case ALL: return true;
                        case CURRENT:
                            return booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
                        case PAST:
                            return booking.getEnd().isBefore(now);
                        case FUTURE:
                            return booking.getStart().isAfter(now);
                        case WAITING:
                            return booking.getStatus() == BookingStatus.WAITING;
                        case REJECTED:
                            return booking.getStatus() == BookingStatus.REJECTED;
                        default: return true;
                    }
                })
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private enum BookingState {
        ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED
    }
}
