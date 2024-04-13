package webSocketMessages.userCommands;

import chess.ChessGame.TeamColor;

public class JoinPlayer extends UserGameCommand {
  private final int gameId;
  private final TeamColor playerColor;

  public JoinPlayer(String authToken, int gameId, TeamColor playerColor) {
    super(authToken);
    this.gameId = gameId;
    this.playerColor = playerColor;
    this.commandType = CommandType.JOIN_PLAYER;
  }

  public int getGameId() {
    return gameId;
  }

  public TeamColor getPlayerColor() {
    return playerColor;
  }
}
