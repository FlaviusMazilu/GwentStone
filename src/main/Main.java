package main;

import checker.Checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import checker.CheckerConstants;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.*;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
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
import static java.util.Random.*;

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

        for (GameInput currentGame : inputData.getGames()) {
            Game gameEnv = new Game(currentGame.getStartGame(), playerOne, playerTwo);
//            gameEnv.setGameStart(currentGame);


            ArrayList<ActionsInput> actionsInp = currentGame.getActions();

            for (ActionsInput iteratorAction : actionsInp) {
                ObjectNode printResult = executeAction(iteratorAction, gameEnv, objectMapper);
                if (printResult.size() != 0)
                    output.add(printResult);
            }

        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }

    static private ObjectNode executeAction(ActionsInput iteratorAction, Game gameEnv, ObjectMapper objectMapper) throws JsonProcessingException {

        ObjectNode resultForPrint = objectMapper.createObjectNode();
        int playerIndex = iteratorAction.getPlayerIdx();
        int handIndex = iteratorAction.getHandIdx();


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
                resultForPrint.put("handIdx", 0);
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
        return resultForPrint;
    }
}
class Table {
    private Card[][] table;
    private boolean[][] freezeStatus;

    public Table() {
        table = new Card[4][5];
        freezeStatus = new boolean[4][5];
    }

    public Card getEntry(int i, int j) {
        return table[i][j];
    }
    public void setEntry(int i, int j, Card card) {
        table[i][j] = card;
    }
    public boolean getFreezeStatus(int i, int j) {
        return freezeStatus[i][j];
    }
    public void setFreezeStatus(int i, int j, boolean status) {
        freezeStatus[i][j] = status;
    }

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


    public Game(StartGameInput gameInp, Player playerOne, Player playerTwo) {
        seed = gameInp.getShuffleSeed();
        deckIndexPlayerOne = gameInp.getPlayerOneDeckIdx();
        deckIndexPlayerTwo = gameInp.getPlayerTwoDeckIdx();

        playerTurn = gameInp.getStartingPlayer();
        playerStarting = playerTurn;

        playerOne.setMana(1);
        playerTwo.setMana(1);

        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        playerOne.setChosenDeck(gameInp.getPlayerOneDeckIdx(), gameInp.getShuffleSeed());
        playerTwo.setChosenDeck(gameInp.getPlayerTwoDeckIdx(), gameInp.getShuffleSeed());

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
                table.setFreezeStatus(i,j, false);
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
    private int cardsOnRowCounter(char row, int indexPlayer) {
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

    private void placeCardOnRow(Minion card, int playerIdx) {
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
//    public boolean checkIfCanAttackRow() {
//
//    }
//    private void shiftToLeft() {
//
//    }
    public ArrayList<ArrayList<Card>> getCardsOnTable() {
        return table.printTable();
    }
    public ArrayList<Card> getCardsInHand(int playerIdx) {
            return getPlayer(playerIdx).getCardsInHand();
    }

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

    public void setChosenDeck(int indexDeck, int seed) {
        this.chosenDeck = new ArrayList<Card>(allDecks.get(indexDeck));

        Collections.shuffle(chosenDeck, new Random(seed));
    }
    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }
}

abstract class Environment extends Card {
    public Environment(CardInput cardInp) {
        super(cardInp);
    }

}

class Minion extends Card {

    private int health;
    private int attackDamage;
    private char sittingRow;


    public Minion(CardInput cardInp, char row) {
        super(cardInp);
        attackDamage = cardInp.getAttackDamage();
        health = cardInp.getHealth();
        sittingRow = row;
    }

    public void attack () {

    }
    public char findSittingRow() {
        return sittingRow;
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
//    public boolean frozen;

    Card(CardInput cardInp) { // shallow copy
        this.name = cardInp.getName();
        this.colors = cardInp.getColors();
        this.description = cardInp.getDescription();
//        this.health = cardInp.getHealth();
        this.mana = cardInp.getMana();
//        this.frozen = false;
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
    public void useAbility();
}
class Miraj extends Minion implements SpecialAbility {
    Miraj(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility() {

    }
}

class TheRipper extends Minion implements SpecialAbility {
    TheRipper(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility() {

    }
}

class Disciple extends Minion implements SpecialAbility {
    Disciple(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility() {

    }
}

class TheCursedOne extends Minion implements SpecialAbility {
    TheCursedOne(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility() {

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
    public void attack() {

    }
}

class Winterfell extends Environment {

    public Winterfell(CardInput cardInput) {
        super(cardInput);
    }
    @Override
    public void attack() {

    }
}

class HeartHound extends Environment {

    public HeartHound(CardInput cardInput) {
        super(cardInput);
    }
    @Override
    public void attack() {

    }
}

class LordRoyce extends Hero implements SpecialAbility {

    public LordRoyce(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public void useAbility() {

    }
}

class EmpressThorina extends Hero implements SpecialAbility {

    public EmpressThorina(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public void useAbility() {

    }
}

class KingMudface extends Hero implements SpecialAbility {

    public KingMudface(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public void useAbility() {

    }
}

class GeneralKociraw extends Hero implements SpecialAbility {

    public GeneralKociraw(CardInput cardInp) {
        super(cardInp);
    }
    @Override
    public void useAbility() {

    }
}