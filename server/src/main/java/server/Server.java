package server;

import spark.*;

public class Server {

  public int run(int desiredPort) {
    Spark.port(desiredPort);

    Spark.staticFiles.location("web");

    // Register endpoints. I have so few that I can put them all here. If this
    // becomes unwieldy, I'll move them to their own classes.
    // https://github.com/softwareconstruction240/softwareconstruction/blob/main/chess/3-web-api/web-api.md#endpoint-specifications

    // Clears the database. Removes all users, games, and authTokens.
    Spark.delete("/db", (req, res) -> 204);

    // Register a new user.
    Spark.post("/user", (req, res) -> 201);

    // Logs in an existing user (returns a new authToken).
    Spark.post("/session", (req, res) -> 201);

    // Logs out the user represented by the authToken.
    Spark.delete("/session", (req, res) -> 204);

    // Gives a list of all games.
    Spark.get("/game", (req, res) -> 200);

    // Creates a new game.
    Spark.post("/game", (req, res) -> 201);

    // Join a game. Verifies that the specified game exists, and, if a color is
    // specified, adds the caller as the requested color to the game. If no
    // color is specified the user is joined as an observer. This request is
    // idempotent.
    Spark.put("/game", (req, res) -> 200);

    Spark.awaitInitialization();
    return Spark.port();
  }

  public void stop() {
    Spark.stop();
    Spark.awaitStop();
  }
}
