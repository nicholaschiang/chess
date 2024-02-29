package service;

import chess.ChessGame.TeamColor;
import dataAccess.*;
import model.*;
import server.*;

public class GameService {
  private AuthDataAccess authDataAccess;
  private GameDataAccess gameDataAccess;

  public GameService(AuthDataAccess authDataAccess, GameDataAccess gameDataAccess) {
    this.authDataAccess = authDataAccess;
    this.gameDataAccess = gameDataAccess;
  }

  public ListGamesResponse listGames(String authToken) throws ExceptionWithStatusCode {
    if (authDataAccess.getAuth(authToken) == null) {
      throw new ExceptionWithStatusCode(401, "unauthorized");
    }
    return new ListGamesResponse(gameDataAccess.listGames());
  }

  public GameData createGame(String authToken, GameData gameData) throws ExceptionWithStatusCode {
    if (authDataAccess.getAuth(authToken) == null) {
      throw new ExceptionWithStatusCode(401, "unauthorized");
    }
    gameDataAccess.createGame(gameData);
    return gameData;
  }

  public GameData joinGame(String authToken, JoinGameRequest joinGameRequest)
      throws ExceptionWithStatusCode {
    AuthData auth = authDataAccess.getAuth(authToken);
    if (auth == null) {
      throw new ExceptionWithStatusCode(401, "unauthorized");
    }
    GameData game = gameDataAccess.getGame(joinGameRequest.getGameId());
    if (game == null) {
      throw new ExceptionWithStatusCode(400, "bad request");
    }
    if (joinGameRequest.getPlayerColor() == TeamColor.WHITE) {
      if (game.getWhiteUsername() != null) {
        throw new ExceptionWithStatusCode(403, "already taken");
      }
      game.setWhiteUsername(auth.getUsername());
    } else if (joinGameRequest.getPlayerColor() == TeamColor.BLACK) {
      if (game.getBlackUsername() != null) {
        throw new ExceptionWithStatusCode(403, "already taken");
      }
      game.setBlackUsername(auth.getUsername());
    } else if (joinGameRequest.getPlayerColor() == null) {
      // Add observer. Right now, there's nothing in the specification that
      // actually requires this to be done. No "observer" fields required.
    } else {
      throw new ExceptionWithStatusCode(400, "invalid color");
    }
    try {
      gameDataAccess.updateGame(game.getGameId(), game);
    } catch (DataAccessException e) {
      throw new ExceptionWithStatusCode(500, e.getMessage());
    }
    return game;
  }
}
