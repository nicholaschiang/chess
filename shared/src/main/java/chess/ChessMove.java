package chess;

/**
 * Represents moving a chess piece on a chessboard
 *
 * <p>Note: You can add to this class, but you may not alter signature of the existing methods.
 */
public class ChessMove {
  private ChessPosition startPosition;
  private ChessPosition endPosition;
  private ChessPiece.PieceType promotionPiece;

  @Override
  public boolean equals(Object other) {
    if (other instanceof ChessMove) {
      ChessMove otherMove = (ChessMove) other;
      return (startPosition.equals(otherMove.startPosition)
          && endPosition.equals(otherMove.endPosition)
          && promotionPiece == otherMove.promotionPiece);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + startPosition.hashCode();
    hash = 31 * hash + endPosition.hashCode();
    if (promotionPiece != null) {
      hash = 31 * hash + promotionPiece.hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    var move = String.format("%s -> %s", startPosition, endPosition);
    if (promotionPiece != null) {
      move += String.format(" (%s)", promotionPiece);
    }
    return move;
  }

  public ChessMove(ChessPosition startPosition, ChessPosition endPosition) {
    this.startPosition = startPosition;
    this.endPosition = endPosition;
    this.promotionPiece = null;
  }

  public ChessMove(
      ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
    this.startPosition = startPosition;
    this.endPosition = endPosition;
    this.promotionPiece = promotionPiece;
  }

  /**
   * @return ChessPosition of starting location
   */
  public ChessPosition getStartPosition() {
    return this.startPosition;
  }

  /**
   * @return ChessPosition of ending location
   */
  public ChessPosition getEndPosition() {
    return this.endPosition;
  }

  /**
   * Gets the type of piece to promote a pawn to if pawn promotion is part of this chess move
   *
   * @return Type of piece to promote a pawn to, or null if no promotion
   */
  public ChessPiece.PieceType getPromotionPiece() {
    return this.promotionPiece;
  }
}
