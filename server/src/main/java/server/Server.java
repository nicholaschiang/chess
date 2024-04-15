package server;

import chess.ChessGame.TeamColor;
import com.google.gson.Gson;
import dataAccess.*;
import exception.ResponseException;
import java.util.HashMap;
import java.util.HashSet;
import model.*;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.*;
import spark.Spark;
import webSocketMessages.serverMessages.*;
import webSocketMessages.userCommands.*;

@WebSocket
public class Server {
  private static final Gson gson = new Gson();
  private UserDataAccess userDataAccess;
  private AuthDataAccess authDataAccess;
  private GameDataAccess gameDataAccess;
  private UserService userService;
  private GameService gameService;
  private DataService dataService;

  // Track all connected sessions by gameId.
  private HashMap<Integer, HashSet<Session>> sessions = new HashMap<Integer, HashSet<Session>>();

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

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws Exception {
    System.out.printf("Received message: %s%n", message);
    var command = gson.fromJson(message, UserGameCommand.class);
    try {
      var authData = authDataAccess.getAuth(command.getAuthString());
      switch (command.getCommandType()) {
        case JOIN_OBSERVER:
          {
            var join = gson.fromJson(message, JoinObserver.class);
            var gameData = gameDataAccess.getGame(join.getGameId());
            var notification = authData.getUsername() + " is now observing your game.";
            addSession(gameData.getGameId(), session);
            send(session, new LoadGame(gameData));
            sendToOthers(session, gameData.getGameId(), new Notification(notification));
            break;
          }
        case JOIN_PLAYER:
          {
            var join = gson.fromJson(message, JoinPlayer.class);
            var gameData = gameDataAccess.getGame(join.getGameId());

            var notification = "";
            if (join.getPlayerColor() == TeamColor.WHITE) {
              notification += gameData.getWhiteUsername();
              notification += " has joined the game playing white!";
            } else {
              notification += gameData.getBlackUsername();
              notification += " has joined the game playing black!";
            }

            addSession(gameData.getGameId(), session);
            send(session, new LoadGame(gameData));
            sendToOthers(session, gameData.getGameId(), new Notification(notification));
            break;
          }
        case MAKE_MOVE:
          {
            var move = gson.fromJson(message, MakeMove.class);
            var gameData = gameDataAccess.getGame(move.getGameId());
            var game = gameData.getGame();

            var notification =
                String.format(
                    "%s moved their %s from %s to %s.",
                    authData.getUsername(),
                    game.getBoard().getPiece(move.getMove().getStartPosition()),
                    move.getMove().getStartPosition(),
                    move.getMove().getEndPosition());

            game.makeMove(move.getMove());
            gameData.setGame(game);
            gameDataAccess.updateGame(gameData.getGameId(), gameData);
            sendToAll(move.getGameId(), new LoadGame(gameData));
            sendToOthers(session, move.getGameId(), new Notification(notification));
            break;
          }
        case LEAVE:
          {
            var leave = gson.fromJson(message, Leave.class);
            var gameData = gameDataAccess.getGame(leave.getGameId());
            if (gameData.getBlackUsername() == authData.getUsername()) {
              gameData.setBlackUsername(null);
            } else if (gameData.getWhiteUsername() == authData.getUsername()) {
              gameData.setWhiteUsername(null);
            }
            var notification = authData.getUsername() + " has left the game.";
            gameDataAccess.updateGame(gameData.getGameId(), gameData);
            removeSession(leave.getGameId(), session);
            sendToOthers(session, leave.getGameId(), new Notification(notification));
            break;
          }
        case RESIGN:
          {
            var resign = gson.fromJson(message, Resign.class);
            var gameData = gameDataAccess.getGame(resign.getGameId());
            var isWhite = authData.getUsername().equals(gameData.getWhiteUsername());
            gameData.getGame().setResigned(isWhite ? TeamColor.WHITE : TeamColor.BLACK);
            gameDataAccess.updateGame(gameData.getGameId(), gameData);
            var notification = authData.getUsername() + " has forfeited the game.";
            sendToAll(resign.getGameId(), new Notification(notification));
            removeSession(resign.getGameId(), session);
            break;
          }
      }
    } catch (Exception e) {
      var errorMessage =
          String.format("Command %s failed: %s", command.getCommandType(), e.getMessage());
      System.err.println(errorMessage);
      System.err.println(e.getStackTrace());
      send(session, new ServerError(errorMessage));
    }
  }

  private void addSession(int gameId, Session session) {
    if (!sessions.containsKey(gameId)) sessions.put(gameId, new HashSet<Session>());
    sessions.get(gameId).add(session);
  }

  private void removeSession(int gameId, Session session) {
    if (sessions.containsKey(gameId)) sessions.get(gameId).remove(session);
  }

  private void send(Session session, ServerMessage message) throws Exception {
    session.getRemote().sendString(gson.toJson(message));
  }

  private void sendToAll(int gameId, ServerMessage message) throws Exception {
    sessions
        .get(gameId)
        .forEach(
            (session) -> {
              try {
                send(session, message);
              } catch (Exception e) {
                System.err.println("Failed to send message to session: " + e.getMessage());
                System.err.println(e.getStackTrace());
              }
            });
  }

  private void sendToOthers(Session skip, int gameId, ServerMessage message) throws Exception {
    sessions
        .get(gameId)
        .forEach(
            (session) -> {
              if (session != skip) {
                try {
                  send(session, message);
                } catch (Exception e) {
                  System.err.println("Failed to send message to session: " + e.getMessage());
                  System.err.println(e.getStackTrace());
                }
              }
            });
  }

  public int run(int desiredPort) {
    Spark.port(desiredPort);

    Spark.staticFiles.location("web");

    // Register endpoints. I have so few that I can put them all here. If this
    // becomes unwieldy, I'll move them to their own classes.
    // https://github.com/softwareconstruction240/softwareconstruction/blob/main/chess/3-web-api/web-api.md#endpoint-specifications

    // Opens a websocket connection.
    Spark.webSocket("/connect", Server.class);

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
