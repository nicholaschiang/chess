package dataAccessTests;

import static org.junit.jupiter.api.Assertions.*;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDataAccessTests extends DataAccessTests {
  private static UserData userData = new UserData("john.doe", "password", "john.doe@example.com");

  @BeforeEach
  public void setup() throws Exception {
    super.setup();
    userData = userDataAccess.createUser(userData);
  }

  @Test
  @Order(1)
  @DisplayName("Create User")
  public void createUser() throws Exception {
    // Create a user.
    var newUserData = new UserData("jane.doe", "password", "jane.doe@example.com");
    var createdUserData = userDataAccess.createUser(newUserData);
    assertNotNull(createdUserData.getUsername(), "Username should not be null");
  }

  @Test
  @Order(2)
  @DisplayName("Create User Duplicate Username")
  public void createUserDuplicateUsername() throws Exception {
    assertThrows(
        ResponseException.class,
        () -> {
          userDataAccess.createUser(userData);
        },
        "Create user should throw an exception when the user already exists");
  }

  @Test
  @Order(3)
  @DisplayName("Get User")
  public void getUser() throws Exception {
    // Get a user.
    var fetchedUserData = userDataAccess.getUser(userData.getUsername());
    assertEquals(
        userData.getUsername(),
        fetchedUserData.getUsername(),
        "Username should match the fetched user");
  }

  @Test
  @Order(4)
  @DisplayName("Get User Does Not Exist")
  public void getUserDoesNotExist() throws Exception {
    // Try to get a user that does not exist.
    var user = userDataAccess.getUser("DNE");
    assertNull(user, "You cannot get a user that does not exist");
  }

  @Test
  @Order(5)
  @DisplayName("Clear Users")
  public void clearUsers() throws Exception {
    userDataAccess.clear();
    var user = userDataAccess.getUser(userData.getUsername());
    assertNull(user, "You should not be able to get a user after clearing the data");
  }
}
