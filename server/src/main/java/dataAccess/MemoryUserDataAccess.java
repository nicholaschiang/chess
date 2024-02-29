package dataAccess;

import java.util.Map;

import model.UserData;

class interfaceMemoryUserDataAccess implements UserDataAccess {
  private Map<String, UserData> users;

  // Create a new user.
  public void createUser(UserData user) {
    users.put(user.getUsername(), user);
  }

  // Retrieve a user with the given username.
  public UserData getUser(String username) {
    return users.get(username);
  }
}
