package dataAccess;

import java.util.Map;
import model.UserData;

public class MemoryUserDataAccess implements UserDataAccess {
  private Map<String, UserData> users;

  // Clears all users.
  public void clear() {
    users.clear();
  }

  // Create a new user.
  public void createUser(UserData user) {
    users.put(user.getUsername(), user);
  }

  // Retrieve a user with the given username.
  public UserData getUser(String username) {
    return users.get(username);
  }
}
