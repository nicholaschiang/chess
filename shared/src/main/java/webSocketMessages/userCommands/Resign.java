package webSocketMessages.userCommands;

public class Resign extends UserGameCommand {
  protected final CommandType commandType = CommandType.RESIGN;
  private final int gameId;

  public Resign(String authToken, int gameId) {
    super(authToken);
    this.gameId = gameId;
  }

  public int getGameId() {
    return gameId;
  }
}
