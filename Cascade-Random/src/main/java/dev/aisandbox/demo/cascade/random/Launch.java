package dev.aisandbox.demo.cascade.random;


import dev.aisandbox.server.simulation.cascade.proto.CascadeAction;
import dev.aisandbox.server.simulation.cascade.proto.CascadeResult;
import dev.aisandbox.server.simulation.cascade.proto.CascadeState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Play Cascade using random choices
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
      // don't delay before ack'ing, removes delay with small packets
      clientSocket.setTcpNoDelay(true);
      // create input and output streams
      OutputStream outputStream = clientSocket.getOutputStream();
      InputStream inputStream = clientSocket.getInputStream();
      while (true) {
        // read the simulation state
        CascadeState state = CascadeState.parseDelimitedFrom(inputStream);
        if (state != null) {
          // randomly pick two tile coordinates in the 8x8 grid
          int x1 = random.nextInt(8);
          int y1 = random.nextInt(8);
          int x2 = random.nextInt(8);
          int y2 = random.nextInt(8);
          // write state and action to screen
          System.out.println(
              "Got state with " + state.getMovesRemaining() + " moves remaining, score "
                  + state.getScore() + ", swapping (" + x1 + "," + y1 + ") with (" + x2 + ","
                  + y2 + ")");
          CascadeAction action = CascadeAction.newBuilder().setX1(x1).setY1(y1).setX2(x2)
              .setY2(y2).build();
          // send this to the server
          action.writeDelimitedTo(outputStream);
          outputStream.flush();
          // read the result
          CascadeResult result = CascadeResult.parseDelimitedFrom(inputStream);
          System.out.println("Signal " + result.getSignal());
        } else {
          System.err.println("Server finished - closing connection");
          clientSocket.close();
          System.exit(0);
        }
      }
    } catch (IOException e) {
      System.err.println("Error talking to server");
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
