package dataAccessTests;

import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import dataAccess.DatabaseManager;
import exception.ResponseException;
import java.util.Collection;
import java.util.List;
import model.*;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDataAccessTests extends DataAccessTests {
  private static GameData gameData = new GameData(1, null, null, "name", new ChessGame());

  @BeforeEach
  public void setup() throws Exception {
    super.setup();
    gameData = gameDataAccess.createGame(gameData);
  }

  @Test
  @Order(1)
  @DisplayName("List Games")
  public void listGames() throws Exception {
    Collection<GameData> games = List.of(gameData);

    // List games.
    Collection<GameData> response = gameDataAccess.listGames();
    assertEquals(
        games.size(), response.size(), "List games should return the expected response length");
  }

  @Test
  @Order(2)
  @DisplayName("List Games Table Does Not Exist")
  public void listGamesTableDoesNotExist() throws Exception {
    // I could not think of a valid error for the listGames() method, so I'm
    // just artificially dropping the database table and testing that the MySQL
    // error is propagated correctly.
    try (var conn = DatabaseManager.getConnection()) {
      System.out.println("Dropping games table...");
      conn.createStatement().execute("DROP TABLE game");
    }

    // Try to list games.
    assertThrows(
        ResponseException.class,
        () -> {
          gameDataAccess.listGames();
        },
        "You cannot list games when the table does not exist");
  }

  @Test
  @Order(3)
  @DisplayName("Create Game")
  public void createGame() throws Exception {
    // Create a game.
    GameData newGameData = new GameData(2, null, null, "newName", new ChessGame());
    GameData createdGameData = gameDataAccess.createGame(newGameData);
    assertNotNull(createdGameData.getGameId(), "Game ID should not be null");
    assertEquals(
        createdGameData.getGame(),
        gameDataAccess.getGame(createdGameData.getGameId()).getGame(),
        "Game should now exist in our database");
  }

  @Test
  @Order(4)
  @DisplayName("Create Game User Does Not Exist")
  public void createGameUserDoesNotExist() throws Exception {
    GameData newGameData = new GameData(2, "john", null, "newName", new ChessGame());
    assertThrows(
        ResponseException.class,
        () -> {
          gameDataAccess.createGame(newGameData);
        },
        "Create game should throw an exception when the user does not exist");
  }

  @Test
  @Order(5)
  @DisplayName("Get Game")
  public void getGame() throws Exception {
    var fetchedGameData = gameDataAccess.getGame(gameData.getGameId());
    assertEquals(
        gameData.getGame(), fetchedGameData.getGame(), "Game should exist in our database");
  }

  @Test
  @Order(6)
  @DisplayName("Get Game Does Not Exist")
  public void getGameDoesNotExist() throws Exception {
    var fetchedGameData = gameDataAccess.getGame(420);
    assertNull(fetchedGameData, "Game should not exist in our database");
  }

  @Test
  @Order(7)
  @DisplayName("Update Game")
  public void updateGame() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    userDataAccess.createUser(userData);

    // Join a game.
    var updatedGameData =
        gameDataAccess.updateGame(
            gameData.getGameId(),
            new GameData(
                gameData.getGameId(),
                null,
                userData.getUsername(),
                gameData.getGameName(),
                gameData.getGame()));
    assertNotNull(
        updatedGameData.getBlackUsername(), "Black player should not be null after joining game");

    // Make a valid chess move.
    var chessGame = updatedGameData.getGame();
    var validMoves = chessGame.validMoves(new ChessPosition(2, 1));
    ChessMove move = validMoves.toArray(new ChessMove[0])[0];
    chessGame.makeMove(move);
    updatedGameData =
        gameDataAccess.updateGame(
            gameData.getGameId(),
            new GameData(
                gameData.getGameId(),
                null,
                userData.getUsername(),
                gameData.getGameName(),
                chessGame));
    assertEquals(
        updatedGameData.getGame(), chessGame, "Game should be updated after making a move");

    // Get the game again and check that the move was made.
    updatedGameData = gameDataAccess.getGame(gameData.getGameId());
    assertEquals(
        updatedGameData.getGame(), chessGame, "Game should be updated after making a move");
  }

  @Test
  @Order(8)
  @DisplayName("Join Game User Does Not Exist")
  public void joinGameUserDoesNotExist() throws Exception {
    // Try to join the game again with a user that does not exist.
    assertThrows(
        ResponseException.class,
        () -> {
          gameDataAccess.updateGame(
              gameData.getGameId(),
              new GameData(
                  gameData.getGameId(), null, "DNE", gameData.getGameName(), gameData.getGame()));
        },
        "You cannot join a game with a user that does not exist");
  }

  @Test
  @Order(9)
  @DisplayName("Clear Games")
  public void clearGames() throws Exception {
    gameDataAccess.clear();
    Collection<GameData> games = gameDataAccess.listGames();
    assertEquals(0, games.size(), "There should be no games in the database");
  }
}
