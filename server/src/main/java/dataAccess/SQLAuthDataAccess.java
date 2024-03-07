package dataAccess;

import java.util.HashMap;
import java.util.Map;
import model.AuthData;

public class SQLAuthDataAccess implements AuthDataAccess {

  public SQLAuthDataAccess() {}

  private Map<String, AuthData> auths = new HashMap<String, AuthData>();

  // Clear all auths.
  public void clear() {
    auths.clear();
  }

  // Create a new authorization.
  public void createAuth(AuthData auth) {
    auths.put(auth.getAuthToken(), auth);
  }

  // Retrieve an authorization given an authToken.
  public AuthData getAuth(String authToken) {
    return auths.get(authToken);
  }

  // Delete an authorization so that it is no longer valid.
  public void deleteAuth(String authToken) {
    auths.remove(authToken);
  }
}
