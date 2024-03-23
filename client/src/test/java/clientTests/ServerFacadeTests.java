package clientTests;

import static org.junit.jupiter.api.Assertions.*;

import chess.*;
import model.*;
import org.junit.jupiter.api.*;
import server.*;
import ui.ServerFacade;

public class ServerFacadeTests {
  private static ServerFacade serverFacade;
  private static Server server;

  @BeforeAll
  public static void init() {
    server = new Server();
    var port = server.run(0);
    System.out.println("Started test HTTP server on " + port);
    serverFacade = new ServerFacade("http://localhost:" + port);
  }

  @AfterAll
  public static void stopServer() {
    server.stop();
  }

  @BeforeEach
  public void clearServer() throws Exception {
    server.clear();
  }

  @Test
  public void sampleTest() {
    Assertions.assertTrue(true);
  }

  @Test
  public void registerUserSuccess() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
  }

  @Test
  public void registerUserFailure() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
    assertThrows(
        Exception.class,
        () -> {
          serverFacade.registerUser(user);
        });
  }

  @Test
  public void loginUserSuccess() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
    var request = new LoginRequest(user.getUsername(), user.getPassword());
    var loginAuthData = serverFacade.loginUser(request);
    assertNotNull(loginAuthData);
  }

  @Test
  public void loginUserFailure() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var request = new LoginRequest(user.getUsername(), user.getPassword());
    assertThrows(
        Exception.class,
        () -> {
          serverFacade.loginUser(request);
        });
  }

  @Test
  public void logoutUserSuccess() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
    assertDoesNotThrow(
        () -> {
          serverFacade.logoutUser(authData.getAuthToken());
        });
  }

  @Test
  public void logoutUserFailure() {
    assertThrows(
        Exception.class,
        () -> {
          serverFacade.logoutUser("DNE");
        });
  }

  @Test
  public void listGamesSuccess() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
    var games = serverFacade.listGames(authData.getAuthToken());
    assertNotNull(games);
  }

  @Test
  public void listGamesFailure() {
    assertThrows(
        Exception.class,
        () -> {
          serverFacade.listGames("DNE");
        });
  }

  @Test
  public void createGameSuccess() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
    var gameData = new GameData(0, null, null, "Test Game", new ChessGame());
    var game = serverFacade.createGame(authData.getAuthToken(), gameData);
    assertNotNull(game);
  }

  @Test
  public void createGameFailure() {
    var gameData = new GameData(0, null, null, "Test Game", new ChessGame());
    assertThrows(
        Exception.class,
        () -> {
          serverFacade.createGame("DNE", gameData);
        });
  }

  @Test
  public void joinGameSuccess() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
    var gameData = new GameData(0, null, null, "Test Game", new ChessGame());
    var game = serverFacade.createGame(authData.getAuthToken(), gameData);
    assertNotNull(game);
    var request = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.getGameId());
    var joinedGame = serverFacade.joinGame(authData.getAuthToken(), request);
    assertNotNull(joinedGame);
  }

  @Test
  public void joinGameFailure() {
    var user = new UserData("johndoe", "password", "john.doe@example.com");
    var authData = serverFacade.registerUser(user);
    assertNotNull(authData);
    var gameData = new GameData(0, null, null, "Test Game", new ChessGame());
    var game = serverFacade.createGame(authData.getAuthToken(), gameData);
    assertNotNull(game);
    var request = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.getGameId());
    assertThrows(
        Exception.class,
        () -> {
          serverFacade.joinGame("DNE", request);
        });
  }
}
