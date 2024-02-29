package server;

import chess.ChessGame.TeamColor;

public class JoinGameRequest {
  private TeamColor playerColor;
  private int gameId;

  public JoinGameRequest(TeamColor playerColor, int gameId) {
    this.playerColor = playerColor;
    this.gameId = gameId;
  }

  public TeamColor getPlayerColor() {
    return playerColor;
  }

  public int getGameId() {
    return gameId;
  }
}
