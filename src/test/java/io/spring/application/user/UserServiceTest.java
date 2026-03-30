package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService =
        new UserService(
            userRepository,
            "https://static.productionready.io/images/smiley-cyrus.jpg",
            passwordEncoder);
  }

  @Test
  void createUser_shouldEncodePasswordAndSave() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "plaintext");
    when(passwordEncoder.encode("plaintext")).thenReturn("encodedpassword");

    User user = userService.createUser(registerParam);

    assertNotNull(user);
    assertEquals("test@example.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("encodedpassword", user.getPassword());
    assertEquals("", user.getBio());
    assertEquals("https://static.productionready.io/images/smiley-cyrus.jpg", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void createUser_shouldSetDefaultImage() {
    RegisterParam registerParam = new RegisterParam("a@b.com", "user1", "pass");
    when(passwordEncoder.encode("pass")).thenReturn("encoded");

    User user = userService.createUser(registerParam);

    assertEquals("https://static.productionready.io/images/smiley-cyrus.jpg", user.getImage());
  }

  @Test
  void updateUser_shouldDelegateToUserUpdateAndSave() {
    User user = new User("old@example.com", "olduser", "oldpass", "old bio", "old.jpg");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .bio("new bio")
            .image("new.jpg")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("new@example.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("new bio", user.getBio());
    assertEquals("new.jpg", user.getImage());
    verify(userRepository).save(user);
  }

  @Test
  void updateUser_shouldNotChangeFieldsWhenParamFieldsAreEmpty() {
    User user = new User("keep@example.com", "keepuser", "keeppass", "keep bio", "keep.jpg");
    UpdateUserParam param = UpdateUserParam.builder().build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("keep@example.com", user.getEmail());
    assertEquals("keepuser", user.getUsername());
    assertEquals("keep bio", user.getBio());
    assertEquals("keep.jpg", user.getImage());
    verify(userRepository).save(user);
  }
}
