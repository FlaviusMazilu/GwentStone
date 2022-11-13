package main;
import checker.Checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import checker.CheckerConstants;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import static java.lang.System.exit;
import static java.util.Collections.min;
import static java.util.Collections.shuffle;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Input inputData = objectMapper.readValue(new File(CheckerConstants.TESTS_PATH + filePath1),
                Input.class);

        ArrayNode output = objectMapper.createArrayNode();

        //TODO add here the entry point to your implementation

        Player playerOne = new Player(inputData.getPlayerOneDecks());
        Player playerTwo = new Player(inputData.getPlayerTwoDecks());
        int gameIdx = 0;
        for (GameInput currentGame : inputData.getGames()) {
            Game gameEnv = new Game(inputData, gameIdx, playerOne, playerTwo);

            ArrayList<ActionsInput> actionsInp = currentGame.getActions();

            for (ActionsInput iteratorAction : actionsInp) {
                ObjectNode printResult = executeAction(iteratorAction, gameEnv, objectMapper);
                if (printResult.size() != 0)
                    output.add(printResult);
            }
            gameIdx++;
        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }

    static private ObjectNode executeAction(ActionsInput iteratorAction, Game gameEnv, ObjectMapper objectMapper) throws JsonProcessingException {

        ObjectNode resultForPrint = objectMapper.createObjectNode();
        int playerIndex = iteratorAction.getPlayerIdx();
        int handIndex = iteratorAction.getHandIdx();
        int rowIndex = iteratorAction.getAffectedRow();

        if (iteratorAction.getCommand().compareTo("getPlayerTurn") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.put("output", gameEnv.getPlayerTurn());
        }

        if (iteratorAction.getCommand().compareTo("getPlayerHero") == 0) {
            Hero hero = gameEnv.getPlayer(playerIndex).getPlayerHero();
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.put("playerIdx", playerIndex);
            resultForPrint.putPOJO("output", hero);
        }
        if (iteratorAction.getCommand().compareTo("getPlayerDeck") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.put("playerIdx", playerIndex);
            resultForPrint.putPOJO("output", new ArrayList<Card>(gameEnv.getPlayer(playerIndex).getChosenDeck()));
        }
        if (iteratorAction.getCommand().compareTo("placeCard") == 0) {
            String error = gameEnv.placeCard(handIndex);
            if (error != null) {
                resultForPrint.put("command", iteratorAction.getCommand());
                resultForPrint.put("handIdx", handIndex);
                resultForPrint.put("error", error);
            }
        }
        if (iteratorAction.getCommand().compareTo("endPlayerTurn") == 0)
            gameEnv.endPlayerTurn();

        if (iteratorAction.getCommand().compareTo("getCardsInHand") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.put("playerIdx", playerIndex);
            resultForPrint.putPOJO("output", new ArrayList<Card>(gameEnv.getCardsInHand(playerIndex)));
        }
        if (iteratorAction.getCommand().compareTo("getCardsOnTable") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.putPOJO("output", gameEnv.getCardsOnTable());
        }
        if (iteratorAction.getCommand().compareTo("getPlayerMana") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.put("playerIdx", playerIndex);
            resultForPrint.put("output", gameEnv.getPlayer(playerIndex).getMana());
        }
        if (iteratorAction.getCommand().compareTo("useEnvironmentCard") == 0) {
            String error = gameEnv.useEnvironmentCard(handIndex, rowIndex);
            if (error != null) {
                resultForPrint.put("command", iteratorAction.getCommand());
                resultForPrint.put("handIdx", handIndex);
                resultForPrint.put("affectedRow", rowIndex);
                resultForPrint.put("error", error);
            }
        }
        if (iteratorAction.getCommand().compareTo("getEnvironmentCardsInHand") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.put("playerIdx", playerIndex);
            resultForPrint.putPOJO("output", gameEnv.getPlayer(playerIndex).getEnvironmentCardsInHand());
        }
        if (iteratorAction.getCommand().compareTo("getCardAtPosition") == 0) {
            int x = iteratorAction.getX();
            int y = iteratorAction.getY();

            resultForPrint.put("command", iteratorAction.getCommand());
            Card cardAtPos = gameEnv.getTable().getCardAtPosition(x,y);
            if (cardAtPos != null) {
                resultForPrint.putPOJO("output", new Minion((Minion)cardAtPos));
                resultForPrint.put("x", x);
                resultForPrint.put("y", y);
            } else {
                resultForPrint.put("error", "No card at position");
            }
        }
        if (iteratorAction.getCommand().compareTo("getFrozenCardsOnTable") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.putPOJO("output", gameEnv.getTable().getFrozenCardsOnTable());
        }
//        if (iteratorAction.getCommand().compareTo(""))
        return resultForPrint;
    }
}
class Table {
    private Minion[][] table;
//    private boolean[][] freezeStatus;

    public Table() {
        table = new Minion[4][5];
//        freezeStatus = new boolean[4][5];
    }

    public Minion getEntry(int i, int j) {
        return table[i][j];
    }
    public void setEntry(int i, int j, Minion card) {
        table[i][j] = card;
    }
//    public boolean getFreezeStatus(int i, int j) {
//        return freezeStatus[i][j];
//    }

    public ArrayList<ArrayList<Card>> printTable() {
        ArrayList<ArrayList<Card>>cardsOnTable = new ArrayList<>();
        for (int i = 0 ; i < 4; i++) {
            cardsOnTable.add(new ArrayList<Card>());
            for (int j = 0; j < 5; j++) {
                if (table[i][j] != null)
                    cardsOnTable.get(i).add(table[i][j]);
            }
        }
        return cardsOnTable;
    }

    public Card removeCard(int i, int j) {
        Card aux = table[i][j];
        table[i][j] = null;

        //shift them to the left
        for (int k = j; k < 4; k++) {
            table[i][k] = table[i][k + 1];
        }
        for (int k = 4; k >=0; k--) {
            if (table[i][k] != null) {
                table[i][k] = null;
                break;
            }
        }
        return aux;
    }
    public Card getCardAtPosition(int x, int y) {
        return table[x][y];
    }

    public ArrayList<Card> getFrozenCardsOnTable() {
        ArrayList<Card> frozenCardsList = new ArrayList<>();
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 5; j ++) {
                if (table[i][j] != null && table[i][j].findFreezeStatus())
                    frozenCardsList.add(table[i][j]);
            }
        }
        return frozenCardsList;
    }
}
class Game {


    private Player playerOne;
    private Table table = new Table();
    private Player playerTwo;
    private int seed;
    private int deckIndexPlayerOne;
    private int deckIndexPlayerTwo;
    private int playerTurn;
    private int nrRound = 1;
    private int playerStarting;

    private int affectedRow;

    public Game(Input inputData, int gameIdx, Player playerOne, Player playerTwo) {//StartGameInput gameInp, Player playerOne, Player playerTwo) {
        StartGameInput gameInp = inputData.getGames().get(gameIdx).getStartGame();
        seed = gameInp.getShuffleSeed();
        deckIndexPlayerOne = gameInp.getPlayerOneDeckIdx();
        deckIndexPlayerTwo = gameInp.getPlayerTwoDeckIdx();

        playerTurn = gameInp.getStartingPlayer();
        playerStarting = playerTurn;

        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        playerOne.setMana(1);
        playerTwo.setMana(1);

        playerOne.setChosenDeck(inputData, 1, gameInp.getPlayerOneDeckIdx(), seed);
        playerTwo.setChosenDeck(inputData, 2, gameInp.getPlayerTwoDeckIdx(), seed);

        playerOne.setPlayerHero(new Hero(gameInp.getPlayerOneHero()));
        playerTwo.setPlayerHero(new Hero(gameInp.getPlayerTwoHero()));

        //initiates first round
        drawNewCard();

    }

    public Card getElem(int i, int j) {
        return table.getEntry(i,j);
    }
    public int getSeed() {
        return seed;
    }
    public int getDeckIndexPlayerOne() {
        return deckIndexPlayerOne;
    }
    public int getDeckIndexPlayerTwo() {
        return deckIndexPlayerTwo;
    }
    public int getPlayerTurn() {
        return playerTurn;
    }

//    "endPlayerTurn"
    public void endPlayerTurn() {
        unfreezePlayerCards(playerTurn);

        playerTurn = ((playerTurn == 1) ? 2 : 1);
        if (playerTurn == playerStarting) {
            // it starts a new round
            nrRound++;
            startNewRound();
        }
    }
    private void unfreezePlayerCards(int indexPlayer) {
        int i,j;
        if (indexPlayer == 2) {
            i = 0;
            j = 2;
        } else {
            i = 2;
            j = 4;
        }
        for (; i < 2; i++) {
            for (; j < 5; j++) {
                if (table.getEntry(i,j) != null)
                    table.getEntry(i,j).freeze(false);
            }
        }
    }
    private void startNewRound() {
        int aux = playerOne.getMana();
        playerOne.setMana(aux + Math.min(nrRound, 10));

        aux = playerTwo.getMana();
        playerTwo.setMana(aux + Math.min(nrRound, 10));
        //both of them get in hand first card available in their deck
        drawNewCard();

    }

    private void drawNewCard() {
        if (!playerOne.getChosenDeck().isEmpty())
            playerOne.addCardInHand(playerOne.getChosenDeck().remove(0));
        else
            System.out.println("empty deck 1 tried to draw card");
        if (!playerTwo.getChosenDeck().isEmpty())
            playerTwo.addCardInHand(playerTwo.getChosenDeck().remove(0));
        else
            System.out.println("empty deck 2 tried to draw card");

    }

    public Player getPlayer(int index) {
        if (index == 1)
            return playerOne;
        else if (index == 2)
            return playerTwo;
        System.out.println("Wrong player index");
        return null;
    }

    public String placeCard(int handIndex) {
        Player player = getPlayer(playerTurn);
        ArrayList<Card> handCards = player.getCardsInHand();
        Card card;
        if (handIndex >= handCards.size())
            return "ke pasa zoro";
        card = handCards.get(handIndex);
        if (card instanceof Environment) {
            return "Cannot place environment card on table.";
        }
        if (player.getMana() < card.getMana()) {
            return "Not enough mana to place card on table.";
        }
        if (cardsOnRowCounter(((Minion)card).findSittingRow(), playerTurn) == 5) {
            return "Cannot place card on table since row is full.";
        }
        card = handCards.remove(handIndex); // removes the card from the hand
        placeCardOnRow((Minion)card, playerTurn);
        int aux = player.getMana();
        player.setMana(aux - card.getMana());
        //it's no error
        return null;
    }
    public int cardsOnRowCounter(char row, int indexPlayer) {
        int i = findNeededRow(indexPlayer, row);
        
        if (i == -1) {
            System.out.println("checkIfRowFull error");
            exit(100);
        }
        
        int count = 0;
        for (int j = 0; j < 5; j++) {
            if (table.getEntry(i, j) != null)
                count++;
        }
        return count;
    }

    public void placeCardOnRow(Minion card, int playerIdx) {
        int i = findNeededRow(playerIdx, card.findSittingRow());
        int j = 0;
        for (j = 0; j < 5; j++) {
            if (table.getEntry(i,j) == null)
                break;
        }
        table.setEntry(i, j, card);
    }

    //finds the row according to the type of card(sits Front or Back) and to the player index
    private int findNeededRow(int playerIdx, char rowPos) {
        if (playerIdx == 2 && rowPos == 'F')
            return 1;
        if (playerIdx == 2 && rowPos == 'B')
            return 0;
        if (playerIdx == 1 && rowPos == 'F')
            return 2;
        if (playerIdx == 1 && rowPos == 'B')
            return 3;
        return -1;
    }
    //TODO
//    public boolean checkIfCanAttackRow() {
//
//    }

    public ArrayList<ArrayList<Card>> getCardsOnTable() {
        return table.printTable();
    }
    public Table getTable() {
        return table;
    }
    public ArrayList<Card> getCardsInHand(int playerIdx) {
            return getPlayer(playerIdx).getCardsInHand();
    }

    public String useEnvironmentCard(int handIdx, int affectedRow) {
        this.affectedRow = affectedRow;
        Player player = getPlayer(playerTurn);
        Card card = getPlayer(playerTurn).getCardsInHand().get(handIdx);
        if (card instanceof Environment) {
            if (player.getMana() < card.getMana())
                return "Not enough mana to use environment card.";
            if (playerTurn == 2 && (affectedRow == 0 || affectedRow == 1))
                return "Chosen row does not belong to the enemy.";
            if (playerTurn == 1 && (affectedRow == 2 || affectedRow == 3))
                return "Chosen row does not belong to the enemy.";

            String error = ((Environment)card).useAbility(this);
            if (error == null) {
                player.setMana(player.getMana() - card.getMana());
                getPlayer(playerTurn).getCardsInHand().remove(handIdx); // deleting card after use
            }
            for (int j = 0; j < 5; j++) {
                if (table.getEntry(affectedRow,j) != null)
                    if (table.getEntry(affectedRow,j).getHealth() == 0)
                        table.removeCard(affectedRow, j);
            }
            //there is no error and the card was used
            return error;
        } else
            return "Chosen card is not of type environment.";
    }
    public int getAffectedRow() {
        return affectedRow;
    }

    public void setAffectedRow(int affectedRow) {
        this.affectedRow = affectedRow;
    }

//    public ArrayList<Card> getEnvironmentCardsInHand(int playerIdx) {
//        ArrayList<Card> cardsInHand = getCardsInHand(playerIdx);
//        ArrayList<Card> environmentCards = new ArrayList<>();
//        for (Card card : cardsInHand) {
//            if (card instanceof Environment)
//                environmentCards.add(card);
//        }
//        return environmentCards;
//    }
}

class Player {
    private int score;
    private Hero playerHero;
    private ArrayList<Card> cardsInHand;
    private ArrayList<Card> chosenDeck;
    private ArrayList<ArrayList<Card>> allDecks;
    private int nrDecks;
    private int nrCardsInDeck;
    private int mana;

    public Player(DecksInput decksInp) {
        nrDecks = decksInp.getNrDecks();
        nrCardsInDeck = decksInp.getNrCardsInDeck();
        createDecks(decksInp.getDecks());
        nrCardsInDeck = decksInp.getNrCardsInDeck();
        cardsInHand = new ArrayList<>();
    }

    private void createDecks(ArrayList<ArrayList<CardInput>> decksInp) {
        // allocation of the list for every deck
        allDecks = new ArrayList<>();
        for (int i = 0; i < nrDecks; i++) {
            ArrayList<Card> auxDeck = new ArrayList<>();
            allDecks.add(auxDeck);
        }
        int i = 0;
        // implementation of creating a card based on which type of it, the card is
        for (ArrayList<CardInput> currentDeckInp : decksInp) {
            for (CardInput currentCardInp : currentDeckInp) {
                Card newCard = createTypeOfCard(currentCardInp);
                if (newCard == null) {
                    System.out.println("Invalid card name\n");
                    exit(69);
                }
                allDecks.get(i).add(newCard);
            }
            i++;
        }
    }
    private Card createTypeOfCard(CardInput cardInput) {
        String name = cardInput.getName();
        if (name.compareTo("The Ripper") == 0)
            return new TheRipper(cardInput, 'F'); //FRONT_ROWer
        if (name.compareTo("Miraj") == 0)
            return new Miraj(cardInput, 'F');
        if (name.compareTo("Goliath") == 0 || name.compareTo("Warden") == 0)
            return new Tank(cardInput, 'F');
        if (name.compareTo("Sentinel") == 0 || name.compareTo("Berserker") == 0)
            return new Minion(cardInput, 'B');
        if (name.compareTo("The Cursed One") == 0)
            return new TheCursedOne(cardInput, 'B');
        if (name.compareTo("Disciple") == 0)
            return new Disciple(cardInput, 'B');
        if (name.compareTo("Firestorm") == 0)
            return new Firestorm(cardInput);
        if (name.compareTo("Winterfell") == 0)
            return new Winterfell(cardInput);
        if (name.compareTo("Heart Hound") == 0)
            return new HeartHound(cardInput);
        if (name.compareTo("Lord Royce") == 0)
            return new LordRoyce(cardInput);
        if (name.compareTo("Empress Thorina") == 0)
            return new EmpressThorina(cardInput);
        if (name.compareTo("King Mudface") == 0)
            return new KingMudface(cardInput);
        if (name.compareTo("General Kocioraw") == 0)
            return new GeneralKociraw(cardInput);

        return null;
    }

    public Hero getPlayerHero() {
        return playerHero;
    }

    public void setPlayerHero(Hero playerHero) {
        this.playerHero = playerHero;
    }
    public int getScor() {
        return score;
    }

    public void setScor(int scor) {
        this.score = scor;
    }

    public ArrayList<Card> getCardsInHand() {
        return cardsInHand;
    }

    public void addCardInHand(Card newCard) {
        cardsInHand.add(newCard);
    }

    public ArrayList<Card> getChosenDeck() {
        return chosenDeck;
    }

    public void setChosenDeck(Input inputData, int playerIdx, int deckIdx, int seed) {
        //TODO modify back index and mana
        this.chosenDeck = new ArrayList<Card>();

        DecksInput deckInp;
        if (playerIdx == 1)
            deckInp = inputData.getPlayerOneDecks();
        else
            deckInp = inputData.getPlayerTwoDecks();

        for (CardInput currentCardInp : deckInp.getDecks().get(deckIdx)) {
            Card newCard = createTypeOfCard(currentCardInp);
            chosenDeck.add(newCard);
        }
//        chosenDeck.get(0).setMana(99);
//        System.out.println(chosenDeck.get(0).getMana() + " " + allDecks.get(0).get(0).getMana());
        Collections.shuffle(chosenDeck, new Random(seed));
    }
    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public ArrayList<Card> getEnvironmentCardsInHand() {
        ArrayList<Card> environmentCards = new ArrayList<>();
        for (Card card : cardsInHand) {
            if (card instanceof Environment)
                environmentCards.add(card);
        }
        return environmentCards;
    }
}

abstract class Environment extends Card implements SpecialAbility {
    public Environment(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public void attack() {
    }

    public String useAbility(Game gameEnv) {
        return null;
    }

}

class Minion extends Card {

    private int health;
    private int attackDamage;
    private char sittingRow;
    private boolean freezeStatus;

    public Minion(CardInput cardInp, char row) {
        super(cardInp);
        attackDamage = cardInp.getAttackDamage();
        health = cardInp.getHealth();
        sittingRow = row;
    }
    public Minion(Minion minion) {
        health = minion.getHealth();
        attackDamage = minion.getAttackDamage();
        sittingRow = minion.findSittingRow();
        freezeStatus = minion.findFreezeStatus();
        super.setName(minion.getName());
        super.setDescription(minion.getDescription());
        super.setColors(minion.getColors());
        super.setMana(minion.getMana());
    }
    public Minion() {

    }

    public void attack () {

    }
    public char findSittingRow() {
        return sittingRow;
    }
    public void freeze(boolean status) {
        freezeStatus = status;
    }
    public boolean findFreezeStatus() {
        return freezeStatus;
    }
    public int getAttackDamage() {
        return attackDamage;
    }
    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }
    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }
}

class Hero extends Card {
    private int health;
    public Hero(CardInput cardInp) {
        super(cardInp);
        health = 30;
    }
    @Override
    public void attack() {

    }
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

}

abstract class Card {
    private int mana;
    private String description;
    private ArrayList<String> colors;
    private String name;

    public Card(CardInput cardInp) { // shallow copy
        this.name = cardInp.getName();
        this.colors = cardInp.getColors();
        this.description = cardInp.getDescription();
//        this.health = cardInp.getHealth();
        this.mana = cardInp.getMana();
//        this.frozen = false;
    }
    public Card() {

    }

    abstract public void attack();

//    public void useAbility() {
//        System.out.println("You don't have an ability peasant");
//    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMana() {
        return mana;
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void setColors(ArrayList<String> colors) {
        this.colors = colors;
    }
//    public void freezeCard(boolean value) {
//        frozen = value;
//    }
//    public boolean isFrozen() {
//        return frozen;
//    }

}

interface SpecialAbility {
    //return value = error
    public String useAbility(Game gameEnv);
}
class Miraj extends Minion {
    Miraj(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Game gameEnv) {

    }
}

class TheRipper extends Minion {
    TheRipper(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Game gameEnv) {

    }
}

class Disciple extends Minion {
    Disciple(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Game gameEnv) {

    }
}

class TheCursedOne extends Minion {
    TheCursedOne(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Game gameEnv) {

    }
}

class Tank extends Minion { // wrapper class for Goliath & Warden & other tanks if any
    Tank(CardInput cardIn, char row) {
        super(cardIn, row);
    }
}

class Firestorm extends Environment {

    public Firestorm(CardInput cardInput) {
        super(cardInput);
    }
    @Override
    public String useAbility(Game gameEnv) {
        int affectedRow = gameEnv.getAffectedRow();
        int playerTurn = gameEnv.getPlayerTurn();
        Player player = gameEnv.getPlayer(playerTurn);

        for (int j = 0; j < 5; j++) {
            Minion affectedCard = (Minion)gameEnv.getElem(affectedRow,j);
            if (affectedCard != null) {
                int aux = affectedCard.getHealth();
                affectedCard.setHealth(aux - 1);
            }
        }
        return null;
    }

}

class Winterfell extends Environment {

    public Winterfell(CardInput cardInput) {
        super(cardInput);
    }

    @Override
    public String useAbility(Game gameEnv) {
        int affectedRow = gameEnv.getAffectedRow();

        for (int j = 0; j < 5; j++)
            if (gameEnv.getTable().getEntry(affectedRow,j) != null)
                gameEnv.getTable().getEntry(affectedRow,j).freeze(true);
//            gameEnv.getTable().setFreezeStatus(affectedRow, j, true);

        return null;
    }
}

class HeartHound extends Environment{

    public HeartHound(CardInput cardInput) {
        super(cardInput);
    }
    @Override
    public String useAbility(Game gameEnv) {
        int playerIdx = gameEnv.getPlayerTurn();
        int affectedRow = gameEnv.getAffectedRow();
        int maxHealth = -1;
        int affectedColumn = 0;

        for (int j = 0; j < 5; j++) {
            Minion itrCard = (Minion)gameEnv.getElem(affectedRow, j);
            if (itrCard != null && itrCard.getHealth() > maxHealth) {
                maxHealth = itrCard.getHealth();
                affectedColumn = j;
            }
        }
        // stolenCard = the card that was stolen from the other player
        Minion stolenCard = (Minion)gameEnv.getElem(affectedRow, affectedColumn);

        // if the number of cards on current player is already full, return error
        // stolenCard.getSittingRow returns the position (Front, Back) that the card is supposed to sit
        if (gameEnv.cardsOnRowCounter(stolenCard.findSittingRow(), playerIdx) == 5)
            return "Cannot steal enemy card since the player's row is full.";

        stolenCard = (Minion)gameEnv.getTable().removeCard(affectedRow, affectedColumn);
        gameEnv.placeCardOnRow(stolenCard, playerIdx);
        //return no error
        return null;

    }

}

class LordRoyce extends Hero implements SpecialAbility {

    public LordRoyce(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public String useAbility(Game gameEnv) {
        return null;
    }
}

class EmpressThorina extends Hero implements SpecialAbility {

    public EmpressThorina(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public String useAbility(Game gameEnv) {
        return null;
    }
}

class KingMudface extends Hero implements SpecialAbility {

    public KingMudface(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public String useAbility(Game gameEnv) {
        return null;
    }
}

class GeneralKociraw extends Hero implements SpecialAbility {

    public GeneralKociraw(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public String useAbility(Game gameEnv) {
        return null;
    }
}