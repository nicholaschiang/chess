package dataAccess;

import model.UserData;

interface UserDataAccess {
  // Create a new user.
  public void createUser(UserData user);

  // Retrieve a user with the given username.
  public UserData getUser(String username);
}
