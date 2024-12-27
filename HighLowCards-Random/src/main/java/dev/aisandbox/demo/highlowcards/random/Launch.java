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
        try {
            // connect to localhost:9000
            System.out.println("Connecting to server on port 9000");
            Socket clientSocket = new Socket("localhost", 9000);
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
            System.exit(1);
        }
    }
}
