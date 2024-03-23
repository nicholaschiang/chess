package ui;

import chess.*;
import java.util.Scanner;
import model.*;
import server.*;

public class Repl {
  private ServerFacade serverFacade;
  private AuthData authData;
  private Scanner scanner;

  public Repl(String serverUrl) {
    this.serverFacade = new ServerFacade(serverUrl);
    this.scanner = new Scanner(System.in);
  }

  public void run() {
    System.out.println("Welcome to the Chess REPL!");
    System.out.println("Listening at " + this.serverFacade.getServerUrl());
    System.out.println("Type 'help' for a list of commands.");
    while (true) {
      System.out.print("> ");
      var command = scanner.nextLine();
      switch (command) {
        case "register":
          register();
          break;
        case "login":
          login();
          break;
        case "logout":
          logout();
          break;
        case "games":
          listGames();
          break;
        case "new":
          createGame();
          break;
        case "join":
          joinGame();
          break;
        case "watch":
          joinObserver();
          break;
        case "quit":
          quit();
          break;
        case "help":
          help();
          break;
        default:
          System.out.println("Unknown command. Type 'help' for a list of commands.");
      }
    }
  }

  private void help() {
    System.out.println("Available commands:");
    if (authData == null) {
      System.out.println("  register - Create a new account");
      System.out.println("  login - Log in with an existing account");
    } else {
      System.out.println("  logout - Log out of " + authData.getUsername());
      System.out.println("  games - List all games");
      System.out.println("  new - Start a new game");
      System.out.println("  join - Join a game");
      System.out.println("  watch - Join a game as an observer");
    }
    System.out.println("  quit - Exit the program");
    System.out.println("  help - Show this help message");
  }

  private void quit() {
    System.out.println("Goodbye!");
    System.exit(0);
  }

  private void register() {
    System.out.print("Email: ");
    var email = scanner.nextLine();
    System.out.print("Username: ");
    var username = scanner.nextLine();
    System.out.print("Password: ");
    var password = new String(System.console().readPassword());
    var user = new UserData(username, password, email);
    try {
      authData = serverFacade.registerUser(user);
      System.out.println("Registered as " + authData.getUsername());
    } catch (Exception e) {
      System.out.println("Registration failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private void login() {
    System.out.print("Enter your username: ");
    var username = System.console().readLine();
    System.out.print("Enter your password: ");
    var password = new String(System.console().readPassword());
    System.out.printf("Logging in as %s...\n", username);
    try {
      authData = serverFacade.loginUser(new LoginRequest(username, password));
      System.out.printf("Logged in as %s.\n", authData.getUsername());
    } catch (Exception e) {
      System.out.println("Login failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private void logout() {
    System.out.printf("Logging out %s...\n", authData.getUsername());
    serverFacade.logoutUser(authData.getAuthToken());
    System.out.printf("Logged out %s.\n", authData.getUsername());
    authData = null;
  }

  private void createGame() {
    System.out.print("Name your game: ");
    var gameName = scanner.nextLine();
    var gameData = new GameData(0, null, null, gameName, new ChessGame());
    try {
      var game = serverFacade.createGame(authData.getAuthToken(), gameData);
      System.out.printf("Created game %d.\n", game.getGameId());
    } catch (Exception e) {
      System.out.println("Game creation failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private void listGames() {
    var response = serverFacade.listGames(authData.getAuthToken());
    System.out.println("Games:");
    for (var game : response.getGames()) {
      System.out.printf(
          "  %d: %s (black = %s, white = %s)\n",
          game.getGameId(), game.getGameName(), game.getBlackUsername(), game.getWhiteUsername());
    }
  }

  private void joinGame() {
    System.out.print("Enter the game ID you want to join: ");
    var gameId = Integer.parseInt(scanner.nextLine());
    System.out.print("Which color would you like to be? (white/black): ");
    var color = scanner.nextLine();
    var playerColor = color.equals("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
    var request = new JoinGameRequest(playerColor, gameId);
    try {
      var game = serverFacade.joinGame(authData.getAuthToken(), request);
      System.out.printf("Joined game %d.\n", game.getGameId());
      printGame(game.getGame());
    } catch (Exception e) {
      System.out.println("Join game failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private void joinObserver() {
    System.out.print("Enter the game ID you want to watch: ");
    var gameId = Integer.parseInt(scanner.nextLine());
    var request = new JoinGameRequest(null, gameId);
    var game = serverFacade.joinGame(authData.getAuthToken(), request);
    System.out.printf("Watching game %d.\n", game.getGameId());
    printGame(game.getGame());
  }

  private static void printGame(ChessGame chessGame) {
    System.out.println("Per convention, capital letters indicate black pieces.");
    System.out.println("Similarly, lowercase letters indicate white pieces.");
    System.out.println("Chess game with black on top:");
    System.out.println(chessGame.getBoard().toString());
    System.out.println("Chess game with white on top:");
    System.out.println(chessGame.getBoard().toWhiteString());
  }
}
