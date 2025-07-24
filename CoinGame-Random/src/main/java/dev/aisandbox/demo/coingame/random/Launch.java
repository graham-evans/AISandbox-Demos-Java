package dev.aisandbox.demo.coingame.random;

import dev.aisandbox.server.simulation.coingame.proto.CoinGameAction;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameResult;
import dev.aisandbox.server.simulation.coingame.proto.CoinGameState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class Launch {

  private static final Random random = new Random();

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
        CoinGameState state = CoinGameState.parseDelimitedFrom(inputStream);
        if (state != null) {
          // write current state to screen
          System.out.println("Got state with " + state.getCoinCountList() + " coins");
          // convert list into map (only piles with coins left)
          Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
          for (int row = 0; row < state.getCoinCountCount(); row++) {
            if (state.getCoinCount(row) > 0) {
              rowMap.put(row, state.getCoinCount(row));
            }
          }
          // pick a random row with some coins
          List<Entry<Integer, Integer>> entryList = new ArrayList<Entry<Integer, Integer>>(
              rowMap.entrySet());
          Collections.shuffle(entryList, random);
          Map.Entry<Integer, Integer> rowEntry = entryList.getFirst();
          int takeCoins = Math.min(random.nextInt(rowEntry.getValue()) + 1, state.getMaxPick());
          System.out.println("Taking " + takeCoins + " coins from pile " + rowEntry.getKey());
          // convert this to an action
          CoinGameAction action = CoinGameAction.newBuilder().setSelectedRow(rowEntry.getKey())
              .setRemoveCount(takeCoins).build();
          // send this to the server
          action.writeDelimitedTo(outputStream);
          outputStream.flush();
          // read the result
          CoinGameResult result = CoinGameResult.parseDelimitedFrom(inputStream);
          System.out.println("Status: " + result.getStatus());
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
