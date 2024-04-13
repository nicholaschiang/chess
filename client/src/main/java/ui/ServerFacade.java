package ui;

import chess.ChessMove;
import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import javax.websocket.*;
import model.*;
import server.*;
import webSocketMessages.userCommands.*;

public class ServerFacade extends Endpoint {
  private static final Gson gson = new Gson();
  private final String serverUrl;
  private Session session;
  private final Consumer<String> onMessage;

  public ServerFacade(String serverUrl, Consumer<String> onMessage) {
    this.serverUrl = serverUrl;
    this.onMessage = onMessage;
  }

  public String getServerUrl() {
    return this.serverUrl;
  }

  public AuthData registerUser(UserData user) {
    var authData = fetch("POST", "/user", gson.toJson(user), null);
    return gson.fromJson(authData, AuthData.class);
  }

  public AuthData loginUser(LoginRequest loginRequest) {
    var authData = fetch("POST", "/session", gson.toJson(loginRequest), null);
    return gson.fromJson(authData, AuthData.class);
  }

  public void logoutUser(String authToken) {
    fetch("DELETE", "/session", "", authToken);
  }

  public ListGamesResponse listGames(String authToken) {
    var games = fetch("GET", "/game", "", authToken);
    return gson.fromJson(games, ListGamesResponse.class);
  }

  public GameData createGame(String authToken, GameData gameData) {
    var game = fetch("POST", "/game", gson.toJson(gameData), authToken);
    return gson.fromJson(game, GameData.class);
  }

  public GameData joinGame(String authToken, JoinGameRequest joinGameRequest) throws Exception {
    var game = fetch("PUT", "/game", gson.toJson(joinGameRequest), authToken);
    var gameData = gson.fromJson(game, GameData.class);
    this.connect();
    if (joinGameRequest.getPlayerColor() == null) {
      this.send(new JoinObserver(authToken, gameData.getGameId()));
    } else {
      this.send(new JoinPlayer(authToken, gameData.getGameId(), joinGameRequest.getPlayerColor()));
    }
    System.out.println("Joined game: " + gameData.getGameId());
    return gameData;
  }

  public void leaveGame(String authToken, int gameId) throws Exception {
    this.send(new Leave(authToken, gameId));
  }

  public void resignGame(String authToken, int gameId) throws Exception {
    this.send(new Resign(authToken, gameId));
  }

  public void makeMove(String authToken, int gameId, ChessMove move) throws Exception {
    this.send(new MakeMove(authToken, gameId, move));
  }

  private void connect() throws Exception {
    var uri = new URI(serverUrl.replace("http:", "ws:") + "/connect");
    System.out.println("Connecting to " + uri);
    var container = ContainerProvider.getWebSocketContainer();
    session = container.connectToServer(this, uri);
    session.addMessageHandler(
        new MessageHandler.Whole<String>() {
          public void onMessage(String message) {
            onMessage.accept(message);
          }
        });
  }

  public void onOpen(Session session, EndpointConfig config) {
    System.out.println("Connected to websocket server.");
  }

  private void send(UserGameCommand command) throws Exception {
    var json = gson.toJson(command);
    System.out.println("Sending command: " + json);
    session.getBasicRemote().sendText(json);
  }

  private InputStreamReader fetch(String method, String path, String body, String authToken) {
    try {
      var http = sendRequest(method, serverUrl + path, body, authToken);
      var response = receiveResponse(http);
      return response;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static HttpURLConnection sendRequest(
      String method, String url, String body, String authToken)
      throws URISyntaxException, IOException {
    System.out.println("Sending " + method + " request to " + url);
    URI uri = new URI(url);
    HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
    if (authToken != null) http.setRequestProperty("Authorization", authToken);
    http.setRequestMethod(method);
    writeRequestBody(body, http);
    http.connect();
    return http;
  }

  private static void writeRequestBody(String body, HttpURLConnection http) throws IOException {
    if (!body.isEmpty()) {
      http.setDoOutput(true);
      try (var outputStream = http.getOutputStream()) {
        outputStream.write(body.getBytes());
      }
    }
  }

  private static InputStreamReader receiveResponse(HttpURLConnection http) throws IOException {
    InputStream responseBody = http.getInputStream();
    InputStreamReader inputStreamReader = new InputStreamReader(responseBody);
    return inputStreamReader;
  }
}
