package webSocketMessages.userCommands;

public class Leave extends UserGameCommand {
  protected final CommandType commandType = CommandType.LEAVE;
  private final int gameId;

  public Leave(String authToken, int gameId) {
    super(authToken);
    this.gameId = gameId;
  }

  public int getGameId() {
    return gameId;
  }
}
