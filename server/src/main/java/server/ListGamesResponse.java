package server;

import model.GameData;

public class ListGamesResponse {
  private GameData[] games;

  public ListGamesResponse(GameData[] games) {
    this.games = games;
  }

  public GameData[] getGames() {
    return games;
  }
}
