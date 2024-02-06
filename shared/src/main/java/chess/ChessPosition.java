package chess;

/**
 * Represents a single square position on a chess board
 *
 * <p>Note: You can add to this class, but you may not alter signature of the existing methods.
 */
public class ChessPosition {
  private int row;
  private int col;

  @Override
  public boolean equals(Object other) {
    if (other instanceof ChessPosition) {
      ChessPosition otherPosition = (ChessPosition) other;
      return row == otherPosition.row && col == otherPosition.col;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + row;
    hash = 31 * hash + col;
    return hash;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", row, col);
  }

  public ChessPosition(int row, int col) {
    this.row = row;
    this.col = col;
  }

  /**
   * @return which row this position is in 1 codes for the bottom row
   */
  public int getRow() {
    return this.row;
  }

  /**
   * @return which column this position is in 1 codes for the left row
   */
  public int getColumn() {
    return this.col;
  }
}
