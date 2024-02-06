package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 *
 * <p>Note: You can add to this class, but you may not alter signature of the existing methods.
 */
public class ChessBoard {
  private ChessPiece[][] board;

  @Override
  public boolean equals(Object other) {
    if (other instanceof ChessBoard) {
      ChessBoard otherBoard = (ChessBoard) other;
      for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {
          ChessPiece thisPiece = board[row][col];
          ChessPiece otherPiece = otherBoard.board[row][col];
          if (thisPiece == null && otherPiece == null) {
            continue;
          } else if (thisPiece == null || otherPiece == null) {
            return false;
          } else if (!thisPiece.equals(otherPiece)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    for (ChessPiece[] row : board) {
      for (ChessPiece piece : row) {
        hash = 31 * hash + piece.hashCode();
      }
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (ChessPiece[] row : board) {
      for (ChessPiece piece : row) {
        if (piece == null) {
          builder.append(" ");
        } else {
          builder.append(piece);
        }
        builder.append("|");
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  public ChessBoard() {
    board = new ChessPiece[8][8];
  }

  /**
   * Adds a chess piece to the chessboard
   *
   * @param position where to add the piece to
   * @param piece the piece to add
   */
  public void addPiece(ChessPosition position, ChessPiece piece) {
    board[position.getRow() - 1][position.getColumn() - 1] = piece;
  }

  /**
   * Moves a chess piece across the chessboard
   *
   * @param move the move to preform
   */
  public void movePiece(ChessMove move) {
    ChessPosition start = move.getStartPosition();
    ChessPosition end = move.getEndPosition();
    var piece = board[start.getRow() - 1][start.getColumn() - 1];
    var promotionPieceType = move.getPromotionPiece();
    if (promotionPieceType != null) piece.setPieceType(promotionPieceType);
    board[end.getRow() - 1][end.getColumn() - 1] = piece;
    board[start.getRow() - 1][start.getColumn() - 1] = null;
  }

  /**
   * Gets a chess piece on the chessboard
   *
   * @param position The position to get the piece from
   * @return Either the piece at the position, or null if no piece is at that position
   */
  public ChessPiece getPiece(ChessPosition position) {
    return board[position.getRow() - 1][position.getColumn() - 1];
  }

  /**
   * Checks if a position is within the board Used to ensure that we don't try to move a piece off
   * the board
   *
   * @param position The position to check
   * @return true if the position is within the board, false otherwise
   */
  public static boolean isPositionWithinBoard(ChessPosition position) {
    boolean positionWithinBoard =
        (position.getRow() >= 1
            && position.getRow() <= 8
            && position.getColumn() >= 1
            && position.getColumn() <= 8);
    System.out.println(
        String.format("Position %s is within board: %s", position, positionWithinBoard));
    return positionWithinBoard;
  }

  /**
   * Sets the board to the default starting board (How the game of chess normally starts)
   *
   * <p>|r|n|b|q|k|b|n|r| |p|p|p|p|p|p|p|p| | | | | | | | | | | | | | | | | | | | | | | | | | | | |
   * | | | | | | | | |P|P|P|P|P|P|P|P| |R|N|B|Q|K|B|N|R|
   */
  public void resetBoard() {
    board = new ChessPiece[8][8];
    ChessPiece[] black = {
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK),
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT),
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP),
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN),
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING),
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP),
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT),
      new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK),
    };
    ChessPiece[] white = {
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK),
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT),
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP),
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN),
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING),
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP),
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT),
      new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK),
    };
    board[0] = white;
    board[1] = pawnRow(ChessGame.TeamColor.WHITE);
    board[7] = black;
    board[6] = pawnRow(ChessGame.TeamColor.BLACK);
  }

  /**
   * Fill an array with eight pawns of the given color
   *
   * @param color The color of the pawns
   */
  private ChessPiece[] pawnRow(ChessGame.TeamColor color) {
    ChessPiece[] row = new ChessPiece[8];
    for (int i = 0; i < 8; i++) {
      row[i] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
    }
    return row;
  }
}
