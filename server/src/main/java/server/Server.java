package server;

import chess.ChessGame.TeamColor;
import com.google.gson.Gson;
import dataAccess.*;
import model.*;
import service.*;
import spark.*;

public class Server {
  private UserDataAccess userDataAccess = new MemoryUserDataAccess();
  private AuthDataAccess authDataAccess = new MemoryAuthDataAccess();
  private GameDataAccess gameDataAccess = new MemoryGameDataAccess();
  private Gson gson = new Gson();

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
            userDataAccess.clear();
            authDataAccess.clear();
            gameDataAccess.clear();
            return "";
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
            if (user.getUsername() == null || user.getPassword() == null) {
              response.status(400);
              return gson.toJson(new ErrorResponse("bad request"));
            }
            UserData existingUser = userDataAccess.getUser(user.getUsername());
            if (existingUser != null) {
              response.status(403);
              return gson.toJson(new ErrorResponse("already taken"));
            }
            userDataAccess.createUser(user);
            String authToken = AuthService.generateNewToken();
            AuthData authData = new AuthData(user.getUsername(), authToken);
            authDataAccess.createAuth(authData);
            return gson.toJson(authData);
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
            UserData user = userDataAccess.getUser(loginRequest.getUsername());
            if (user == null || !user.getPassword().equals(loginRequest.getPassword())) {
              response.status(401);
              return gson.toJson(new ErrorResponse("unauthorized"));
            }
            String authToken = AuthService.generateNewToken();
            AuthData authData = new AuthData(user.getUsername(), authToken);
            authDataAccess.createAuth(authData);
            return gson.toJson(authData);
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
            if (authDataAccess.getAuth(authToken) == null) {
              response.status(401);
              return gson.toJson(new ErrorResponse("unauthorized"));
            }
            authDataAccess.deleteAuth(authToken);
            return "";
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
            if (authDataAccess.getAuth(authToken) == null) {
              response.status(401);
              return gson.toJson(new ErrorResponse("unauthorized"));
            }
            var data = new ListGamesResponse(gameDataAccess.listGames());
            return gson.toJson(data);
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
            if (authDataAccess.getAuth(authToken) == null) {
              response.status(401);
              return gson.toJson(new ErrorResponse("unauthorized"));
            }
            GameData game = gson.fromJson(request.body(), GameData.class);
            gameDataAccess.createGame(game);
            return gson.toJson(game);
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
            AuthData auth = authDataAccess.getAuth(authToken);
            if (auth == null) {
              response.status(401);
              return gson.toJson(new ErrorResponse("unauthorized"));
            }
            JoinGameRequest joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            GameData game = gameDataAccess.getGame(joinGameRequest.getGameId());
            if (game == null) {
              response.status(400);
              return gson.toJson(new ErrorResponse("bad request"));
            }
            if (joinGameRequest.getPlayerColor() == TeamColor.WHITE) {
              if (game.getWhiteUsername() != null) {
                response.status(403);
                return gson.toJson(new ErrorResponse("already taken"));
              }
              game.setWhiteUsername(auth.getUsername());
            } else if (joinGameRequest.getPlayerColor() == TeamColor.BLACK) {
              if (game.getBlackUsername() != null) {
                response.status(403);
                return gson.toJson(new ErrorResponse("already taken"));
              }
              game.setBlackUsername(auth.getUsername());
            } else if (joinGameRequest.getPlayerColor() == null) {
              // Add observer. Right now, there's nothing in the specification that
              // actually requires this to be done. No "observer" fields required.
            } else {
              response.status(400);
              return gson.toJson(new ErrorResponse("invalid color"));
            }
            gameDataAccess.updateGame(game.getGameId(), game);
            return gson.toJson(game);
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
}
