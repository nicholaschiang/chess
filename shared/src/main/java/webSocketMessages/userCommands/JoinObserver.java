package webSocketMessages.userCommands;

public class JoinObserver extends UserGameCommand {
  private final int gameId;

  public JoinObserver(String authToken, int gameId) {
    super(authToken);
    this.gameId = gameId;
    this.commandType = CommandType.JOIN_OBSERVER;
  }

  public int getGameId() {
    return gameId;
  }
}
