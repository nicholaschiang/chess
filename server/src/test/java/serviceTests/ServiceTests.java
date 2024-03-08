package serviceTests;

import dataAccess.*;
import org.junit.jupiter.api.*;
import service.*;

abstract class ServiceTests {
  protected static UserDataAccess userDataAccess = new MemoryUserDataAccess();
  protected static AuthDataAccess authDataAccess = new MemoryAuthDataAccess();
  protected static GameDataAccess gameDataAccess = new MemoryGameDataAccess();
  protected static UserService userService = new UserService(userDataAccess, authDataAccess);
  protected static GameService gameService = new GameService(authDataAccess, gameDataAccess);
  protected static DataService dataService =
      new DataService(userDataAccess, authDataAccess, gameDataAccess);

  @BeforeEach
  public void setup() throws Exception {
    userDataAccess.clear();
    authDataAccess.clear();
    gameDataAccess.clear();
  }
}
