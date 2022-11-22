<div align="center"> <h1> Gwentstone </h1></div>

--------

#### About the project: 
- Gwentstone is a **card game** played with two players, which imputs are provided by a json file
and the output of the game is also parsed in a json. It's a combination between Heartstone and Gwent.
---
#### How the game works:
- There are multiple types of cards: Minions, Environement and Hero
- At the start of each game, every player chooses a Hero card, which would be his *fighter*, who kills the other one's
hero, wins the game. 
- At the start of each round, consisting of 2 turns(one for the first player and one for the other) both of the players draw a card
in their hand, from their own deck which was shuffled.
- In order to place down a card on the table, which could be done only for Minion type of cards, the player has to posses enough 
mana for how much that card requires, after placing it, the players mana is decremented.
- In every round, both player receive an increase of mana, the amount being equal to the round number(except if the round is 10+, than it gets
truncated to 10).
- In order to attack the other player's hero, you need to possess enough mana to use a card to attack it, and it has to be not garded by any 'Tank' card.
---
#### How it was implemented:
- There were defined multiple classes for every type of card there is: a **Card** being at the top of the inheritance tree
followed by the 3 types of cards there are: **Hero, Minion, Environment** and from these, for each type of environment & hero card a separate
class in order for a making good use of Java's *polymorphism*. For the Minion types of cards, there was made only a child class of type Tank
for the 2 tanks, 4 for the Minions with special abilities, and defined as just Minion those who are neither tanks neither do possess special abilities.
- Card
  1. Hero
     * LordRoyce
     * EmpressThorina
     * KingMudface
     * GeneralKocioraw
  2. Minion
     * Tank
     * TheRipper
     * Miraj
     * The Cursed One
     * Disciple
  3. Environment
     * Firestorm
     * Winterfell
     * HeartHound

- There was defined a class for the **Game** itself, which has all the commands that can be applied. This class **has**
two players, of specific **Player** class and **a Table** which implements the board where the cards are placed.


#### Feedback:
- Honestly the best written requirement for a homework that I had, the long explanations really helped me understand how the game works and what i had to do
- I love the practicality of it, not just making random classes for the sake of it, but because we had a real purpose.
- A little demanding in the thinking part at the beginning understanding how to make the hierarchy of classes to best suit the needs
and there were so many things to consider that you end up forgetting parts of it. But that's due to the fact that we didn't work on a real project in java before
- Not my finest implementation, I could've done better if I had no mercy for the code that i already wrote and start from scratch a couple of times, but it is what it is.
At the end i figured out more about how i could do the relationship between classes and what kinds of problems there can appear if the relationship chosen is wrong.

<div align="center"><img src="https://tenor.com/view/witcher3-gif-9340436.gif" width="500px"></div>
