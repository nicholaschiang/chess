package dataAccess;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import model.GameData;

public class SQLGameDataAccess extends SQLDataAccess implements GameDataAccess {
  protected final String[] createStatements = {
    """
    CREATE TABLE IF NOT EXISTS game (
      `gameID` int NOT NULL AUTO_INCREMENT,
      `whiteUsername` varchar(256) NOT NULL,
      `blackUsername` varchar(256) NOT NULL,
      `gameName` varchar(256) NOT NULL,
      `json` TEXT DEFAULT NULL,
      PRIMARY KEY (`gameID`),
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
    """
  };

  public SQLGameDataAccess() throws ResponseException {
    super();
  }

  // Clears all games.
  public void clear() throws ResponseException {
    var statement = "TRUNCATE game";
    executeUpdate(statement);
  }

  // Create a new game.
  public GameData createGame(GameData game) throws ResponseException {
    var statement =
        "INSERT INTO game (whiteUsername, blackUsername, gameName, json) VALUES (?, ?, ?, ?)";
    var json = new Gson().toJson(game.getGame());
    var id =
        executeUpdate(
            statement, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), json);
    return new GameData(
        id, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), game.getGame());
  }

  // Updates a chess game. It should replace the chess game string corresponding
  // to a given gameID. This is used when players join a game or when a move is
  // made.
  public GameData updateGame(int gameID, GameData game) throws ResponseException {
    var statement =
        "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, json = ? WHERE gameID"
            + " = ?";
    var id =
        executeUpdate(
            statement,
            game.getWhiteUsername(),
            game.getBlackUsername(),
            game.getGameName(),
            new Gson().toJson(game.getGame()),
            gameID);
    return new GameData(
        id, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), game.getGame());
  }

  // Retrieve a specified game with the given game ID.
  public GameData getGame(int gameID) throws ResponseException {
    try (var conn = DatabaseManager.getConnection()) {
      var statement = "SELECT * FROM game WHERE gameID = ?";
      try (var ps = conn.prepareStatement(statement)) {
        ps.setInt(1, gameID);
        try (var rs = ps.executeQuery()) {
          if (rs.next()) {
            return readGame(rs);
          }
        }
      }
    } catch (Exception e) {
      throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
    }
    return null;
  }

  // Retrieve all games.
  public Collection<GameData> listGames() throws ResponseException {
    var result = new ArrayList<GameData>();
    try (var conn = DatabaseManager.getConnection()) {
      var statement = "SELECT * FROM game";
      try (var ps = conn.prepareStatement(statement)) {
        try (var rs = ps.executeQuery()) {
          while (rs.next()) {
            result.add(readGame(rs));
          }
        }
      }
    } catch (Exception e) {
      throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
    }
    return result;
  }

  private GameData readGame(ResultSet rs) throws SQLException {
    var id = rs.getInt("id");
    var whiteUsername = rs.getString("whiteUsername");
    var blackUsername = rs.getString("blackUsername");
    var gameName = rs.getString("gameName");
    var json = rs.getString("json");
    var game = new Gson().fromJson(json, ChessGame.class);
    return new GameData(id, whiteUsername, blackUsername, gameName, game);
  }
}
