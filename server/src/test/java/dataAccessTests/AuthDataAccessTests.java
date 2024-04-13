package dataAccessTests;

import static org.junit.jupiter.api.Assertions.*;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import service.AuthService;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDataAccessTests extends DataAccessTests {
  private static UserData userData = new UserData("john.doe", "password", "john.doe@example.com");
  private static AuthData authData =
      new AuthData(userData.getUsername(), AuthService.generateNewToken());

  @BeforeEach
  public void setup() throws Exception {
    super.setup();
    userData = userDataAccess.createUser(userData);
    authData = authDataAccess.createAuth(authData);
  }

  @Test
  @Order(1)
  @DisplayName("Create Auth")
  public void createAuth() throws Exception {
    var newAuthData = new AuthData(userData.getUsername(), AuthService.generateNewToken());
    var createdAuthData = authDataAccess.createAuth(newAuthData);
    assertNotNull(createdAuthData.getUsername(), "Username should not be null");
  }

  @Test
  @Order(2)
  @DisplayName("Create Auth User Does Not Exist")
  public void createAuthUserDoesNotExist() throws Exception {
    var newAuthData = new AuthData("DNE", AuthService.generateNewToken());
    assertThrows(
        ResponseException.class,
        () -> {
          authDataAccess.createAuth(newAuthData);
        },
        "Create auth should throw an exception when the user does not exist");
  }

  @Test
  @Order(3)
  @DisplayName("Get Auth")
  public void getAuth() throws Exception {
    // Get an auth.
    var fetchedAuthData = authDataAccess.getAuth(authData.getAuthToken());
    assertEquals(
        authData.getAuthToken(),
        fetchedAuthData.getAuthToken(),
        "Auth token should match the fetched auth");
  }

  @Test
  @Order(4)
  @DisplayName("Get Auth Does Not Exist")
  public void getAuthDoesNotExist() throws Exception {
    // Get an auth.
    var fetchedAuthData = authDataAccess.getAuth("DNE");
    assertNull(fetchedAuthData, "You cannot get an auth that does not exist");
  }

  @Test
  @Order(5)
  @DisplayName("Delete Auth")
  public void deleteAuth() throws Exception {
    // Delete an auth.
    authDataAccess.deleteAuth(authData.getAuthToken());
    var fetchedAuthData = authDataAccess.getAuth(authData.getAuthToken());
    assertNull(fetchedAuthData, "Auth should be deleted");
  }

  @Test
  @Order(6)
  @DisplayName("Delete Auth Does Not Exist")
  public void deleteAuthDoesNotExist() throws Exception {
    // Delete an auth.
    authDataAccess.deleteAuth("DNE");
    var fetchedAuthData = authDataAccess.getAuth(authData.getAuthToken());
    assertNotNull(fetchedAuthData, "Auth should be unaffected");
  }

  @Test
  @Order(7)
  @DisplayName("Clear Auths")
  public void clearAuths() throws Exception {
    authDataAccess.clear();
    var fetchedAuthData = authDataAccess.getAuth(authData.getAuthToken());
    assertNull(fetchedAuthData, "Auths should be cleared");
  }
}
