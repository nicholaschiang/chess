package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board;

    public ChessBoard() {
       resetBoard(); 
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Checks if a position is within the board
     * Used to ensure that we don't try to move a piece off the board
     *
     * @param position The position to check
     * @return true if the position is within the board, false otherwise
     */
    public static boolean isPositionWithinBoard(ChessPosition position) {
        return (
            position.getRow() >= 1 && 
            position.getRow() <= 8 && 
            position.getColumn() >= 1 && 
            position.getColumn() <= 8
        );
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     *
     * |r|n|b|q|k|b|n|r|
     * |p|p|p|p|p|p|p|p|
     * | | | | | | | | |
     * | | | | | | | | |
     * | | | | | | | | |
     * | | | | | | | | |
     * |P|P|P|P|P|P|P|P|
     * |R|N|B|Q|K|B|N|R|
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
