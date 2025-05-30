package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(BookingDto bookingDto, Long bookerId);

    BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getAllBookingsForUser(Long userId, String state);

    List<BookingDto> getAllBookingsForOwner(Long ownerId, String state);
}
