package dataAccess;

import java.util.ArrayList;
import model.GameData;

public class MemoryGameDataAccess implements GameDataAccess {
  private ArrayList<GameData> games = new ArrayList<GameData>();

  // Get the game index from the game ID.
  private static int getGameIndexFromID(int gameID) {
    return gameID - 1;
  }

  // Clears all games.
  public void clear() {
    games.clear();
  }

  // Create a new game.
  public void createGame(GameData game) {
    games.add(game);
    game.setGameId(games.size());
  }

  // Retrieve a specified game with the given game ID.
  public GameData getGame(int gameID) {
    var index = getGameIndexFromID(gameID);
    if (index < 0 || index > games.size() - 1) {
      return null;
    }
    return games.get(getGameIndexFromID(gameID));
  }

  // Retrieve all games.
  public GameData[] listGames() {
    return games.toArray(new GameData[0]);
  }

  // Updates a chess game. It should replace the chess game string corresponding
  // to a given gameID. This is used when players join a game or when a move is
  // made.
  public void updateGame(int gameID, GameData game) throws DataAccessException {
    var index = getGameIndexFromID(gameID);
    if (index < 0 || index > games.size() - 1) {
      throw new DataAccessException("game not found");
    }
    games.set(index, game);
  }
}
