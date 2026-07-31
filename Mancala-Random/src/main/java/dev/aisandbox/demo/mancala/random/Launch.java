package dev.aisandbox.demo.mancala.random;


import dev.aisandbox.server.simulation.mancala.proto.MancalaAction;
import dev.aisandbox.server.simulation.mancala.proto.MancalaResult;
import dev.aisandbox.server.simulation.mancala.proto.MancalaState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Play Mancala using random choices
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
        MancalaState state = MancalaState.parseDelimitedFrom(inputStream);
        if (state != null) {
          // randomly pick one of the valid moves
          int target = state.getValidMoves(random.nextInt(state.getValidMovesCount()));
          // write state and target to screen
          System.out.println(
              "Got state with valid moves " + state.getValidMovesList() + ", chosing pit #"
                  + target);
          MancalaAction action = MancalaAction.newBuilder().setSelectedPit(target).build();
          // send this to the server
          action.writeDelimitedTo(outputStream);
          outputStream.flush();
          // read the result
          MancalaResult result = MancalaResult.parseDelimitedFrom(inputStream);
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
