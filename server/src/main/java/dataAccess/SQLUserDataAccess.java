package dataAccess;

import exception.ResponseException;
import java.sql.*;
import model.UserData;

public class SQLUserDataAccess extends SQLDataAccess implements UserDataAccess {
  private static final String[] createStatements = {
    """
    CREATE TABLE IF NOT EXISTS user (
      username varchar(256) NOT NULL,
      password varchar(256) NOT NULL,
      email varchar(256) NOT NULL,
      PRIMARY KEY (username)
    );
    """
  };

  public SQLUserDataAccess() throws ResponseException {
    super(createStatements);
  }

  // Clears all users.
  public void clear() throws ResponseException {
    var statement = "TRUNCATE user";
    executeUpdate(statement);
  }

  // Create a new user.
  public UserData createUser(UserData user) throws ResponseException {
    var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
    executeUpdate(statement, user.getUsername(), user.getPassword(), user.getEmail());
    return new UserData(user.getUsername(), user.getPassword(), user.getEmail());
  }

  // Retrieve a user with the given username.
  public UserData getUser(String username) throws ResponseException {
    try (var conn = DatabaseManager.getConnection()) {
      var statement = "SELECT username, password, email FROM user WHERE username=?";
      try (var ps = conn.prepareStatement(statement)) {
        ps.setString(1, username);
        try (var rs = ps.executeQuery()) {
          if (rs.next()) {
            return readUser(rs);
          }
        }
      }
    } catch (Exception e) {
      throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
    }
    return null;
  }

  private UserData readUser(ResultSet rs) throws SQLException {
    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
  }
}
