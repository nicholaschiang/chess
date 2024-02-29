package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 *
 * <p>Note: You can add to this class, but you may not alter signature of the existing methods.
 */
public class ChessGame {
  private ChessBoard board;
  private TeamColor teamTurn;

  @Override
  public boolean equals(Object other) {
    if (other instanceof ChessGame) {
      ChessGame otherGame = (ChessGame) other;
      return otherGame.board.equals(board) && otherGame.teamTurn.equals(teamTurn);
    } else {
      return false;
    }
  }

  public ChessGame() {
    teamTurn = TeamColor.WHITE;
    board = new ChessBoard();
    board.resetBoard();
  }

  /**
   * Creates a shallow clone of the given chessboard
   *
   * @param board the board to clone
   * @param teamTurn the team whose turn it is
   */
  public ChessGame(ChessBoard board, TeamColor teamTurn) {
    this.board = new ChessBoard(board);
    this.teamTurn = teamTurn;
  }

  /**
   * @return Which team's turn it is
   */
  public TeamColor getTeamTurn() {
    return teamTurn;
  }

  /**
   * Set's which teams turn it is
   *
   * @param team the team whose turn it is
   */
  public void setTeamTurn(TeamColor team) {
    teamTurn = team;
  }

  /** Enum identifying the 2 possible teams in a chess game */
  public enum TeamColor {
    WHITE,
    BLACK
  }

  /**
   * Gets a valid moves for a piece at the given location
   *
   * @param startPosition the piece to get valid moves for
   * @return Set of valid moves for requested piece, or null if no piece at startPosition
   */
  public Collection<ChessMove> validMoves(ChessPosition startPosition) {
    // All the moves that the piece at startPosition can make.
    var piece = board.getPiece(startPosition);
    var allMoves = piece.pieceMoves(board, startPosition);

    // All the moves that do not put the team's king in danger.
    var validMoves = new HashSet<ChessMove>();
    for (var move : allMoves) {
      var tempGame = new ChessGame(board, teamTurn);
      tempGame.getBoard().movePiece(move);
      if (!tempGame.isInCheck(piece.getTeamColor())) validMoves.add(move);
    }

    return validMoves;
  }

  /**
   * Makes a move in a chess game
   *
   * @param move chess move to preform
   * @throws InvalidMoveException if move is invalid
   */
  public void makeMove(ChessMove move) throws InvalidMoveException {
    // A move is illegal if it’s not the corresponding team's turn.
    if (board.getPiece(move.getStartPosition()).getTeamColor() != teamTurn)
      throw new InvalidMoveException("It is not your turn.");

    // A move is illegal if the chess piece cannot move there.
    if (!validMoves(move.getStartPosition()).contains(move))
      throw new InvalidMoveException("This piece cannot make this move.");

    // Otherwise, the move is legal, and the piece is moved.
    board.movePiece(move);
    if (teamTurn == TeamColor.WHITE) {
      teamTurn = TeamColor.BLACK;
    } else {
      teamTurn = TeamColor.WHITE;
    }
  }

  /**
   * Determines if the given team is in check
   *
   * <p>Returns true if the specified team’s King could be captured by an opposing piece.
   *
   * @param teamColor which team to check for check
   * @return True if the specified team is in check
   */
  public boolean isInCheck(TeamColor teamColor) {
    // Find the position of the king.
    ChessPiece king = new ChessPiece(teamColor, ChessPiece.PieceType.KING);
    ChessPosition kingPosition = board.findPiece(king);

    // If there is no king, we cannot be in check.
    if (kingPosition == null) {
      System.out.println(String.format("No %s found in board:\n%s", king, board));
      return false;
    }

    // For every enemy piece on the board, check if it can move to the same
    // position as the king (i.e. capture the king).
    for (int row = 1; row <= 8; row++) {
      for (int col = 1; col <= 8; col++) {
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(position);
        if (piece != null && piece.getTeamColor() != teamColor) {
          for (ChessMove move : piece.pieceMoves(board, position)) {
            if (move.getEndPosition().equals(kingPosition)) {
              return true;
            }
          }
        }
      }
    }

    // No enemy piece can capture the king.
    return false;
  }

  /**
   * Determines if the given team is in checkmate
   *
   * <p>Returns true if the given team has no way to protect their king from being captured.
   *
   * @param teamColor which team to check for checkmate
   * @return True if the specified team is in checkmate
   */
  public boolean isInCheckmate(TeamColor teamColor) {
    // If we are not in check, we cannot be in checkmate.
    if (!isInCheck(teamColor)) return false;

    // For every friendly piece on the board, check if it can move to a position
    // where we would no longer be in check.
    for (int row = 1; row <= 8; row++) {
      for (int col = 1; col <= 8; col++) {
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(position);
        if (piece != null && piece.getTeamColor() == teamColor) {
          for (ChessMove move : piece.pieceMoves(board, position)) {
            ChessGame tempGame = new ChessGame(board, teamTurn);
            tempGame.getBoard().movePiece(move);
            if (!tempGame.isInCheck(teamColor)) {
              return false;
            }
          }
        }
      }
    }

    // No friendly piece can move to a position where we would no longer be in
    // check. Thus, we are in checkmate and have lost the game.
    return true;
  }

  /**
   * Determines if the given team is in stalemate, which here is defined as having no valid moves
   *
   * <p>Returns true if the given team has no legal moves and it is currently that team’s turn.
   *
   * @param teamColor which team to check for stalemate
   * @return True if the specified team is in stalemate, otherwise false
   */
  public boolean isInStalemate(TeamColor teamColor) {
    // For every friendly piece on the board, check if it can move to a
    // position. If any friendly piece can move, we are not in stalemate.
    for (int row = 1; row <= 8; row++) {
      for (int col = 1; col <= 8; col++) {
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(position);
        if (piece != null && piece.getTeamColor() == teamColor) {
          if (!validMoves(position).isEmpty()) {
            return false;
          }
        }
      }
    }

    // No friendly piece can make a move. Thus, we are in stalemate.
    return true;
  }

  /**
   * Sets this game's chessboard with a given board
   *
   * @param board the new board to use
   */
  public void setBoard(ChessBoard board) {
    this.board = board;
  }

  /**
   * Gets the current chessboard
   *
   * @return the chessboard
   */
  public ChessBoard getBoard() {
    return this.board;
  }
}
