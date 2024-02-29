package dataAccess;

import model.AuthData;

interface AuthDataAccess {
  // Create a new authorization.
  public void createAuth(AuthData auth);

  // Retrieve an authorization given an authToken.
  public AuthData getAuth(String authToken);

  // Delete an authorization so that it is no longer valid.
  public void deleteAuth(String authToken);
}
