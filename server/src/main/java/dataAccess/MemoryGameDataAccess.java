package dataAccess;

import java.util.Map;

import model.GameData;
import chess.ChessGame;

class MemoryGameDataAccess implements GameDataAccess {
  private Map<Integer, GameData> games;

  // Create a new game.
  public void createGame(GameData game) {
    games.put(game.getGameId(), game);
  }

  // Retrieve a specified game with the given game ID.
  public GameData getGame(int gameId) {
    return games.get(gameId);
  }

  // Retrieve all games.
  public GameData[] listGames() {
    return games.values().toArray(new GameData[0]);
  }

  // Updates a chess game. It should replace the chess game string corresponding
  // to a given gameID. This is used when players join a game or when a move is
  // made.
  public void updateGame(int gameId, ChessGame game) {
    games.get(gameId).setGame(game);
  }
}
