package server;

import java.util.Collection;
import model.GameData;

public class ListGamesResponse {
  private Collection<GameData> games;

  public ListGamesResponse(Collection<GameData> games) {
    this.games = games;
  }

  public Collection<GameData> getGames() {
    return games;
  }
}
