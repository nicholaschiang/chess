package dataAccess;

import model.GameData;

public interface GameDataAccess extends DataAccess {
  // Create a new game.
  public void createGame(GameData game);

  // Retrieve a specified game with the given game ID.
  public GameData getGame(int gameId);

  // Retrieve all games.
  public GameData[] listGames();

  // Updates a chess game. It should replace the chess game string corresponding
  // to a given gameID. This is used when players join a game or when a move is
  // made.
  public void updateGame(int gameId, GameData game);
}
