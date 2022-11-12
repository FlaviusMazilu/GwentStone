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
                output.add(printResult);
            }

        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }
//    static void setPlayersVariables(Player playerOne, Player playerTwo, Game gameEnv, GameInput gameInp) {
//        playerOne.setChosenDeck(gameEnv.getDeckIndexPlayerOne(), gameEnv.getSeed());
//        playerTwo.setChosenDeck(gameEnv.getDeckIndexPlayerTwo(), gameEnv.getSeed());
//
//        playerOne.setPlayerHero(new Hero(gameInp.getStartGame().getPlayerOneHero()));
//        playerTwo.setPlayerHero(new Hero(gameInp.getStartGame().getPlayerTwoHero()));
//
//    }

    static private ObjectNode executeAction(ActionsInput iteratorAction, Game gameEnv, ObjectMapper objectMapper) throws JsonProcessingException {

        ObjectNode resultForPrint = objectMapper.createObjectNode();
        int playerIndex = iteratorAction.getPlayerIdx();
        resultForPrint.put("command", iteratorAction.getCommand());

        if (iteratorAction.getCommand().compareTo("getPlayerTurn") == 0) {
            resultForPrint.put("output", gameEnv.getPlayerTurn());
        }

        if (iteratorAction.getCommand().compareTo("getPlayerHero") == 0) {
            Hero hero = gameEnv.getPlayer(playerIndex).getPlayerHero();
            resultForPrint.put("playerIdx", playerIndex);
            resultForPrint.putPOJO("output", hero);
        }
        if (iteratorAction.getCommand().compareTo("getPlayerDeck") == 0) {
            resultForPrint.put("playerIdx", playerIndex);
            resultForPrint.putPOJO("output", gameEnv.getPlayer(playerIndex).getChosenDeck());
        }
        return resultForPrint;
    }
}
class Game {


    private Player playerOne;
    private Player playerTwo;
    private Card[][] table = new Card[4][5];
    private int seed;
    private int deckIndexPlayerOne;
    private int deckIndexPlayerTwo;
    private int playerTurn;



    public Game(StartGameInput gameInp, Player playerOne, Player playerTwo) {
        seed = gameInp.getShuffleSeed();
        deckIndexPlayerOne = gameInp.getPlayerOneDeckIdx();
        deckIndexPlayerTwo = gameInp.getPlayerTwoDeckIdx();
        playerTurn = gameInp.getStartingPlayer();
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        playerOne.setChosenDeck(gameInp.getPlayerOneDeckIdx(), gameInp.getShuffleSeed());
        playerTwo.setChosenDeck(gameInp.getPlayerTwoDeckIdx(), gameInp.getShuffleSeed());

        playerOne.setPlayerHero(new Hero(gameInp.getPlayerOneHero()));
        playerTwo.setPlayerHero(new Hero(gameInp.getPlayerTwoHero()));

        playerOne.addCardInHand(playerOne.getChosenDeck().remove(0));
        playerTwo.addCardInHand(playerTwo.getChosenDeck().remove(0));

    }
    public Card[][] getTable() {
        return table;
    }

    public void setTable(Card[][] table) {
        this.table = table;
    }

    public Card getElem(int i, int j) {
        return table[i][j];
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
    public void endPlayerTurn() { //TODO adaugat mana
        playerTurn = ((playerTurn == 1) ? 2 : 1);
    }
    public Player getPlayer(int index) {
        if (index == 1)
            return playerOne;
        else if (index == 2)
            return playerTwo;
        System.out.println("Wrong player index");
        return null;
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
                String type = TestCondition.typeOfCard(currentCardInp.getName());
                Card newCard;
                if (type.compareTo("Minion") == 0) {
                    newCard = new Minion(currentCardInp);
                } else {
                    if (type.compareTo("Hero") == 0) {
                        newCard = new Hero(currentCardInp);
                    } else {
                        if (type.compareTo("Environment") == 0) {
                            newCard = new Environment(currentCardInp);
                        } else {
                            newCard = null;
                            System.exit(69);
                        }
                    }
                }
                allDecks.get(i).add(newCard);
            }
            i++;
        }


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

//        Random randomer = new Random(seed);
        Collections.shuffle(chosenDeck, new Random(seed));

    }
}

class Environment extends Card {
    public Environment(CardInput cardInp) {
        super(cardInp);
    }
    public void attack (){

    }

}

class Minion extends Card {

    private int health;
    private int attackDamage;



    public Minion(CardInput cardInp) {
        super(cardInp);
        attackDamage = cardInp.getAttackDamage();
        health = cardInp.getHealth();
    }
    public void attack () {

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
    private boolean frozen;

    Card(CardInput cardInp) { // shallow copy
        this.name = cardInp.getName();
        this.colors = cardInp.getColors();
        this.description = cardInp.getDescription();
//        this.health = cardInp.getHealth();
        this.mana = cardInp.getMana();
        this.frozen = false;
    }
    abstract public void attack();

    public void useAbility() {
        System.out.println("You don't have an ability peasant");
    }

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


//    @Override
//    public String toString() {
//        return name + " ";
//    }
}

abstract class TestCondition {
    public static String typeOfCard(String name) {
        if (name.compareTo("The Ripper") == 0 ||
                name.compareTo("Miraj") == 0 ||
                name.compareTo("Goliath") == 0 ||
                name.compareTo("Warden") == 0 ||
                name.compareTo("Sentinel") == 0 ||
                name.compareTo("Berserker") == 0 ||
                name.compareTo("The Cursed One") == 0 ||
                name.compareTo("Disciple") == 0)
            return "Minion";

        if (name.compareTo("Firestorm") == 0 ||
                name.compareTo("Winterfell") == 0 ||
                name.compareTo("Heart Hound") == 0)
            return "Environment";

        if (name.compareTo("Lord Royce") == 0 ||
                name.compareTo("Empress Thorina") == 0 ||
                name.compareTo("King Mudface") == 0||
                name.compareTo("General Kocioraw") == 0)
            return "Hero";

        return "Not a valid name";
    }
}