package ui;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import javax.websocket.*;
import model.*;
import server.*;

public class ServerFacade {
  private final String serverUrl;
  private static final Gson gson = new Gson();
  private Session session;

  public ServerFacade(String serverUrl) {
    this.serverUrl = serverUrl;
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

  public GameData joinGame(String authToken, JoinGameRequest joinGameRequest) {
    var game = fetch("PUT", "/game", gson.toJson(joinGameRequest), authToken);
    return gson.fromJson(game, GameData.class);
  }

  private void connect() throws Exception {
    URI uri = new URI("ws://" + this.serverUrl + "/connect");
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    this.session = container.connectToServer(this, uri);

    this.session.addMessageHandler(
        new MessageHandler.Whole<String>() {
          public void onMessage(String message) {
            System.out.println(message);
          }
        });
  }

  private void send(String message) throws Exception {
    this.session.getBasicRemote().sendText(message);
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
