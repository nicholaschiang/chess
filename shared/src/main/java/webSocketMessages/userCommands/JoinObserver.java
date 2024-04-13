package webSocketMessages.userCommands;

public class JoinObserver extends UserGameCommand {
  protected final CommandType commandType = CommandType.JOIN_OBSERVER;
  private final int gameId;

  public JoinObserver(String authToken, int gameId) {
    super(authToken);
    this.gameId = gameId;
  }

  public int getGameId() {
    return gameId;
  }
}
