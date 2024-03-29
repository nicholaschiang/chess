package server;

import com.google.gson.Gson;
import dataAccess.*;
import exception.ResponseException;
import model.*;
import service.*;
import spark.*;

public class Server {
  private Gson gson = new Gson();
  private UserDataAccess userDataAccess;
  private AuthDataAccess authDataAccess;
  private GameDataAccess gameDataAccess;
  private UserService userService;
  private GameService gameService;
  private DataService dataService;

  public Server() {
    try {
      userDataAccess = new SQLUserDataAccess();
      authDataAccess = new SQLAuthDataAccess();
      gameDataAccess = new SQLGameDataAccess();
      userService = new UserService(userDataAccess, authDataAccess);
      gameService = new GameService(authDataAccess, gameDataAccess);
      dataService = new DataService(userDataAccess, authDataAccess, gameDataAccess);
    } catch (Throwable ex) {
      System.err.println("Failed to initialize server: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public int run(int desiredPort) {
    Spark.port(desiredPort);

    Spark.staticFiles.location("web");

    // Register endpoints. I have so few that I can put them all here. If this
    // becomes unwieldy, I'll move them to their own classes.
    // https://github.com/softwareconstruction240/softwareconstruction/blob/main/chess/3-web-api/web-api.md#endpoint-specifications

    // Clears the database. Removes all users, games, and authTokens.
    Spark.delete(
        "/db",
        (request, response) -> {
          try {
            dataService.clearData();
            return "";
          } catch (ResponseException e) {
            System.err.println("Error clearing data: " + e.getMessage());
            response.status(e.getStatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
          } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse(e.getMessage()));
          }
        });

    // Register a new user.
    Spark.post(
        "/user",
        (request, response) -> {
          try {
            UserData user = gson.fromJson(request.body(), UserData.class);
            return gson.toJson(userService.registerUser(user));
          } catch (ResponseException e) {
            System.err.println("Error registering user: " + e.getMessage());
            response.status(e.getStatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
          } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse(e.getMessage()));
          }
        });

    // Logs in an existing user (returns a new authToken).
    Spark.post(
        "/session",
        (request, response) -> {
          try {
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
            return gson.toJson(userService.loginUser(loginRequest));
          } catch (ResponseException e) {
            System.err.println("Error logging in: " + e.getMessage());
            response.status(e.getStatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
          } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse(e.getMessage()));
          }
        });

    // Logs out the user represented by the authToken.
    Spark.delete(
        "/session",
        (request, response) -> {
          try {
            String authToken = request.headers("Authorization");
            userService.logoutUser(authToken);
            return "";
          } catch (ResponseException e) {
            System.err.println("Error logging out: " + e.getMessage());
            response.status(e.getStatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
          } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse(e.getMessage()));
          }
        });

    // Gives a list of all games.
    Spark.get(
        "/game",
        (request, response) -> {
          try {
            String authToken = request.headers("Authorization");
            return gson.toJson(gameService.listGames(authToken));
          } catch (ResponseException e) {
            System.err.println("Error listing games: " + e.getMessage());
            response.status(e.getStatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
          } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse(e.getMessage()));
          }
        });

    // Creates a new game.
    Spark.post(
        "/game",
        (request, response) -> {
          try {
            String authToken = request.headers("Authorization");
            GameData gameData = gson.fromJson(request.body(), GameData.class);
            return gson.toJson(gameService.createGame(authToken, gameData));
          } catch (ResponseException e) {
            System.err.println("Error creating game: " + e.getMessage());
            response.status(e.getStatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
          } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse(e.getMessage()));
          }
        });

    // Join a game. Verifies that the specified game exists, and, if a color is
    // specified, adds the caller as the requested color to the game. If no
    // color is specified the user is joined as an observer. This request is
    // idempotent.
    Spark.put(
        "/game",
        (request, response) -> {
          try {
            String authToken = request.headers("Authorization");
            JoinGameRequest joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            return gson.toJson(gameService.joinGame(authToken, joinGameRequest));
          } catch (ResponseException e) {
            System.err.println("Error joining game: " + e.getMessage());
            response.status(e.getStatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
          } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse(e.getMessage()));
          }
        });

    Spark.awaitInitialization();
    return Spark.port();
  }

  public void stop() {
    Spark.stop();
    Spark.awaitStop();
  }

  public void clear() throws ResponseException {
    dataService.clearData();
  }
}
