package dev.aisandbox.demo.highlowcards.random;

import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsState;
import dev.aisandbox.server.simulation.highlowcards.proto.Signal;

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
                    // does the server want a prediction
                    if (state.getSignal().equals(Signal.PLAY)) {
                        // guess the next card
                        HighLowCardAction action = HighLowCardAction.newBuilder()
                                .setAction(random.nextBoolean() ? HIGH : LOW)
                                .build();
                        // send this to the server
                        action.writeDelimitedTo(outputStream);

                    }
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
