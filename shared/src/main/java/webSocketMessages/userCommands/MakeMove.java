package webSocketMessages.userCommands;

import chess.ChessMove;

public class MakeMove extends UserGameCommand {
  protected final CommandType commandType = CommandType.MAKE_MOVE;
  private final int gameId;
  private final ChessMove move;

  public MakeMove(String authToken, int gameId, ChessMove move) {
    super(authToken);
    this.gameId = gameId;
    this.move = move;
  }

  public int getGameId() {
    return gameId;
  }

  public ChessMove getMove() {
    return move;
  }
}
