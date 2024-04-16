package webSocketMessages.userCommands;

import chess.ChessGame.TeamColor;

public class JoinPlayer extends UserGameCommand {
  private final int gameID;
  private final TeamColor playerColor;

  public JoinPlayer(String authToken, int gameID, TeamColor playerColor) {
    super(authToken);
    this.gameID = gameID;
    this.playerColor = playerColor;
    this.commandType = CommandType.JOIN_PLAYER;
  }

  public int getGameId() {
    return gameID;
  }

  public TeamColor getPlayerColor() {
    return playerColor;
  }
}
