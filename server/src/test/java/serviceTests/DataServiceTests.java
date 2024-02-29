package serviceTests;

import static org.junit.jupiter.api.Assertions.*;

import model.UserData;
import org.junit.jupiter.api.*;
import service.DataService;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataServiceTests extends ServiceTests {
  private static DataService dataService =
      new DataService(userDataAccess, authDataAccess, gameDataAccess);

  @Test
  @Order(1)
  @DisplayName("Clear Data")
  public void clearData() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    userDataAccess.createUser(userData);
    assertNotNull(
        userDataAccess.getUser(userData.getUsername()),
        "User should not be null before clearing data");

    // Clear the data.
    dataService.clearData();

    // Ensure the user no longer exists.
    assertNull(
        userDataAccess.getUser(userData.getUsername()), "User should be null after clearing data");
  }
}
