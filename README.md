# Multiplayer Mazewar using a centralized server

Authors:
- Nayeem Zen
- Warren Marivel

## Compiling
To compile run make from the project root directory.
Else you can also execute the following command:

```javac *.java```


## Running the server
```java MazewarServer < port >```

## Running the client
```java Mazewar < server_host > < server_port >```

## Playing the game
- The game will not start unless 4 players are connected.
- Once the game has started, if a player quits the game the game will continue until only one player is left.
- If there is only one player remaining, the game will close.
