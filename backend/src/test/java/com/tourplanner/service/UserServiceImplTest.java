package com.tourplanner.service;

import com.tourplanner.dto.UpdateCurrentProfileInputDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.dto.UserRegisterDTO;
import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.main.MainApplication;
import com.tourplanner.model.Booking;
import com.tourplanner.model.Profile;
import com.tourplanner.model.User;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.ProfileRepository;
import com.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MainApplication.class)
public class UserServiceImplTest {
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    @Test
    void testRegisterUserSuccess() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("test@gmail.com");
        dto.setPassword("test1234");
        dto.setFirstName("test");
        dto.setLastName("test");
        dto.setAadharNumber("123456789012");
        dto.setCity("Mangalore");
        dto.setPhoneNumber("9876543210");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setEmail(dto.getEmail());

        Profile savedProfile = new Profile();
        savedProfile.setUserId(1L);

        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(profileRepository.save(any(Profile.class))).thenReturn(Mono.just(savedProfile));

        Mono<UserRegisterDTO> result = userService.registerUser(dto);

        StepVerifier.create(result)
                .assertNext(returnedDto -> {
                    Assertions.assertEquals("test@gmail.com", returnedDto.getEmail());
                    Assertions.assertEquals("test", returnedDto.getFirstName());
                    Assertions.assertEquals("Mangalore", returnedDto.getCity());
                }).verifyComplete();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUserDuplicateEmail() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("test@gmail.com");
        dto.setPassword("test1234");
        dto.setFirstName("test");
        dto.setLastName("test");
        dto.setAadharNumber("123456789012");
        dto.setCity("Mangalore");
        dto.setPhoneNumber("9876543210");

        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("Duplicate email")));

        Mono<UserRegisterDTO> result = userService.registerUser(dto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Email already registered")
                ).verify();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetCurrentUserProfileSuccess() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@gmail.com");

        Profile profile = new Profile();
        profile.setProfileId(1L);
        profile.setUserId(1L);
        profile.setFirstName("test");
        profile.setLastName("test");
        profile.setAadharNumber("123456789034");
        profile.setPhoneNumber("9876543210");
        profile.setCity("Mangalore");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.just(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Mono.just(profile));

        Mono<UserProfileDTO> result = userService.getCurrentUserProfile("test@gmail.com");

        StepVerifier.create(result)
                .assertNext(dto -> {
                    Assertions.assertEquals("test", dto.getFirstName());
                    Assertions.assertEquals("test", dto.getLastName());
                    Assertions.assertEquals("123456789034", dto.getAadharNumber());
                    Assertions.assertEquals("9876543210", dto.getPhoneNumber());
                    Assertions.assertEquals("Mangalore", dto.getCity());
                }).verifyComplete();

        verify(userRepository).findByEmail("test@gmail.com");
        verify(profileRepository).findByUserId(1L);
    }

    @Test
    void testGetCurrentUserProfileFail() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.empty());

        Mono<UserProfileDTO> result = userService.getCurrentUserProfile("test@gmail.com");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException && throwable.getMessage().equals("User not found with email: test@gmail.com")
                ).verify();

        verify(userRepository).findByEmail("test@gmail.com");
    }

    @Test
    void testGetAllBookingForAUserSuccess() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@gmail.com");

        Booking b1 = new Booking();
        b1.setUserId(1L);
        b1.setBookingId(1L);

        Booking b2 = new Booking();
        b2.setUserId(1L);
        b2.setBookingId(2L);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.just(user));
        when(bookingRepository.findByUserId(1L)).thenReturn(Flux.just(b1, b2));

        Flux<Booking> result = userService.getAllBookingForAUser("test@gmail.com");

        StepVerifier.create(result)
                .assertNext(booking -> {
                    Assertions.assertEquals(1L, booking.getBookingId());
                    Assertions.assertEquals(1L, booking.getUserId());
                })
                .assertNext(booking -> {
                    Assertions.assertEquals(2L, booking.getBookingId());
                    Assertions.assertEquals(1L, booking.getUserId());
                })
                .verifyComplete();

        verify(userRepository).findByEmail("test@gmail.com");
        verify(bookingRepository).findByUserId(1L);
    }

    @Test
    void testGetAllBookingForAUserFail() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.empty());

        Flux<Booking> result = userService.getAllBookingForAUser("test@gmail.com");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof UserNotFoundException && throwable.getMessage().equals("User not found with email: test@gmail.com")
                ).verify();

        verify(userRepository).findByEmail("test@gmail.com");
    }

    @Test
    void testDeleteUserByEmailSuccess() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.just(user));
        when(userRepository.delete(user)).thenReturn(Mono.empty());

        Mono<String> result = userService.deleteUserByEmail("test@gmail.com");

        StepVerifier.create(result)
                .expectNext("User and related data deleted successfully for email: test@gmail.com")
                .verifyComplete();

        verify(userRepository).findByEmail("test@gmail.com");
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUserByEmailFail() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.empty());

        Mono<String> result = userService.deleteUserByEmail("test@gmail.com");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found with email: test@gmail.com")
                ).verify();

        verify(userRepository).findByEmail("test@gmail.com");
    }

    @Test
    void testUpdateCurrentUserProfileSuccess() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@gmail.com");

        Profile existingProfile = new Profile();
        existingProfile.setProfileId(1L);
        existingProfile.setUserId(1L);
        existingProfile.setCity("Mumbai");
        existingProfile.setPhoneNumber("1111111111");

        Profile updatedProfile = new Profile();
        updatedProfile.setProfileId(1L);
        updatedProfile.setUserId(1L);
        updatedProfile.setCity("Mangalore");
        updatedProfile.setPhoneNumber("9876543210");

        UpdateCurrentProfileInputDTO inputDTO = new UpdateCurrentProfileInputDTO();
        inputDTO.setCity("Mangalore");
        inputDTO.setPhoneNumber("9876543210");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.just(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Mono.just(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(Mono.just(updatedProfile));

        Mono<Profile> result = userService.updateCurrentUserProfile("test@gmail.com", inputDTO);

        StepVerifier.create(result)
                .assertNext(profile -> {
                    Assertions.assertEquals("Mangalore", profile.getCity());
                    Assertions.assertEquals("9876543210", profile.getPhoneNumber());
                })
                .verifyComplete();

        verify(userRepository).findByEmail("test@gmail.com");
        verify(profileRepository).findByUserId(1L);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testUpdateCurrentUserProfileFail() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.empty());

        UpdateCurrentProfileInputDTO inputDTO = new UpdateCurrentProfileInputDTO();
        inputDTO.setCity("Mangalore");
        inputDTO.setPhoneNumber("9876543210");

        Mono<Profile> result = userService.updateCurrentUserProfile("test@gmail.com", inputDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found with email: test@gmail.com")
                ).verify();

        verify(userRepository).findByEmail("test@gmail.com");
    }
}
