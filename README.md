# oi - a card game

## Build and run

```shell
cd front
npm i
cd ..
./gradlew buildFront clean bootJar
java -jar build/libs/oi-0.0.1-SNAPSHOT.jar
```

Start redis.

Access to http://localhost:8080/

## How to play oi

demo: https://oi.moreslowly.jp/

### Objective of the Game

Oi is a Oicho-Kabu like card game. It is a simple card game that uses a standard deck of playing cards. Players compete by calculating the units digit of the sum of their cards. The player with the strongest hand wins the bet.

### Preparation

- 1 to 6 players

### Game Progression

1. **Dealing the Cards**: At the start of the game, each player is dealt two cards. One card is face down (hidden), and the other is face up (visible).

2. **Setting the Bet**: Players place bets if they feel confident about the strength of their hand.

3. **Revealing the Card**: Players who have placed bets will turn over their face-down card, allowing them to see both of their cards.

4. **Calculating Hand Strength**: Add the values of the two cards in your hand. For this calculation, J is 11, Q is 12, and K is 13, while numerical cards hold their face value. Suits do not matter. The strength of a hand is determined by the units digit of the sum of the cards. 9 is the strongest, followed by 8, 7, 6, 5, 4, 3, 2, 1, and 0.

5. **Requesting an Additional Card**: If a player is not satisfied with the strength of their hand, they may request one additional card. In this case, the hand strength is calculated using the units digit of the sum of the three cards.

6. **Computer Player’s Hand**: After the player has determined the strength of their hand, the computer player, acting as the dealer, constructs its hand and determines its strength.

7. **Determining the Winner**: Compare the strength of the player’s hand with the strength of the computer player’s hand. If the player’s hand is stronger, they are paid the amount of the bet.

### Points to Note

- If the units digit of the sum is the same for both players, it is considered a draw.
- Players should carefully consider whether or not to request an additional card. The third card can alter the strength of their hand.
