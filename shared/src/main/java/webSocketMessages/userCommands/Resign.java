package webSocketMessages.userCommands;

public class Resign extends UserGameCommand {
  private final int gameId;

  public Resign(String authToken, int gameId) {
    super(authToken);
    this.gameId = gameId;
    this.commandType = CommandType.RESIGN;
  }

  public int getGameId() {
    return gameId;
  }
}
