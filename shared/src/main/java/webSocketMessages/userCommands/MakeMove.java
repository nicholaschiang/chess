package webSocketMessages.userCommands;

import chess.ChessMove;

public class MakeMove extends UserGameCommand {
  private final int gameId;
  private final ChessMove move;

  public MakeMove(String authToken, int gameId, ChessMove move) {
    super(authToken);
    this.gameId = gameId;
    this.move = move;
    this.commandType = CommandType.MAKE_MOVE;
  }

  public int getGameId() {
    return gameId;
  }

  public ChessMove getMove() {
    return move;
  }
}
