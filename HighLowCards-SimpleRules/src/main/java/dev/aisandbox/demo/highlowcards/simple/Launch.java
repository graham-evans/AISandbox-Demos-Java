package dev.aisandbox.demo.highlowcards.simple;

import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsAction;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsReward;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowCardsState;
import dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

import static dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice.HIGH;
import static dev.aisandbox.server.simulation.highlowcards.proto.HighLowChoice.LOW;

/**
 * Play the High Low game based on a few simple rules
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
                    // look at the current card and pick high/low
                    HighLowChoice choice = switch (state.getDealtCardList().getLast().charAt(0)) {
                        case 'A', '2', '3', '4', '5', '6' -> HIGH;
                        case '8', '9', '1', 'J', 'Q', 'K' -> LOW;
                        default -> random.nextBoolean() ? HIGH : LOW;
                    };
                    // format this as an action
                    HighLowCardsAction action = HighLowCardsAction.newBuilder()
                            .setAction(choice)
                            .build();
                    System.out.println("Sending action: " + choice);
                    // send this to the server
                    action.writeDelimitedTo(outputStream);
                    // read the reward
                    HighLowCardsReward reward = HighLowCardsReward.parseDelimitedFrom(inputStream);
                    System.out.println("Score: "+reward.getScore());
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
