package ui;

import chess.*;
import com.google.gson.Gson;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import model.*;
import server.*;
import webSocketMessages.serverMessages.*;

public class Repl {
  private static final Gson gson = new Gson();
  private ServerFacade serverFacade;
  private AuthData authData;
  private GameData gameData;
  private Scanner scanner;

  public Repl(String serverUrl) {
    this.serverFacade = new ServerFacade(serverUrl, this::onMessage);
    this.scanner = new Scanner(System.in);
  }

  public void onMessage(String message) {
    System.out.println("");
    var serverMessage = gson.fromJson(message, ServerMessage.class);
    switch (serverMessage.getServerMessageType()) {
      case LOAD_GAME:
        {
          var load = gson.fromJson(message, LoadGame.class);
          gameData.setGame(load.getChessGame());
          printGameData();
          break;
        }
      case ERROR:
        {
          var error = gson.fromJson(message, ServerError.class);
          System.err.println(error.getErrorMessage());
          break;
        }
      case NOTIFICATION:
        {
          var notification = gson.fromJson(message, Notification.class);
          System.out.println(notification.getMessage());
          break;
        }
    }
    System.out.print("> ");
  }

  public void run() {
    System.out.println("Welcome to the Chess REPL!");
    System.out.println("Listening at " + this.serverFacade.getServerUrl());
    System.out.println("Type 'help' for a list of commands.");
    while (true) {
      if (gameData != null) printGameData();
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
        case "redraw":
          break;
        case "move":
          makeMove();
          break;
        case "moves":
          highlightLegalMoves();
          break;
        case "leave":
          leaveGame();
          break;
        case "resign":
          resignGame();
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
    } else if (gameData == null) {
      System.out.println("  logout - Log out of " + authData.getUsername());
      System.out.println("  games - List all games");
      System.out.println("  new - Start a new game");
      System.out.println("  join - Join a game");
      System.out.println("  watch - Join a game as an observer");
    } else {
      System.out.println("  redraw - Redraw the board");
      System.out.println("  move - Make a move");
      System.out.println("  moves - Highlight legal moves");
      System.out.println("  leave - Leave the game");
      System.out.println("  resign - Forfeit the game");
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
      gameData = serverFacade.joinGame(authData.getAuthToken(), request);
      System.out.printf("Joined game %d.\n", gameData.getGameId());
    } catch (Exception e) {
      System.out.println("Join game failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private void joinObserver() {
    System.out.print("Enter the game ID you want to watch: ");
    var gameId = Integer.parseInt(scanner.nextLine());
    var request = new JoinGameRequest(null, gameId);
    try {
      gameData = serverFacade.joinGame(authData.getAuthToken(), request);
      System.out.printf("Watching game %d.\n", gameData.getGameId());
    } catch (Exception e) {
      System.out.println("Join game failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private void makeMove() {
    System.out.print("From (row, col): ");
    var from = scanPosition();
    System.out.print("To (row, col): ");
    var to = scanPosition();
    var move = new ChessMove(from, to);
    try {
      serverFacade.makeMove(authData.getAuthToken(), gameData.getGameId(), move);
    } catch (Exception e) {
      System.out.println("Move failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private ChessPosition scanPosition() {
    var position = scanner.nextLine();
    var row = Integer.parseInt(position.split(",")[0]);
    var col = Integer.parseInt(position.split(",")[1]);
    return new ChessPosition(row, col);
  }

  private void highlightLegalMoves() {
    System.out.print("From (row, col): ");
    var from = scanPosition();
    if (gameData.getGame().getBoard().getPiece(from) == null) {
      System.out.println("No piece at that position.");
      return;
    }
    var legalMoves = gameData.getGame().validMoves(from);
    var highlightPositions = new HashSet<ChessPosition>();
    System.out.print("Possible moves: ");
    for (var move : legalMoves) {
      System.out.print(move.getEndPosition() + " ");
      highlightPositions.add(move.getEndPosition());
    }
    System.out.println();
    printGame(gameData.getGame(), isPlayingBlack(), highlightPositions, from);
  }

  private void leaveGame() {
    System.out.printf("Leaving game %d...\n", gameData.getGameId());
    try {
      serverFacade.leaveGame(authData.getAuthToken(), gameData.getGameId());
      gameData = null;
    } catch (Exception e) {
      System.out.println("Leave game failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private void resignGame() {
    System.out.printf("Resigning game %d...\n", gameData.getGameId());
    try {
      serverFacade.resignGame(authData.getAuthToken(), gameData.getGameId());
      gameData = null;
    } catch (Exception e) {
      System.out.println("Resign game failed. Please try again.");
      System.out.println("Detail: " + e.getMessage());
    }
  }

  private boolean isPlayingBlack() {
    return authData.getUsername().equals(gameData.getBlackUsername());
  }

  private void printGameData() {
    System.out.println(
        String.format(
            "Current game: %s (black) v.s. %s (white)",
            gameData.getBlackUsername(), gameData.getWhiteUsername()));
    var turn =
        gameData.getGame().getTeamTurn() == ChessGame.TeamColor.BLACK
            ? gameData.getBlackUsername()
            : gameData.getWhiteUsername();
    var status = String.format("It is %s's turn.", turn);
    if (authData.getUsername().equals(turn)) {
      status += " Please make your move.";
    } else {
      status += " Waiting for your opponent's move...";
    }
    System.out.println(status);
    System.out.println();
    printGame(gameData.getGame(), isPlayingBlack());
  }

  private static void printGame(ChessGame chessGame, boolean printBlackOnBottom) {
    var highlightPositions = new HashSet<ChessPosition>();
    System.out.println(getGameString(chessGame, printBlackOnBottom, highlightPositions, null));
  }

  private static void printGame(
      ChessGame chessGame,
      boolean printBlackOnBottom,
      Collection<ChessPosition> highlightPositions,
      ChessPosition selectedPosition) {
    System.out.println(
        getGameString(chessGame, printBlackOnBottom, highlightPositions, selectedPosition));
  }

  private static String getGameString(
      ChessGame chessGame,
      boolean printBlackOnBottom,
      Collection<ChessPosition> highlightPositions,
      ChessPosition selectedPosition) {
    var builder = new StringBuilder();
    if (printBlackOnBottom) {
      for (var row = 1; row <= 8; row++) {
        if (row == 1) appendIndicesRow(builder, row);
        appendRow(builder, chessGame, highlightPositions, selectedPosition, row);
        if (row == 8) appendIndicesRow(builder, row);
      }
    } else {
      for (var row = 8; row >= 1; row--) {
        if (row == 8) appendIndicesRow(builder, row);
        appendRow(builder, chessGame, highlightPositions, selectedPosition, row);
        if (row == 1) appendIndicesRow(builder, row);
      }
    }
    builder.append(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
    return builder.toString();
  }

  private static void appendRow(
      StringBuilder builder,
      ChessGame chessGame,
      Collection<ChessPosition> highlightPositions,
      ChessPosition selectedPosition,
      int row) {
    for (var col = 1; col <= 8; col++) {
      var rowSymbol = EscapeSequences.RESET_BG_COLOR + " " + row + " ";
      if (col == 1) builder.append(rowSymbol);
      var position = new ChessPosition(row, col);
      var piece = chessGame.getBoard().getPiece(position);
      var symbol = "";
      if (highlightPositions.contains(position)) {
        symbol +=
            row % 2 == col % 2
                ? EscapeSequences.SET_BG_COLOR_GREEN
                : EscapeSequences.SET_BG_COLOR_DARK_GREEN;
      } else if (selectedPosition != null && selectedPosition.equals(position)) {
        symbol += EscapeSequences.SET_BG_COLOR_YELLOW;
      } else {
        symbol +=
            row % 2 == col % 2
                ? EscapeSequences.SET_BG_COLOR_WHITE
                : EscapeSequences.SET_BG_COLOR_BLACK;
      }
      if (piece == null) {
        symbol += EscapeSequences.EMPTY;
      } else {
        symbol +=
            piece.getTeamColor() == ChessGame.TeamColor.BLACK
                ? blackPieces.get(piece.getPieceType())
                : whitePieces.get(piece.getPieceType());
      }
      builder.append(symbol);
      if (col == 8) builder.append(rowSymbol);
    }
    builder.append(EscapeSequences.RESET_BG_COLOR + "\n");
  }

  private static void appendIndicesRow(StringBuilder builder, int row) {
    builder.append(EscapeSequences.RESET_BG_COLOR);
    builder.append(EscapeSequences.EMPTY);
    for (var col = 1; col <= 8; col++) {
      var colSymbol = " " + col + " ";
      builder.append(colSymbol);
    }
    builder.append(EscapeSequences.EMPTY);
    builder.append("\n");
  }

  private static final Map<ChessPiece.PieceType, String> blackPieces =
      Map.of(
          ChessPiece.PieceType.PAWN, EscapeSequences.BLACK_PAWN,
          ChessPiece.PieceType.KNIGHT, EscapeSequences.BLACK_KNIGHT,
          ChessPiece.PieceType.ROOK, EscapeSequences.BLACK_ROOK,
          ChessPiece.PieceType.QUEEN, EscapeSequences.BLACK_QUEEN,
          ChessPiece.PieceType.KING, EscapeSequences.BLACK_KING,
          ChessPiece.PieceType.BISHOP, EscapeSequences.BLACK_BISHOP);

  private static final Map<ChessPiece.PieceType, String> whitePieces =
      Map.of(
          ChessPiece.PieceType.PAWN, EscapeSequences.WHITE_PAWN,
          ChessPiece.PieceType.KNIGHT, EscapeSequences.WHITE_KNIGHT,
          ChessPiece.PieceType.ROOK, EscapeSequences.WHITE_ROOK,
          ChessPiece.PieceType.QUEEN, EscapeSequences.WHITE_QUEEN,
          ChessPiece.PieceType.KING, EscapeSequences.WHITE_KING,
          ChessPiece.PieceType.BISHOP, EscapeSequences.WHITE_BISHOP);
}
