package webSocketMessages.userCommands;

public class Leave extends UserGameCommand {
  private final int gameId;

  public Leave(String authToken, int gameId) {
    super(authToken);
    this.gameId = gameId;
    this.commandType = CommandType.LEAVE;
  }

  public int getGameId() {
    return gameId;
  }
}
