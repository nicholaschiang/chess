package dataAccess;

import model.AuthData;

public interface AuthDataAccess extends DataAccess {
  // Create a new authorization.
  public void createAuth(AuthData auth);

  // Retrieve an authorization given an authToken.
  public AuthData getAuth(String authToken);

  // Delete an authorization so that it is no longer valid.
  public void deleteAuth(String authToken);
}
