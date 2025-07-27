package dev.aisandbox.demo.maze.random;


import dev.aisandbox.server.simulation.maze.proto.Direction;
import dev.aisandbox.server.simulation.maze.proto.MazeAction;
import dev.aisandbox.server.simulation.maze.proto.MazeResult;
import dev.aisandbox.server.simulation.maze.proto.MazeState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Explore a maze picking random moves
 */
public class Launch {

  private static Random random = new Random();

  public static void main(String[] args) {
    // Work out the port to connect to - defaults to localhost:9000
    String host = "localhost";
    int port = 9000;
    if (args.length == 1) {
      port = Integer.parseInt(args[0]);
    }
    if (args.length == 2) {
      host = args[0];
      port = Integer.parseInt(args[1]);
    }
    System.out.println("Connecting to server on " + host + ":" + port);
    try {
      Socket clientSocket = new Socket(host, port);
      // create input and output streams
      OutputStream outputStream = clientSocket.getOutputStream();
      InputStream inputStream = clientSocket.getInputStream();
      while (true) {
        // read the simulation state
        MazeState state = MazeState.parseDelimitedFrom(inputStream);
        if (state != null) {
          // write current card to screen
          System.out.println("Got state, position in maze is (" + state.getStartX()+","+state.getStartY()+")");
          // choose a random direction
          MazeAction action = MazeAction.newBuilder().setDirectionValue(random.nextInt(4)).build();
          System.out.println("trying to move "+action.getDirection().name());
          // send this to the server
          action.writeDelimitedTo(outputStream);
          // read the reward
          MazeResult result = MazeResult.parseDelimitedFrom(inputStream);
          System.out.println("Score for this move "+result.getStepScore()+" total score for this episode "+result.getAccumulatedScore());
        } else {
          System.err.println("Server finished - closing connection");
          clientSocket.close();
          System.exit(0);
        }
      }
    } catch (IOException e) {
      System.err.println("Error talking to server");
      System.exit(1);
    }
  }
}
