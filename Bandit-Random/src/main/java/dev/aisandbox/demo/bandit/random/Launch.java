package dev.aisandbox.demo.bandit.random;


import dev.aisandbox.server.simulation.bandit.proto.BanditAction;
import dev.aisandbox.server.simulation.bandit.proto.BanditResult;
import dev.aisandbox.server.simulation.bandit.proto.BanditState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Play the Multi Arm Bandit using random choices
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
        BanditState state = BanditState.parseDelimitedFrom(inputStream);
        if (state != null) {
          // randomly pick a bandit
          int target = random.nextInt(state.getBanditCount());
          // write state and target to screen
          System.out.println(
              "Got state with " + state.getBanditCount() + " bandits, chosing #" + target);
          BanditAction action = BanditAction.newBuilder().setArm(target).build();
          // send this to the server
          action.writeDelimitedTo(outputStream);
          outputStream.flush();
          // read the result
          BanditResult result = BanditResult.parseDelimitedFrom(inputStream);
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
