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
// @formatter:on
import static java.lang.System.exit;
import static java.util.Collections.*;

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
     *
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
            ArrayList<Card> list = new ArrayList<Card>();
            for (Card card : gameEnv.getCardsInHand(playerIndex)) {
                Minion auxCard;
                //ROMANEASCA FACUTA --> to change
                if (card instanceof Minion) {
                    auxCard = new Minion((Minion) card);
                    list.add(auxCard);
                } else {
                    list.add(card);
                }
            }
            resultForPrint.putPOJO("output", list);
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
            Card cardAtPos = gameEnv.getTable().getCardAtPosition(x, y);
            if (cardAtPos != null) {
                resultForPrint.putPOJO("output", new Minion((Minion) cardAtPos));

            } else {
                resultForPrint.put("output", "No card available at that position.");
            }
            resultForPrint.put("x", x);
            resultForPrint.put("y", y);
        }
        if (iteratorAction.getCommand().compareTo("getFrozenCardsOnTable") == 0) {
            resultForPrint.put("command", iteratorAction.getCommand());
            resultForPrint.putPOJO("output", gameEnv.getTable().getFrozenCardsOnTable());
        }
        if (iteratorAction.getCommand().compareTo("cardUsesAttack") == 0) {
            String error = gameEnv.cardUsesAttack(iteratorAction);
            if (error != null) {
                resultForPrint.put("command", iteratorAction.getCommand());
                resultForPrint.putPOJO("cardAttacker", iteratorAction.getCardAttacker());
                resultForPrint.putPOJO("cardAttacked", iteratorAction.getCardAttacked());
                resultForPrint.put("error", error);
            }

        }
        if (iteratorAction.getCommand().compareTo("cardUsesAbility") == 0) {
            String error = gameEnv.cardUsesAbility(iteratorAction);
            if (error != null) {
                resultForPrint.put("command", iteratorAction.getCommand());
                resultForPrint.putPOJO("cardAttacker", iteratorAction.getCardAttacker());
                resultForPrint.putPOJO("cardAttacked", iteratorAction.getCardAttacked());
                resultForPrint.put("error", error);
            }
        }
        return resultForPrint;
    }
}

class Table {
//    private Minion[][] table;
    ArrayList<ArrayList<Minion>> table;
    public Table() {
        table = new ArrayList<>();
        ArrayList<Minion> row;
        for (int j = 0; j < Cons.nrRows; j++) {
             row = new ArrayList<>();
            for (int i = 0; i < Cons.nrCols; i++) {
                row.add(null);
            }
            table.add(row);
        }
    }

    public Minion getEntry(int i, int j) {
        return (table.get(i)).get(j);
    }

    public void setEntry(int i, int j, Minion card) {
        (table.get(i)).set(j, card);
    }

    public ArrayList<ArrayList<Minion>> printTable() {
        ArrayList<ArrayList<Minion>> cardsOnTable = new ArrayList<>();
        for (int i = 0; i < Cons.nrRows; i++) {
            cardsOnTable.add(new ArrayList<>());
        }

        int i = 0;
        for (ArrayList<Minion> row : table) {
            for (Minion minion : row) {
                if (minion != null)
                    cardsOnTable.get(i).add(new Minion(minion));
            }
            i++;
        }
        return cardsOnTable;
    }

    public Minion removeCard(int i, int j) {

        (table.get(i)).add(null);
        System.out.println(table.get(i).size() + " " + getEntry(i,j).getHealth());
        Minion minion = table.get(i).remove(j);
        if (getEntry(i,j) != null)
            System.out.println("-->"+table.get(i).size() + " " + (getEntry(i,j) == minion));

        return minion;

    }

    public Card getCardAtPosition(int x, int y) {
        return getEntry(x, y);
    }

    public ArrayList<Card> getFrozenCardsOnTable() {
        ArrayList<Card> frozenCardsList = new ArrayList<>();
        for (int i = 0; i < Cons.nrRows; i++) {
            for (int j = 0; j < Cons.nrCols; j++) {
                if (getEntry(i,j) != null && getEntry(i, j).findFreezeStatus())
                    frozenCardsList.add(getEntry(i, j));
            }
        }
        return frozenCardsList;
    }

    public boolean existTank(int playerIdx) {
        int i;
        if (playerIdx == 2)
            i = Cons.player2Front;
        else
            i = Cons.player1Front;
        for (int j = 0; j < Cons.nrCols; j++) {
            if (getEntry(i, j) instanceof Tank)
                return true;
        }
        return false;
    }

    public void markCardAbleAttack(int playerIdx) {
        int i, k;
        if (playerIdx == 1) {
            i = Cons.player1Front;
            k = Cons.player1Back;
        } else {
            i = Cons.player2Back;
            k = Cons.player2Front;
        }
        for (; i <= k; i++) {
            for (int j = 0; j < Cons.nrCols; j++)
                if (getEntry(i, j) != null)
                    getEntry(i, j).setAttackUsed(false);
        }
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

    //TODO to remove->redundant
    public Card getElem(int i, int j) {
        return table.getEntry(i, j);
    }

    public int getPlayerTurn() {
        return playerTurn;
    }

    public void endPlayerTurn() {
        unfreezePlayerCards(playerTurn);
        table.markCardAbleAttack(playerTurn);

        playerTurn = ((playerTurn == 1) ? 2 : 1);
        if (playerTurn == playerStarting) {
            // it starts a new round
            nrRound++;
            startNewRound();
        }
    }
    // TODO move func to table
    private void unfreezePlayerCards(int indexPlayer) {
        int i, j, k;
        if (indexPlayer == Cons.player2) {
            i = Cons.player2Back;
            k = Cons.player2Front;
        } else {
            i = Cons.player1Front;
            k = Cons.player1Back;
        }
        for (; i <= k ; i++) {
            for (j = 0; j < 5; j++) {
                if (table.getEntry(i, j) != null)
                    table.getEntry(i, j).freeze(false);
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
        if (cardsOnRowCounter(((Minion) card).findSittingRow(), playerTurn) == 5) {
            return "Cannot place card on table since row is full.";
        }
        card = handCards.remove(handIndex); // removes the card from the hand
        placeCardOnRow((Minion) card, playerTurn);
        int aux = player.getMana();
        player.setMana(aux - card.getMana());
        //it's no error
        return null;
    }
    //TODO move to table
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
    //TODO move to table
    public void placeCardOnRow(Minion card, int playerIdx) {
        int i = findNeededRow(playerIdx, card.findSittingRow());
        int j = 0;
        for (j = 0; j < 5; j++) {
            if (table.getEntry(i, j) == null)
                break;
        }
        table.setEntry(i, j, card);
    }

    //finds the row according to the type of card(sits Front or Back) and to the player index
    //TODO maybe move to table
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

    public ArrayList<ArrayList<Minion>> getCardsOnTable() {
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

            String error = ((Environment) card).useAbility(this);
            if (error == null) {
                player.setMana(player.getMana() - card.getMana());
                getPlayer(playerTurn).getCardsInHand().remove(handIdx); // deleting card after use
                for (int j = 0; j < 5; j++) {
                    if (table.getEntry(affectedRow, j) != null)
                        if (table.getEntry(affectedRow, j).getHealth() <= 0) {
                            table.removeCard(affectedRow, j);
                            j--;
                        }
                }
            }

            //there is no error and the card was used
            return error;
        } else
            return "Chosen card is not of type environment.";
    }

    public int getAffectedRow() {
        return affectedRow;
    }

    public String cardUsesAttack(ActionsInput actionsInput) {

        Minion attacker = getAttacker(actionsInput);
        Minion attacked = getAttacked(actionsInput);

        if (attacker == null || attacked == null)
            return "Not a valid attacker/attacked Card";

        if (!belongToEnemy(actionsInput.getCardAttacked().getX()))
            return "Attacked card does not belong to the enemy.";
        if (attacker.checkIfAttacked())
            return "Attacker card has already attacked this turn.";
        if (attacker.findFreezeStatus())
            return "Attacker card is frozen.";

        int enemyIdx = ((playerTurn == 1) ? 2 : 1);
        // if exist at least one tank and the attacked is not a tank
        if (table.existTank(enemyIdx))
                if (!(attacked instanceof Tank))
            return "Attacked card is not of type 'Tank'.";

        attacker.attack(attacked);
        if (attacked.getHealth() <= 0) {
            int x = actionsInput.getCardAttacked().getX();
            int y = actionsInput.getCardAttacked().getY();
            table.removeCard(x, y);
        }
        return null;
    }
    public String cardUsesAbility(ActionsInput actionsInput) {
//        Minion attacker = getAttacker(actionsInput);
//        Minion attacked = getAttacked(actionsInput);
        int x = actionsInput.getCardAttacker().getX();
        int y = actionsInput.getCardAttacker().getY();
        Minion attacker =  table.getEntry(x, y);
        if (attacker == null)
            return "Not a valid attacker Card attacker" + x + " " + y;

        x = actionsInput.getCardAttacked().getX();
        y = actionsInput.getCardAttacked().getY();
        Minion attacked = table.getEntry(x, y);

        if (attacked == null)
            return "Not a valid attacked Card attacker" + x + " " + y;

        if (attacker.findFreezeStatus())
            return "Attacker card is frozen.";

        if (attacker.checkIfAttacked())
            return "Attacker card has already attacked this turn.";

        if (checkIfHelperCard(attacker)) {
            if (belongToEnemy(actionsInput.getCardAttacked().getX()))
                return "Attacked card does not belong to the current player.";
        } else {
            if (!belongToEnemy(actionsInput.getCardAttacked().getX()))
                return "Attacked card does not belong to the enemy.";
        }
        int enemyIdx = ((playerTurn == 1) ? 2 : 1);
        if (table.existTank(enemyIdx))
            if (!(attacked instanceof Tank))
                return "Attacked card is not of type 'Tank'.";
        attacker.useAbility(attacked);
        attacker.setAttackUsed(true);

        if (attacked.getHealth() <= 0) {
            x = actionsInput.getCardAttacked().getX();
            y = actionsInput.getCardAttacked().getY();
            table.removeCard(x, y);
        }
        if (attacker.getHealth() <= 0) {
            x = actionsInput.getCardAttacker().getX();
            y = actionsInput.getCardAttacker().getY();
            table.removeCard(x, y);
        }

        return null;
    }
    private Minion getAttacker(ActionsInput actionsInput) {
        int x = actionsInput.getCardAttacker().getX();
        int y = actionsInput.getCardAttacker().getY();
        return table.getEntry(x, y);

    }
    private Minion getAttacked(ActionsInput actionsInput) {
        int x = actionsInput.getCardAttacked().getX();
        int y = actionsInput.getCardAttacked().getY();
        return table.getEntry(x, y);
    }
    private boolean checkIfHelperCard(Minion card) {
        if (card instanceof Disciple)
            return true;
        return false;
    }
    private boolean belongToEnemy(int x) {
        int enemyIdx = ((playerTurn == 1) ? 2 : 1);
        if (enemyIdx == Cons.player1 && x != Cons.player1Front && x != Cons.player1Back)
            return false;
        if (enemyIdx == Cons.player2 && x != Cons.player2Back && x != Cons.player2Front)
            return false;
        return true;
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

    public void setChosenDeck(Input inputData, int playerIdx, int deckIdx, int seed) {
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

    public String attack() {
        return null;
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

    private boolean attackUse;

    public boolean checkIfAttacked() {
        return attackUse;
    }
    public void setAttackUsed(boolean status) {
        attackUse = status;
    }
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
        attackUse = minion.attackUse;
        super.setName(minion.getName());
        super.setDescription(minion.getDescription());
        super.setColors(minion.getColors());
        super.setMana(minion.getMana());
    }

    public Minion() {

    }

    public void attack(Minion attacked) {
        attacked.setHealth(attacked.getHealth() - this.getAttackDamage());
        this.attackUse = true;
    }

    public void useAbility(Minion attacked) {

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

    public String attack() {

        return null;
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
        this.mana = cardInp.getMana();
    }

    public Card() {

    }
        // TODO resolve the problem here
//    abstract public String attack();

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


}

interface SpecialAbility {
    //return value = error
    public String useAbility(Game gameEnv);
}

class Miraj extends Minion {
    Miraj(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Minion attacked) {
        int aux = this.getHealth();
        this.setHealth(attacked.getHealth());
        attacked.setHealth(aux);
    }
}

class TheRipper extends Minion {
    TheRipper(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Minion attacked) {
        attacked.setAttackDamage(Math.max(attacked.getAttackDamage() - 2, 0));
    }
}

class Disciple extends Minion {
    Disciple(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Minion attacked) {
        //TODO modify back
        int aux = attacked.getHealth();
        attacked.setHealth(aux + 2);
    }
}

class TheCursedOne extends Minion {
    TheCursedOne(CardInput cardInp, char row) {
        super(cardInp, row);
    }

    public void useAbility(Minion attacked) {
        int aux = attacked.getHealth();
        attacked.setHealth(attacked.getAttackDamage());
        attacked.setAttackDamage(aux);
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

        int aux;
        for (int j = 0; j < 5; j++) {
            Minion affectedCard = (Minion) gameEnv.getElem(affectedRow, j);
            if (affectedCard != null) {
                aux = affectedCard.getHealth();
                //TODO remove this
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
            if (gameEnv.getTable().getEntry(affectedRow, j) != null)
                gameEnv.getTable().getEntry(affectedRow, j).freeze(true);

        return null;
    }
}

class HeartHound extends Environment {

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
            Minion itrCard = (Minion) gameEnv.getElem(affectedRow, j);
            if (itrCard != null && itrCard.getHealth() > maxHealth) {
                maxHealth = itrCard.getHealth();
                affectedColumn = j;
            }
        }
        // stolenCard = the card that was stolen from the other player
        Minion stolenCard = (Minion) gameEnv.getElem(affectedRow, affectedColumn);

        // if the number of cards on current player is already full, return error
        // stolenCard.getSittingRow returns the position (Front, Back) that the card is supposed to sit
        if (gameEnv.cardsOnRowCounter(stolenCard.findSittingRow(), playerIdx) == 5)
            return "Cannot steal enemy card since the player's row is full.";

        stolenCard = (Minion) gameEnv.getTable().removeCard(affectedRow, affectedColumn);
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
// @formatter:off

interface Cons {
    int nrRows = 4;
    int nrCols = 5;
    int player2Back = 0;
    int player2Front = 1;
    int player1Front = 2;
    int player1Back = 3;
    int player1 = 1;
    int player2 = 2;
}
