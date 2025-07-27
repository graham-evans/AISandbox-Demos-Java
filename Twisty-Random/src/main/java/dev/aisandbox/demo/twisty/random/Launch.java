package dev.aisandbox.demo.twisty.random;


import dev.aisandbox.server.simulation.twisty.proto.TwistyAction;
import dev.aisandbox.server.simulation.twisty.proto.TwistyResult;
import dev.aisandbox.server.simulation.twisty.proto.TwistyState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Try and solve a twisty puzzle using random moves
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
        TwistyState state = TwistyState.parseDelimitedFrom(inputStream);
        if (state != null) {
          // write current card to screen
          System.out.println("Got state, " + state.getState());
          // choose a random action
          TwistyAction action = TwistyAction.newBuilder()
              .setMove(state.getValidMoves(random.nextInt(state.getValidMovesCount()))).build();
          System.out.println("trying move " + action.getMove());
          // send this to the server
          action.writeDelimitedTo(outputStream);
          // read the reward
          TwistyResult result = TwistyResult.parseDelimitedFrom(inputStream);
          System.out.println("Result is " + result.getSignal().name());
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
