package dev.aisandbox.demo.highlowcards.random;

import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsReward;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

import static dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice.HIGH;
import static dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice.LOW;

/**
 * Play the High Low game using random choices
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
                HighLowCardsState state = HighLowCardsState.parseDelimitedFrom(inputStream);
                if (state != null) {
                    // write current card to screen
                    System.out.println("Got state with " + state.getDealtCardList().size() + " cards");
                    System.out.println(String.join(":", state.getDealtCardList()));
                    // guess the next card
                    HighLowCardsAction action = HighLowCardsAction.newBuilder()
                            .setAction(random.nextBoolean() ? HIGH : LOW)
                            .build();
                    // send this to the server
                    action.writeDelimitedTo(outputStream);
                    outputStream.flush();
                    // read the result
                    HighLowCardsReward reward = HighLowCardsReward.parseDelimitedFrom(inputStream);
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
