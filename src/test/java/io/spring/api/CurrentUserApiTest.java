package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrentUserApi.class)
@Import({
  WebSecurityConfig.class,
  JacksonCustomizations.class,
  UserService.class,
  ValidationAutoConfiguration.class,
  BCryptPasswordEncoder.class
})
public class CurrentUserApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private UserQueryService userQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_current_user_with_token() throws Exception {
    when(userQueryService.findById(any())).thenReturn(Optional.of(userData));

    given()
        .header("Authorization", "Token " + token)
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username))
        .body("user.bio", equalTo(""))
        .body("user.image", equalTo(defaultAvatar))
        .body("user.token", equalTo(token));
  }

  @Test
  public void should_get_401_without_token() throws Exception {
    given().contentType("application/json").when().get("/user").then().statusCode(401);
  }

  @Test
  public void should_get_401_with_invalid_token() throws Exception {
    String invalidToken = "asdfasd";
    when(jwtService.getSubFromToken(eq(invalidToken))).thenReturn(Optional.empty());
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + invalidToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_update_current_user_profile() throws Exception {
    String newEmail = "newemail@example.com";
    String newBio = "updated";
    String newUsername = "newusernamee";

    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("email", newEmail);
                    put("bio", newBio);
                    put("username", newUsername);
                  }
                });
          }
        };

    when(userRepository.findByUsername(eq(newUsername))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.empty());

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_error_if_email_exists_when_update_user_profile() throws Exception {
    String newEmail = "newemail@example.com";
    String newBio = "updated";
    String newUsername = "newusernamee";

    Map<String, Object> param = prepareUpdateParam(newEmail, newBio, newUsername);

    when(userRepository.findByEmail(eq(newEmail)))
        .thenReturn(Optional.of(new User(newEmail, "username", "123", "", "")));
    when(userRepository.findByUsername(eq(newUsername))).thenReturn(Optional.empty());

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .prettyPeek()
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("email already exist"));
  }

  private HashMap<String, Object> prepareUpdateParam(
      final String newEmail, final String newBio, final String newUsername) {
    return new HashMap<String, Object>() {
      {
        put(
            "user",
            new HashMap<String, Object>() {
              {
                put("email", newEmail);
                put("bio", newBio);
                put("username", newUsername);
              }
            });
      }
    };
  }

  @Test
  public void should_get_401_if_not_login() throws Exception {
    given()
        .contentType("application/json")
        .body(
            new HashMap<String, Object>() {
              {
                put("user", new HashMap<String, Object>());
              }
            })
        .when()
        .put("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_error_if_username_exists_when_update_user_profile() throws Exception {
    String newEmail = "newemail@example.com";
    String newBio = "updated";
    String newUsername = "existingusername";

    Map<String, Object> param = prepareUpdateParam(newEmail, newBio, newUsername);

    when(userRepository.findByUsername(eq(newUsername)))
        .thenReturn(Optional.of(new User("other@example.com", newUsername, "123", "", "")));
    when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.empty());

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(422)
        .body("errors.username[0]", equalTo("username already exist"));
  }

  @Test
  public void should_update_profile_with_same_email_and_username() throws Exception {
    String sameEmail = email;
    String sameBio = "updated bio";
    String sameUsername = username;

    Map<String, Object> param = prepareUpdateParam(sameEmail, sameBio, sameUsername);

    when(userRepository.findByUsername(eq(sameUsername))).thenReturn(Optional.of(user));
    when(userRepository.findByEmail(eq(sameEmail))).thenReturn(Optional.of(user));

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_update_profile_with_only_password() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("password", "newpassword123");
                  }
                });
          }
        };

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_update_profile_with_only_bio() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("bio", "This is my new bio");
                  }
                });
          }
        };

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_update_profile_with_only_image() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("image", "https://example.com/newimage.jpg");
                  }
                });
          }
        };

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_show_error_for_invalid_email_format_in_update() throws Exception {
    String invalidEmail = "notanemail";
    String newBio = "updated";
    String newUsername = "newusername";

    Map<String, Object> param = prepareUpdateParam(invalidEmail, newBio, newUsername);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("should be an email"));
  }

  @Test
  public void should_get_401_with_malformed_authorization_header() throws Exception {
    when(userQueryService.findById(any())).thenReturn(Optional.of(userData));

    given()
        .header("Authorization", "InvalidFormat")
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_401_with_empty_authorization_header() throws Exception {
    given()
        .header("Authorization", "")
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_401_for_token_of_non_existent_user() throws Exception {
    String nonExistentToken = "validformatbutnonexistent";
    when(jwtService.getSubFromToken(eq(nonExistentToken)))
        .thenReturn(Optional.of("nonexistentuserid"));
    when(userRepository.findById(eq("nonexistentuserid"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + nonExistentToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_401_when_updating_with_invalid_token() throws Exception {
    String invalidToken = "invalidtoken";
    when(jwtService.getSubFromToken(eq(invalidToken))).thenReturn(Optional.empty());

    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("bio", "new bio");
                  }
                });
          }
        };

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + invalidToken)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_401_when_updating_with_malformed_authorization_header() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("bio", "new bio");
                  }
                });
          }
        };

    given()
        .contentType("application/json")
        .header("Authorization", "MalformedHeader")
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_update_profile_with_empty_bio() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("bio", "");
                  }
                });
          }
        };

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_update_profile_with_empty_image() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("image", "");
                  }
                });
          }
        };

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_update_profile_with_all_fields() throws Exception {
    String newEmail = "newemail@example.com";
    String newPassword = "newpassword123";
    String newUsername = "newusername";
    String newBio = "This is my new bio";
    String newImage = "https://example.com/newimage.jpg";

    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("email", newEmail);
                    put("password", newPassword);
                    put("username", newUsername);
                    put("bio", newBio);
                    put("image", newImage);
                  }
                });
          }
        };

    when(userRepository.findByUsername(eq(newUsername))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.empty());
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }
}
