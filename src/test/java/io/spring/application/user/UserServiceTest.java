package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  public void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    userService =
        new UserService(
            userRepository,
            "https://static.productionready.io/images/smiley-cyrus.jpg",
            passwordEncoder);
  }

  @Test
  void createUser_withValidParams_createsUserWithEncodedPassword() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");

    User user = userService.createUser(registerParam);

    assertNotNull(user);
    assertEquals("test@example.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    assertEquals("", user.getBio());
    assertEquals("https://static.productionready.io/images/smiley-cyrus.jpg", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void createUser_setsDefaultImage() {
    RegisterParam registerParam = new RegisterParam("img@example.com", "imguser", "pass");

    User user = userService.createUser(registerParam);

    assertEquals("https://static.productionready.io/images/smiley-cyrus.jpg", user.getImage());
  }

  @Test
  void updateUser_withAllFields_updatesUser() {
    User existingUser = new User("old@example.com", "olduser", "oldpass", "old bio", "old image");

    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();

    existingUser.update(
        updateParam.getEmail(),
        updateParam.getUsername(),
        updateParam.getPassword(),
        updateParam.getBio(),
        updateParam.getImage());

    assertEquals("new@example.com", existingUser.getEmail());
    assertEquals("newuser", existingUser.getUsername());
    assertEquals("newpass", existingUser.getPassword());
    assertEquals("new bio", existingUser.getBio());
    assertEquals("new image", existingUser.getImage());
  }

  @Test
  void updateUser_withPartialFields_updatesOnlyProvidedFields() {
    User existingUser =
        new User("keep@example.com", "keepuser", "keeppass", "keep bio", "keep image");

    UpdateUserParam updateParam = UpdateUserParam.builder().bio("updated bio only").build();

    existingUser.update(
        updateParam.getEmail(),
        updateParam.getUsername(),
        updateParam.getPassword(),
        updateParam.getBio(),
        updateParam.getImage());

    assertEquals("keep@example.com", existingUser.getEmail());
    assertEquals("keepuser", existingUser.getUsername());
    assertEquals("updated bio only", existingUser.getBio());
  }

  @Test
  void updateUser_withDefaultValues_usesBuilderDefaults() {
    UpdateUserParam defaultParam = UpdateUserParam.builder().build();

    assertEquals("", defaultParam.getEmail());
    assertEquals("", defaultParam.getPassword());
    assertEquals("", defaultParam.getUsername());
    assertEquals("", defaultParam.getBio());
    assertEquals("", defaultParam.getImage());
  }

  @Test
  void updateUser_withSomeFieldsOverridden_usesCorrectValues() {
    UpdateUserParam param =
        UpdateUserParam.builder().email("custom@example.com").bio("custom bio").build();

    assertEquals("custom@example.com", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("custom bio", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void registerParam_gettersReturnCorrectValues() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "testpass");

    assertEquals("test@example.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("testpass", param.getPassword());
  }
}
