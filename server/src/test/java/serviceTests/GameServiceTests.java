package serviceTests;

import static org.junit.jupiter.api.Assertions.*;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import model.*;
import org.junit.jupiter.api.*;
import server.*;
import service.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTests extends ServiceTests {
  private static GameData gameData = new GameData(1, null, null, "name", new ChessGame());

  @BeforeEach
  public void setup() {
    super.setup();
    gameDataAccess.createGame(gameData);
  }

  @Test
  @Order(1)
  @DisplayName("List Games")
  public void listGames() throws Exception {
    GameData[] games = {gameData};
    ListGamesResponse expectedResponse = new ListGamesResponse(games);

    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);

    // List games.
    ListGamesResponse response = gameService.listGames(authData.getAuthToken());
    assertEquals(
        expectedResponse.getGames().length,
        response.getGames().length,
        "List games should return the expected response length");
  }

  @Test
  @Order(2)
  @DisplayName("List Games Unauthorized")
  public void listGamesUnauthorized() throws Exception {
    assertThrows(
        ExceptionWithStatusCode.class,
        () -> {
          gameService.listGames("invalidToken");
        },
        "List games should throw an exception when given an invalid token");
  }

  @Test
  @Order(3)
  @DisplayName("Create Game")
  public void createGame() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);

    // Create a game.
    GameData newGameData =
        new GameData(2, authData.getUsername(), null, "newName", new ChessGame());
    GameData createdGameData = gameService.createGame(authData.getAuthToken(), newGameData);
    assertEquals(newGameData, createdGameData, "Game should be created");
    assertEquals(
        newGameData,
        gameDataAccess.getGame(newGameData.getGameId()),
        "Game should now exist in our database");
  }

  @Test
  @Order(4)
  @DisplayName("Create Game Unauthorized")
  public void createGameUnauthorized() throws Exception {
    GameData newGameData = new GameData(2, "john", null, "newName", new ChessGame());
    assertThrows(
        ExceptionWithStatusCode.class,
        () -> {
          gameService.createGame("invalidToken", newGameData);
        },
        "Create game should throw an exception when given an invalid token");
  }

  @Test
  @Order(5)
  @DisplayName("Join Game")
  public void joinGame() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);

    // Join a game.
    JoinGameRequest joinGameRequest = new JoinGameRequest(TeamColor.BLACK, gameData.getGameId());
    GameData updatedGameData = gameService.joinGame(authData.getAuthToken(), joinGameRequest);
    assertNotNull(
        updatedGameData.getBlackUsername(), "Black player should not be null after joining game");
  }

  @Test
  @Order(6)
  @DisplayName("Join Game Already Joined")
  public void joinGameAlreadyJoined() throws Exception {
    // Create a user.
    UserData userData = new UserData("john", "password", "john@example.com");
    AuthData authData = userService.registerUser(userData);

    // Join a game.
    JoinGameRequest joinGameRequest = new JoinGameRequest(TeamColor.BLACK, gameData.getGameId());
    GameData updatedGameData = gameService.joinGame(authData.getAuthToken(), joinGameRequest);
    assertNotNull(
        updatedGameData.getBlackUsername(), "Black player should not be null after joining game");

    // Create another user.
    UserData userData2 = new UserData("jane", "password", "jane@example.com");
    AuthData authData2 = userService.registerUser(userData2);

    // Try to join the game again with a different user.
    assertThrows(
        ExceptionWithStatusCode.class,
        () -> {
          gameService.joinGame(authData2.getAuthToken(), joinGameRequest);
        },
        "You cannot join a game that has already been joined");
  }
}
