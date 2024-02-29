package serviceTests;

import static org.junit.jupiter.api.Assertions.*;

import model.*;
import org.junit.jupiter.api.*;
import server.*;
import service.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTests extends ServiceTests {
  @Test
  @Order(1)
  @DisplayName("Register User")
  public void registerUser() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);
    assertNotNull(authData, "AuthData should not be null");
    assertNotNull(authData.getAuthToken(), "AuthData should have an access token");
  }

  @Test
  @Order(2)
  @DisplayName("Register User Already Exists")
  public void registerUserAlreadyExists() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);
    assertNotNull(authData, "AuthData should not be null");
    assertNotNull(authData.getAuthToken(), "AuthData should have an access token");

    // Try to create the same user again.
    assertThrows(
        ExceptionWithStatusCode.class,
        () -> {
          userService.registerUser(userData);
        },
        "Should throw an exception when trying to create a user that already exists");
  }

  @Test
  @Order(3)
  @DisplayName("Login User")
  public void loginUser() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);

    // Login the user.
    LoginRequest loginRequest = new LoginRequest(userData.getUsername(), userData.getPassword());
    AuthData newAuthData = userService.loginUser(loginRequest);
    assertNotNull(newAuthData, "AuthData should not be null");
    assertNotEquals(
        authData.getAuthToken(), newAuthData.getAuthToken(), "Auth tokens should be different");
  }

  @Test
  @Order(4)
  @DisplayName("Login User Incorrect Password")
  public void loginUserIncorrectPassword() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    userService.registerUser(userData);

    // Login the user.
    LoginRequest loginRequest = new LoginRequest(userData.getUsername(), "wrong");
    assertThrows(
        ExceptionWithStatusCode.class,
        () -> {
          userService.loginUser(loginRequest);
        },
        "Login should throw an exception when the password is incorrect");
  }

  @Test
  @Order(5)
  @DisplayName("Logout User")
  public void logoutUser() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);

    // Logout the user.
    userService.logoutUser(authData.getAuthToken());
    assertNull(authDataAccess.getAuth(authData.getAuthToken()), "AuthData should be null");
  }

  @Test
  @Order(6)
  @DisplayName("Logout User Unauthorized")
  public void logoutUserUnauthorized() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    userService.registerUser(userData);

    // Logout the user.
    assertThrows(
        ExceptionWithStatusCode.class,
        () -> {
          userService.logoutUser("wrong");
        },
        "You cannot log out a user with an incorrect token");
  }
}
