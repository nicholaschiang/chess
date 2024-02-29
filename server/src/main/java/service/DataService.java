package service;

import dataAccess.*;

public class DataService {
  private UserDataAccess userDataAccess;
  private AuthDataAccess authDataAccess;
  private GameDataAccess gameDataAccess;

  public DataService(
      UserDataAccess userDataAccess, AuthDataAccess authDataAccess, GameDataAccess gameDataAccess) {
    this.userDataAccess = userDataAccess;
    this.authDataAccess = authDataAccess;
    this.gameDataAccess = gameDataAccess;
  }

  public void clearData() {
    userDataAccess.clear();
    authDataAccess.clear();
    gameDataAccess.clear();
  }
}
