package dataAccessTests;

import dataAccess.*;
import org.junit.jupiter.api.*;

abstract class DataAccessTests {
  protected UserDataAccess userDataAccess;
  protected AuthDataAccess authDataAccess;
  protected GameDataAccess gameDataAccess;

  @BeforeEach
  public void setup() throws Exception {
    userDataAccess = new SQLUserDataAccess();
    authDataAccess = new SQLAuthDataAccess();
    gameDataAccess = new SQLGameDataAccess();
    authDataAccess.clear();
    gameDataAccess.clear();
    userDataAccess.clear();
  }
}
