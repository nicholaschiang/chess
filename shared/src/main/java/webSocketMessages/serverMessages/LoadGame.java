package webSocketMessages.serverMessages;

import chess.ChessGame;

public class LoadGame extends ServerMessage {
  private final ChessGame chessGame;

  public LoadGame(ChessGame chessGame) {
    super(ServerMessageType.LOAD_GAME);
    this.chessGame = chessGame;
  }

  public ChessGame getChessGame() {
    return chessGame;
  }
}
