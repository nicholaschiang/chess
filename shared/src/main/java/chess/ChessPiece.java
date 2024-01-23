package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    @Override
    public boolean equals(Object other) {
        if (other instanceof ChessPiece) {
            ChessPiece otherPiece = (ChessPiece) other;
            return pieceColor == otherPiece.pieceColor && type == otherPiece.type;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + pieceColor.hashCode();
        hash = 31 * hash + type.hashCode();
        return hash;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various directionerent chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Whether a piece can move many squares or only one.
     */
    private boolean isPieceContinuous() {
        return type == ChessPiece.PieceType.QUEEN || type == ChessPiece.PieceType.BISHOP || type == ChessPiece.PieceType.ROOK;
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch (type) {
            case KING:
                return kingMoves(board, myPosition);
            case QUEEN:
                return queenMoves(board, myPosition);
            case BISHOP:
                return bishopMoves(board, myPosition);
            case KNIGHT:
                return knightMoves(board, myPosition);
            case ROOK:
                return rookMoves(board, myPosition);
            case PAWN:
                return pawnMoves(board, myPosition);
            default:
                return new ArrayList<ChessMove>();
        }

    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition[] directions = {
            // Up one row
            new ChessPosition(1, 0),
            // Down one row
            new ChessPosition(-1, 0),
            // Left one column
            new ChessPosition(0, -1),
            // Right one column
            new ChessPosition(0, 1),
            // Up one row, left one column
            new ChessPosition(1, -1),
            // Up one row, right one column
            new ChessPosition(1, 1),
            // Down one row, left one column
            new ChessPosition(-1, -1),
            // Down one row, right one column
            new ChessPosition(-1, 1)
        };
        return generateMoves(directions, board, myPosition);
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition[] directions = {
            // Up two and right one
            new ChessPosition(2, 1),
            // Up one and right two
            new ChessPosition(1, 2),
            // Down one and right two
            new ChessPosition(-1, 2),
            // Down two and right one
            new ChessPosition(-2, 1),
            // Up two and left one
            new ChessPosition(2, -1),
            // Up one and left two
            new ChessPosition(1, -2),
            // Down one and left two
            new ChessPosition(-1, -2),
            // Down two and left one
            new ChessPosition(-2, -1),
        };
        return generateMoves(directions, board, myPosition);
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition[] directions = {
            // Up
            new ChessPosition(1, 0),
            // Down
            new ChessPosition(-1, 0),
            // Left
            new ChessPosition(0, -1),
            // Right
            new ChessPosition(0, 1),
            // Up and left
            new ChessPosition(1, -1),
            // Up and right
            new ChessPosition(1, 1),
            // Down and left
            new ChessPosition(-1, -1),
            // Down and right
            new ChessPosition(-1, 1)
        };
        return generateMoves(directions, board, myPosition);
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition[] directions = {
            // Up and left
            new ChessPosition(1, -1),
            // Up and right
            new ChessPosition(1, 1),
            // Down and left
            new ChessPosition(-1, -1),
            // Down and right
            new ChessPosition(-1, 1)
        };
        return generateMoves(directions, board, myPosition);
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPosition[] directions = {
            // Up
            new ChessPosition(1, 0),
            // Down
            new ChessPosition(-1, 0),
            // Left
            new ChessPosition(0, -1),
            // Right
            new ChessPosition(0, 1)
        };
        return generateMoves(directions, board, myPosition);
    }

    /**
     * The pawn moves method has to be weird, as pawns have unusual rules.
     *
     * A pawn piece moves one square forward, but on its first move, it can move
     * two squares forward. It captures diagonally one square forward.
     *
     * @see {@link https://www.chess.com/terms/chess-pieces}
     *
     * @param board      The current state of the board
     * @param myPosition The current position of the piece
     * @return Collection of valid moves
     */
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        // Pawns can only move forward one row (down for black, up for white)
        ChessPosition[] directions = { new ChessPosition(pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1, 0) };
        var moves = generateMoves(directions, board, myPosition);

        // If it is the pawn's first move, it can move two squares forward
        if (myPosition.getRow() == (pieceColor == ChessGame.TeamColor.WHITE ? 1 : 6)) {
            var twoForward = new ChessPosition(
                myPosition.getRow() + (pieceColor == ChessGame.TeamColor.WHITE ? 2 : -2),
                myPosition.getColumn()
            );
            if (board.getPiece(twoForward) == null)
                moves.add(new ChessMove(myPosition, twoForward));
        }

        // If there is an enemy piece diagonally forward, the pawn can capture it
        var leftDiagonal = new ChessPosition(
            myPosition.getRow() + (pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1),
            myPosition.getColumn() - 1
        );
        var rightDiagonal = new ChessPosition(
            myPosition.getRow() + (pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1),
            myPosition.getColumn() + 1
        );
        if (ChessBoard.isPositionWithinBoard(leftDiagonal)) {
            var piece = board.getPiece(leftDiagonal);
            if (piece != null && piece.getTeamColor() != getTeamColor())
                moves.add(new ChessMove(myPosition, leftDiagonal));
        }
        if (ChessBoard.isPositionWithinBoard(rightDiagonal)) {
            var piece = board.getPiece(rightDiagonal);
            if (piece != null && piece.getTeamColor() != getTeamColor())
                moves.add(new ChessMove(myPosition, rightDiagonal));
        }

        // Perhaps I have to add moves for every possible promotion if one of 
        // the moves included in `moves` is a promotion?

        return moves;
    }

    /**
     * Calculates all the moves that a piece can make given the valid directions
     * it can go (represented as unit vectors), the current state of the board,
     * and the piece's current position.
     *
     * This method checks if a piece is continuous (isPieceContinuous()) and
     * only keeps moving if the piece is continuous. Thus, this method can work
     * for both continuous and non-continuous pieces.
     *
     * @param directions The directions a piece can move in
     * @param board      The current state of the board
     * @param myPosition The current position of the piece
     * @return Collection of valid moves
     */
    private Collection<ChessMove> generateMoves(ChessPosition[] directions, ChessBoard board, ChessPosition myPosition) {
        var moves = new ArrayList<ChessMove>();
        for (var direction : directions) {
            var nextPosition = new ChessPosition(
                myPosition.getRow() + direction.getRow(),
                myPosition.getColumn() + direction.getColumn()
            );
            while (ChessBoard.isPositionWithinBoard(nextPosition)) {
                var piece = board.getPiece(nextPosition);
                if (piece == null) {
                    // If there is no piece at the next position, we can move there
                    moves.add(new ChessMove(myPosition, nextPosition));
                    // Keep moving in the same direction until we hit a piece
                    if (isPieceContinuous()) {
                        nextPosition = new ChessPosition(
                            nextPosition.getRow() + direction.getRow(),
                            nextPosition.getColumn() + direction.getColumn()
                        );
                    }
                } else if (piece.getTeamColor() != getTeamColor()) {
                    // If there is an enemy piece at the next position, we can move there
                    moves.add(new ChessMove(myPosition, nextPosition));
                    // We have to stop moving once we take an enemy piece
                    break;
                } else {
                    // If there is a friendly piece at the next position, we can't move there
                    break;
                }
            }
        }
        return moves;
    }
}
