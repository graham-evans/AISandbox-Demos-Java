# AISandbox-Demos-Java

Java client demos for the [AI Sandbox Server](https://github.com/graham-evans/AISandbox-Server). Each demo is a
standalone Gradle module that connects to a running sandbox server over TCP, plays one simulation using
delimited Protobuf messages, and prints its progress to the console.

## Demos

| Module                     | Simulation                                       | Strategy                                                          |
|-----------------------------|---------------------------------------------------|--------------------------------------------------------------------|
| `Bandit-Random`             | Multi-armed bandit                                 | Picks a random arm each round                                      |
| `CoinGame-Random`           | Coin game (Nim-style pile game)                    | Picks a random non-empty pile and removes a random number of coins |
| `HighLowCards-Random`       | High/low card guessing                             | Guesses high or low at random                                      |
| `HighLowCards-SimpleRules`  | High/low card guessing                             | Guesses based on the current card's rank, falling back to random   |
| `Maze-Random`               | Maze navigation                                    | Moves in a random direction each step                              |
| `Mine-Random`                | Minesweeper                                        | Digs or flags a random cell each step                              |
| `Twisty-Random`             | Twisty puzzle (e.g. Rubik's-cube style)            | Applies a random valid move each step                              |

## Requirements

- JDK 25
- A running instance of the AI Sandbox Server (defaults to `localhost:9000`)

## Running a demo

Each module has an `application` plugin configured with its own main class, so it can be run directly with
the Gradle wrapper from the repository root:

```commandline
./gradlew :Bandit-Random:run
```

By default the client connects to `localhost:9000`. To connect elsewhere, pass arguments through Gradle:

```commandline
# connect to a specific port on localhost
./gradlew :Bandit-Random:run --args="9001"

# connect to a specific host and port
./gradlew :Bandit-Random:run --args="192.168.1.10 9000"
```

Substitute the module name (e.g. `:Maze-Random`, `:Twisty-Random`) for whichever demo you want to run.

## Protobuf definitions

Each module keeps its own copy of the `.proto` file it needs under `src/main/proto`. These are compiled to
Java sources automatically as part of the Gradle build via the `com.google.protobuf` plugin.
