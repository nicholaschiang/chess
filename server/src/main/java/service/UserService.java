package service;

import dataAccess.*;
import model.*;
import server.*;

public class UserService {
  private UserDataAccess userDataAccess;
  private AuthDataAccess authDataAccess;

  public UserService(UserDataAccess userDataAccess, AuthDataAccess authDataAccess) {
    this.userDataAccess = userDataAccess;
    this.authDataAccess = authDataAccess;
  }

  public AuthData registerUser(UserData user) throws ExceptionWithStatusCode {
    if (user.getUsername() == null || user.getPassword() == null) {
      throw new ExceptionWithStatusCode(400, "bad request");
    }
    UserData existingUser = userDataAccess.getUser(user.getUsername());
    if (existingUser != null) {
      throw new ExceptionWithStatusCode(403, "already taken");
    }
    userDataAccess.createUser(user);
    String authToken = AuthService.generateNewToken();
    AuthData authData = new AuthData(user.getUsername(), authToken);
    authDataAccess.createAuth(authData);
    return authData;
  }

  public AuthData loginUser(LoginRequest loginRequest) throws ExceptionWithStatusCode {
    UserData user = userDataAccess.getUser(loginRequest.getUsername());
    if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
      throw new ExceptionWithStatusCode(401, "unauthorized");
    }
    String authToken = AuthService.generateNewToken();
    AuthData authData = new AuthData(user.getUsername(), authToken);
    authDataAccess.createAuth(authData);
    return authData;
  }

  public void logoutUser(String authToken) throws ExceptionWithStatusCode {
    if (authDataAccess.getAuth(authToken) == null) {
      throw new ExceptionWithStatusCode(401, "unauthorized");
    }
    authDataAccess.deleteAuth(authToken);
  }
}
