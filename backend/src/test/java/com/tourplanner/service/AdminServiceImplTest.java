package com.tourplanner.service;

import com.tourplanner.dto.AdminProfileDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.exception.BookingIdNotFoundException;
import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.main.MainApplication;
import com.tourplanner.model.Booking;
import com.tourplanner.model.Profile;
import com.tourplanner.model.User;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.ProfileRepository;
import com.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MainApplication.class)
public class AdminServiceImplTest {
    @Autowired
    private AdminService adminService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private BookingRepository bookingRepository;

    @Test
    void testGetCurrentAdminProfileSuccess() {
        String email = "admin@gmail.com";

        User user = new User();
        user.setUserId(1L);
        user.setEmail(email);
        user.setUserType("ADMIN");
        user.setCreatedAt(LocalDateTime.of(2025, 11, 7, 10, 0));

        Profile profile = new Profile();
        profile.setUserId(1L);
        profile.setFirstName("Ashish");
        profile.setLastName("Shenoy");
        profile.setAadharNumber("123456789012");
        profile.setCity("Mangalore");
        profile.setPhoneNumber("9876543210");

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Mono.just(profile));

        Mono<AdminProfileDTO> result = adminService.getCurrentAdminProfile(email);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals(1L, dto.getUserId());
                    assertEquals("admin@gmail.com", dto.getEmail());
                    assertEquals("ADMIN", dto.getUserType());
                    assertEquals(LocalDateTime.of(2025, 11, 7, 10, 0), dto.getCreatedAt());
                    assertEquals("Ashish", dto.getFirstName());
                    assertEquals("Shenoy", dto.getLastName());
                    assertEquals("123456789012", dto.getAadharNumber());
                    assertEquals("Mangalore", dto.getCity());
                    assertEquals("9876543210", dto.getPhoneNumber());
                })
                .verifyComplete();

        verify(userRepository).findByEmail(email);
        verify(profileRepository).findByUserId(1L);
    }

    @Test
    void testGetCurrentAdminProfileFail() {
        String email = "unknown@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Mono.empty());

        Mono<AdminProfileDTO> result = adminService.getCurrentAdminProfile(email);

        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof UserNotFoundException &&
                                err.getMessage().equals("User not found with email: " + email)
                )
                .verify();

        verify(userRepository).findByEmail(email);
    }

    @Test
    void testGetUserByIdSuccess() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@gmail.com");
        user.setUserType("USER");
        user.setCreatedAt(LocalDateTime.of(2025, 11, 7, 12, 0));

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFirstName("Ashish");
        profile.setLastName("Shenoy");
        profile.setAadharNumber("123456789012");
        profile.setCity("Mangalore");
        profile.setPhoneNumber("9876543210");

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Mono.just(profile));

        Mono<AdminProfileDTO> result = adminService.getUserById(userId);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals(1L, dto.getUserId());
                    assertEquals("user@gmail.com", dto.getEmail());
                    assertEquals("USER", dto.getUserType());
                    assertEquals(LocalDateTime.of(2025, 11, 7, 12, 0), dto.getCreatedAt());
                    assertEquals("Ashish", dto.getFirstName());
                    assertEquals("Shenoy", dto.getLastName());
                    assertEquals("123456789012", dto.getAadharNumber());
                    assertEquals("Mangalore", dto.getCity());
                    assertEquals("9876543210", dto.getPhoneNumber());
                })
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(profileRepository).findByUserId(userId);
    }

    @Test
    void testGetUserByIdUserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        Mono<AdminProfileDTO> result = adminService.getUserById(userId);

        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof UserNotFoundException &&
                                err.getMessage().equals("User not found with id: " + userId)
                )
                .verify();

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetUserByIdIsAdmin() {
        Long userId = 2L;

        User adminUser = new User();
        adminUser.setUserId(userId);
        adminUser.setEmail("admin@gmail.com");
        adminUser.setUserType("ADMIN");

        when(userRepository.findById(userId)).thenReturn(Mono.just(adminUser));

        Mono<AdminProfileDTO> result = adminService.getUserById(userId);

        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof UserNotFoundException &&
                                err.getMessage().equals("No user found with id: " + userId)
                )
                .verify();

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetAdminByIdSuccess() {
        Long userId = 1L;

        User admin = new User();
        admin.setUserId(userId);
        admin.setEmail("admin@gmail.com");
        admin.setUserType("ADMIN");
        admin.setCreatedAt(LocalDateTime.of(2025, 11, 7, 12, 0));

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFirstName("Ashish");
        profile.setLastName("Shenoy");
        profile.setAadharNumber("123456789012");
        profile.setCity("Mangalore");
        profile.setPhoneNumber("9876543210");

        when(userRepository.findById(userId)).thenReturn(Mono.just(admin));
        when(profileRepository.findByUserId(userId)).thenReturn(Mono.just(profile));

        Mono<AdminProfileDTO> result = adminService.getAdminById(userId);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals(userId, dto.getUserId());
                    assertEquals("admin@gmail.com", dto.getEmail());
                    assertEquals("ADMIN", dto.getUserType());
                    assertEquals("Ashish", dto.getFirstName());
                    assertEquals("Shenoy", dto.getLastName());
                    assertEquals("123456789012", dto.getAadharNumber());
                    assertEquals("Mangalore", dto.getCity());
                    assertEquals("9876543210", dto.getPhoneNumber());
                })
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(profileRepository).findByUserId(userId);
    }

    @Test
    void testGetAdminByIdUserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        Mono<AdminProfileDTO> result = adminService.getAdminById(userId);

        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof UserNotFoundException &&
                                err.getMessage().equals("User not found with id: " + userId)
                )
                .verify();

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetAdminByIdRegularUserInsteadOfAdmin() {
        Long userId = 2L;

        User regularUser = new User();
        regularUser.setUserId(userId);
        regularUser.setEmail("user@gmail.com");
        regularUser.setUserType("USER");

        when(userRepository.findById(userId)).thenReturn(Mono.just(regularUser));

        Mono<AdminProfileDTO> result = adminService.getAdminById(userId);

        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof UserNotFoundException &&
                                err.getMessage().equals("No admin found with id: " + userId)
                )
                .verify();

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetUserByEmailSuccess() {
        String email = "user@gmail.com";

        User user = new User();
        user.setUserId(1L);
        user.setEmail(email);
        user.setUserType("USER");
        user.setCreatedAt(LocalDateTime.of(2025, 11, 7, 12, 0));

        Profile profile = new Profile();
        profile.setUserId(1L);
        profile.setFirstName("Ashish");
        profile.setLastName("Shenoy");
        profile.setAadharNumber("123456789012");
        profile.setCity("Mangalore");
        profile.setPhoneNumber("9876543210");

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));
        when(profileRepository.findByUserId(user.getUserId())).thenReturn(Mono.just(profile));

        Mono<AdminProfileDTO> result = adminService.getUserByEmail(email);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals(user.getUserId(), dto.getUserId());
                    assertEquals(email, dto.getEmail());
                    assertEquals("USER", dto.getUserType());
                    assertEquals("Ashish", dto.getFirstName());
                    assertEquals("Shenoy", dto.getLastName());
                    assertEquals("123456789012", dto.getAadharNumber());
                    assertEquals("Mangalore", dto.getCity());
                    assertEquals("9876543210", dto.getPhoneNumber());
                })
                .verifyComplete();

        verify(userRepository).findByEmail(email);
        verify(profileRepository).findByUserId(user.getUserId());
    }

    @Test
    void testGetUserByEmailUserNotFound() {
        String email = "missing@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(Mono.empty());

        Mono<AdminProfileDTO> result = adminService.getUserByEmail(email);

        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof UserNotFoundException &&
                                err.getMessage().equals("User not found with email: " + email)
                )
                .verify();

        verify(userRepository).findByEmail(email);
    }

    @Test
    void testGetUserByEmailAdminInsteadOfUser() {
        String email = "admin@gmail.com";

        User admin = new User();
        admin.setUserId(2L);
        admin.setEmail(email);
        admin.setUserType("ADMIN");

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(admin));

        Mono<AdminProfileDTO> result = adminService.getUserByEmail(email);

        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof UserNotFoundException &&
                                err.getMessage().equals("No user found with email: " + email)
                )
                .verify();

        verify(userRepository).findByEmail(email);
    }

    @Test
    void testGetAllUserSuccess() {
        User user1 = new User();
        user1.setUserId(1L);
        user1.setEmail("user1@gmail.com");
        user1.setUserType("USER");
        user1.setCreatedAt(LocalDateTime.of(2025, 11, 7, 10, 0));

        User user2 = new User();
        user2.setUserId(2L);
        user2.setEmail("user2@gmail.com");
        user2.setUserType("USER");
        user2.setCreatedAt(LocalDateTime.of(2025, 11, 7, 11, 0));

        User admin = new User();
        admin.setUserId(3L);
        admin.setEmail("admin@gmail.com");
        admin.setUserType("ADMIN");

        Profile profile1 = new Profile();
        profile1.setUserId(1L);
        profile1.setFirstName("Ashish");
        profile1.setLastName("Shenoy");
        profile1.setAadharNumber("123456789012");
        profile1.setCity("Mangalore");
        profile1.setPhoneNumber("9999999999");

        Profile profile2 = new Profile();
        profile2.setUserId(2L);
        profile2.setFirstName("Shenoy");
        profile2.setLastName("Ashish");
        profile2.setAadharNumber("987654321098");
        profile2.setCity("Bangalore");
        profile2.setPhoneNumber("8888888888");

        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2, admin));
        when(profileRepository.findByUserId(1L)).thenReturn(Mono.just(profile1));
        when(profileRepository.findByUserId(2L)).thenReturn(Mono.just(profile2));

        Flux<AdminProfileDTO> result = adminService.getAllUser();

        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getUserId().equals(1L)
                                && dto.getEmail().equals("user1@gmail.com")
                                && dto.getFirstName().equals("Ashish")
                                && dto.getLastName().equals("Shenoy")
                )
                .expectNextMatches(dto ->
                        dto.getUserId().equals(2L)
                                && dto.getEmail().equals("user2@gmail.com")
                                && dto.getFirstName().equals("Shenoy")
                                && dto.getLastName().equals("Ashish")
                )
                .verifyComplete();

        verify(userRepository).findAll();
        verify(profileRepository).findByUserId(1L);
        verify(profileRepository).findByUserId(2L);
    }

    @Test
    void testGetAllUserNoUsersFound() {
        when(userRepository.findAll()).thenReturn(Flux.empty());

        Flux<AdminProfileDTO> result = adminService.getAllUser();

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(userRepository).findAll();
    }

    @Test
    void testGetBookingByIdSuccess() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setUserId(1L);
        booking.setDestination("Goa");
        booking.setRate(18000);
        booking.setBookingDate(LocalDate.of(2025, 11, 7));
        booking.setNumberOfPeople(4);

        when(bookingRepository.findById(1L)).thenReturn(Mono.just(booking));

        Mono<Booking> result = adminService.getBookingById(1L);

        StepVerifier.create(result)
                .assertNext(foundBooking -> {
                    assertEquals(1L, foundBooking.getBookingId());
                    assertEquals(1L, foundBooking.getUserId());
                    assertEquals("Goa", foundBooking.getDestination());
                    assertEquals(18000, foundBooking.getRate());
                    assertEquals(LocalDate.of(2025, 11, 7), foundBooking.getBookingDate());
                    assertEquals(4, foundBooking.getNumberOfPeople());
                })
                .verifyComplete();

        verify(bookingRepository).findById(1L);
    }

    @Test
    void testGetBookingByIdFail() {
        when(bookingRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Booking> result = adminService.getBookingById(1L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BookingIdNotFoundException &&
                                throwable.getMessage().equals("Booking not found with id: 1")
                )
                .verify();

        verify(bookingRepository).findById(1L);
    }

    @Test
    void testGetAllBookingsSuccess() {
        Booking b1 = new Booking();
        b1.setBookingId(1L);
        b1.setUserId(1L);
        b1.setDestination("Goa");
        b1.setRate(18000);
        b1.setBookingDate(LocalDate.of(2025, 11, 7));
        b1.setNumberOfPeople(4);

        Booking b2 = new Booking();
        b2.setBookingId(2L);
        b2.setUserId(1L);
        b2.setDestination("Ooty");
        b2.setRate(15000);
        b2.setBookingDate(LocalDate.of(2025, 11, 7));
        b2.setNumberOfPeople(4);

        when(bookingRepository.findAll()).thenReturn(Flux.just(b1, b2));

        Flux<Booking> result = adminService.getAllBookings();

        StepVerifier.create(result)
                .expectNext(b1)
                .expectNext(b2)
                .verifyComplete();

        verify(bookingRepository).findAll();
    }

    @Test
    void testGetAllBookingsEmpty() {
        when(bookingRepository.findAll()).thenReturn(Flux.empty());

        Flux<Booking> result = adminService.getAllBookings();

        StepVerifier.create(result)
                .verifyComplete();

        verify(bookingRepository).findAll();
    }

    @Test
    void testUpdateUserBookingSuccess() {
        Long bookingId = 1L;
        String status = "CONFIRMED";

        Booking existingBooking = new Booking();
        existingBooking.setBookingId(bookingId);
        existingBooking.setUserId(10L);
        existingBooking.setDestination("Goa");
        existingBooking.setStatus("PENDING");

        Booking updatedBooking = new Booking();
        updatedBooking.setBookingId(bookingId);
        updatedBooking.setUserId(10L);
        updatedBooking.setDestination("Goa");
        updatedBooking.setStatus(status);

        when(bookingRepository.findById(bookingId)).thenReturn(Mono.just(existingBooking));
        when(bookingRepository.save(existingBooking)).thenReturn(Mono.just(updatedBooking));

        Mono<Booking> result = adminService.updateUserBooking(bookingId, status);

        StepVerifier.create(result)
                .expectNextMatches(b -> b.getStatus().equals(status))
                .verifyComplete();

        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository).save(existingBooking);
    }

    @Test
    void testUpdateUserBookingNotFound() {
        Long bookingId = 99L;
        String status = "CANCELLED";

        when(bookingRepository.findById(bookingId)).thenReturn(Mono.empty());

        Mono<Booking> result = adminService.updateUserBooking(bookingId, status);

        StepVerifier.create(result)
                .expectError(BookingIdNotFoundException.class)
                .verify();

        verify(bookingRepository).findById(bookingId);
    }

    @Test
    void testUpdateUserByIdSuccess() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("old@gmail.com");

        Profile profile = new Profile();
        profile.setProfileId(1L);
        profile.setUserId(1L);
        profile.setFirstName("ashish");
        profile.setLastName("shenoy");
        profile.setAadharNumber("123456789034");
        profile.setCity("Mangalore");
        profile.setPhoneNumber("9876543210");

        UserProfileDTO dto = new UserProfileDTO();
        dto.setEmail("new@gmail.com");
        dto.setFirstName("shenoy");
        dto.setLastName("ashish");
        dto.setAadharNumber("123456789034");
        dto.setCity("Bangalore");
        dto.setPhoneNumber("9876543210");

        when(userRepository.findById(1L)).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        when(profileRepository.findByUserId(1L)).thenReturn(Mono.just(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(Mono.just(profile));

        Mono<UserProfileDTO> result = adminService.updateUserById(1L, dto);

        StepVerifier.create(result)
                .expectNextMatches(updated ->
                        updated.getEmail().equals("new@gmail.com") &&
                                updated.getFirstName().equals("shenoy") &&
                                updated.getLastName().equals("ashish") &&
                                updated.getAadharNumber().equals("123456789034") &&
                                updated.getPhoneNumber().equals("9876543210") &&
                                updated.getCity().equals("Bangalore"))
                .verifyComplete();

        verify(userRepository).findById(1L);
        verify(profileRepository).findByUserId(1L);
    }

    @Test
    void testUpdateUserByIdUserNotFound() {
        Long userId = 1L;
        UserProfileDTO dto = new UserProfileDTO();

        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        Mono<UserProfileDTO> result = adminService.updateUserById(userId, dto);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains("User not found"))
                .verify();
    }

    @Test
    void testDeleteUserByIdSuccess() {
        Long userId = 1L;
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(userRepository.delete(user)).thenReturn(Mono.empty());

        Mono<String> result = adminService.deleteUserById(userId);

        StepVerifier.create(result)
                .expectNext("User and related data deleted successfully for ID: " + userId)
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUserByIdUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        Mono<String> result = adminService.deleteUserById(userId);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof UserNotFoundException &&
                        e.getMessage().contains("User not found"))
                .verify();

        verify(userRepository).findById(userId);
    }
}
