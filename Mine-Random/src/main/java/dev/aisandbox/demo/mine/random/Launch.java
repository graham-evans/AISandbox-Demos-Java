package dev.aisandbox.demo.mine.random;


import dev.aisandbox.server.simulation.mine.proto.FlagAction;
import dev.aisandbox.server.simulation.mine.proto.MineAction;
import dev.aisandbox.server.simulation.mine.proto.MineResult;
import dev.aisandbox.server.simulation.mine.proto.MineState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Clear a mine picking random moves
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
        MineState state = MineState.parseDelimitedFrom(inputStream);
        if (state != null) {
          // write current card to screen
          System.out.println("Got state, there are " + state.getFlagsLeft() + " to place");
          // choose a random action
          MineAction action = MineAction.newBuilder().setX(random.nextInt(state.getWidth()))
              .setY(random.nextInt(state.getHeight()))
              .setAction(random.nextBoolean() ? FlagAction.DIG : FlagAction.PLACE_FLAG).build();
          System.out.println(
              "trying to " + action.getAction().name() + " at (" + action.getX() + ","
                  + action.getY() + ")");
          // send this to the server
          action.writeDelimitedTo(outputStream);
          // read the reward
          MineResult result = MineResult.parseDelimitedFrom(inputStream);
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
