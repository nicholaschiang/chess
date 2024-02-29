package model;

import chess.ChessGame;

public class GameData {
  private int gameId;
  private String whiteUsername;
  private String blackUsername;
  private String gameName;
  private ChessGame game;

  public GameData(
      int gameId, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    this.gameId = gameId;
    this.whiteUsername = whiteUsername;
    this.blackUsername = blackUsername;
    this.gameName = gameName;
    this.game = game;
  }

  public int getGameId() {
    return gameId;
  }

  public String getWhiteUsername() {
    return whiteUsername;
  }

  public String getBlackUsername() {
    return blackUsername;
  }

  public String getGameName() {
    return gameName;
  }

  public ChessGame getGame() {
    return game;
  }

  public void setGame(ChessGame game) {
    this.game = game;
  }
}
