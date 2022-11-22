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

        Player playerOne = new Player(inputData.getPlayerOneDecks());
        Player playerTwo = new Player(inputData.getPlayerTwoDecks());
        int gameIdx = 0;
        for (GameInput currentGame : inputData.getGames()) {
            Game gameEnv = new Game(inputData, gameIdx, playerOne, playerTwo);

            ArrayList<ActionsInput> actionsInp = currentGame.getActions();
            ObjectNode printResult;
            for (ActionsInput iteratorAction : actionsInp) {
                printResult = executeAction(iteratorAction, gameEnv, objectMapper, gameIdx);
                if (printResult.size() != 0) {
                    output.add(printResult);
                }

            }
            gameIdx++;
        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }

    private static ObjectNode executeAction(final ActionsInput iteratorAction, final Game gameEnv,
                                           final ObjectMapper objectMapper, final int gamesCount) {

        ObjectNode resultForPrint = objectMapper.createObjectNode();
        int playerIndex = iteratorAction.getPlayerIdx();
        int handIndex = iteratorAction.getHandIdx();
        int rowIndex = iteratorAction.getAffectedRow();

        String command = iteratorAction.getCommand();
        String error;
        switch (command) {
            case "getPlayerTurn":
                resultForPrint.put("command", command);
                resultForPrint.put("output", gameEnv.getPlayerTurn());
                break;
            case "getPlayerHero":
                Hero hero = gameEnv.getPlayer(playerIndex).getPlayerHero();
                resultForPrint.put("command", command);
                resultForPrint.put("playerIdx", playerIndex);
                resultForPrint.putPOJO("output", new Hero(hero));
                break;
            case "getPlayerDeck":
                resultForPrint.put("command", command);
                resultForPrint.put("playerIdx", playerIndex);
                resultForPrint.putPOJO("output",
                        new ArrayList<Card>(gameEnv.getPlayer(playerIndex).getChosenDeck()));
                break;
            case "placeCard":
                error = gameEnv.placeCard(handIndex);
                if (error != null) {
                    resultForPrint.put("command", command);
                    resultForPrint.put("handIdx", handIndex);
                    resultForPrint.put("error", error);
                }
                break;
            case "endPlayerTurn":
                gameEnv.endPlayerTurn();
                break;
            case "getCardsInHand":
                resultForPrint.put("command", command);
                resultForPrint.put("playerIdx", playerIndex);
                ArrayList<Card> list = new ArrayList<Card>();
                for (Card card : gameEnv.getCardsInHand(playerIndex)) {
                    Minion auxCard;
                    if (card instanceof Minion) {
                        auxCard = new Minion((Minion) card);
                        list.add(auxCard);
                    } else {
                        list.add(card);
                    }
                }
                resultForPrint.putPOJO("output", list);
                break;
            case "getCardsOnTable":
                resultForPrint.put("command", command);
                resultForPrint.putPOJO("output", gameEnv.getCardsOnTable());
                break;
            case "getPlayerMana":
                resultForPrint.put("command", command);
                resultForPrint.put("playerIdx", playerIndex);
                resultForPrint.put("output", gameEnv.getPlayer(playerIndex).getMana());
                break;
            case "useEnvironmentCard":
                error = gameEnv.useEnvironmentCard(handIndex, rowIndex);
                if (error != null) {
                    resultForPrint.put("command", command);
                    resultForPrint.put("handIdx", handIndex);
                    resultForPrint.put("affectedRow", rowIndex);
                    resultForPrint.put("error", error);
                }
                break;
            case "getEnvironmentCardsInHand":
                resultForPrint.put("command", command);
                resultForPrint.put("playerIdx", playerIndex);
                resultForPrint.putPOJO("output",
                        gameEnv.getPlayer(playerIndex).getEnvironmentCardsInHand());
                break;
            case "getCardAtPosition":
                int x = iteratorAction.getX();
                int y = iteratorAction.getY();

                resultForPrint.put("command", command);
                Card cardAtPos = gameEnv.getTable().getEntry(x, y);
                if (cardAtPos != null) {
                    resultForPrint.putPOJO("output", new Minion((Minion) cardAtPos));

                } else {
                    resultForPrint.put("output", "No card available at that position.");
                }
                resultForPrint.put("x", x);
                resultForPrint.put("y", y);
                break;
            case "getFrozenCardsOnTable":
                resultForPrint.put("command", command);
                resultForPrint.putPOJO("output", gameEnv.getTable().getFrozenCardsOnTable());
                break;
            case "cardUsesAttack":
                error = gameEnv.cardUsesAttack(iteratorAction);
                if (error != null) {
                    resultForPrint.put("command", command);
                    resultForPrint.putPOJO("cardAttacker", iteratorAction.getCardAttacker());
                    resultForPrint.putPOJO("cardAttacked", iteratorAction.getCardAttacked());
                    resultForPrint.put("error", error);
                }
                break;
            case "useAttackHero":
                error = gameEnv.cardUsesAttack(iteratorAction);
                if (error == null) {
                    return resultForPrint;
                }
                if (error.compareTo("Player one killed the enemy hero.") == 0
                        || error.compareTo("Player two killed the enemy hero.") == 0) {
                    resultForPrint.put("gameEnded", error);
                } else {
                    resultForPrint.put("command", command);
                    resultForPrint.putPOJO("cardAttacker", iteratorAction.getCardAttacker());
                    resultForPrint.put("error", error);
                }
                break;
            case "cardUsesAbility":
                error = gameEnv.cardUsesAbility(iteratorAction);
                if (error != null) {
                    resultForPrint.put("command", command);
                    resultForPrint.putPOJO("cardAttacker", iteratorAction.getCardAttacker());
                    resultForPrint.putPOJO("cardAttacked", iteratorAction.getCardAttacked());
                    resultForPrint.put("error", error);
                }
                break;
            case "useHeroAbility":
                error = gameEnv.useHeroAbility(iteratorAction);
                if (error != null) {
                    resultForPrint.put("command", command);
                    resultForPrint.put("affectedRow", iteratorAction.getAffectedRow());
                    resultForPrint.put("error", error);
                }
                break;
            case "getTotalGamesPlayed":
                resultForPrint.put("command", "getTotalGamesPlayed");
                resultForPrint.put("output", gamesCount + 1);
                break;
            case "getPlayerOneWins":
                resultForPrint.put("command", "getPlayerOneWins");
                resultForPrint.put("output", gameEnv.getPlayer(Cons.player1).getWins());
                break;
            case "getPlayerTwoWins":
                resultForPrint.put("command", "getPlayerTwoWins");
                resultForPrint.put("output", gameEnv.getPlayer(Cons.player2).getWins());
                break;
        }
        return resultForPrint;
    }
}

class Table {
    private ArrayList<ArrayList<Minion>> table;
    Table() {
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

    public Minion getEntry(final int i, final int j) {
        return (table.get(i)).get(j);
    }

    public void setEntry(final int i, final int j, final Minion card) {
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
                if (minion != null) {
                    cardsOnTable.get(i).add(new Minion(minion));
                }
            }
            i++;
        }
        return cardsOnTable;
    }

    public Minion removeCard(final int i, final int j) {

        (table.get(i)).add(null);
        return table.get(i).remove(j);
    }

    public ArrayList<Card> getFrozenCardsOnTable() {
        ArrayList<Card> frozenCardsList = new ArrayList<>();
        for (int i = 0; i < Cons.nrRows; i++) {
            for (int j = 0; j < Cons.nrCols; j++) {
                if (getEntry(i, j) != null && getEntry(i, j).findFreezeStatus()) {
                    frozenCardsList.add(getEntry(i, j));
                }
            }
        }
        return frozenCardsList;
    }

    public boolean existTank(final int playerIdx) {
        int i;
        if (playerIdx == 2) {
            i = Cons.player2Front;
        } else {
            i = Cons.player1Front;
        }
        for (int j = 0; j < Cons.nrCols; j++) {
            if (getEntry(i, j) instanceof Tank) {
                return true;
            }
        }
        return false;
    }

    public void markCardAbleAttack(final int playerIdx) {
        int i, k;
        if (playerIdx == 1) {
            i = Cons.player1Front;
            k = Cons.player1Back;
        } else {
            i = Cons.player2Back;
            k = Cons.player2Front;
        }
        for (; i <= k; i++) {
            for (int j = 0; j < Cons.nrCols; j++) {
                if (getEntry(i, j) != null) {
                    getEntry(i, j).setAttackUsed(false);
                }
            }
        }
    }

    public void removeIfHealthZero(final int row) {
        for (Minion card : table.get(row)) {
            if (card != null && card.getHealth() <= 0) {
                table.get(row).add(null);
                table.get(row).remove(card);
            }
        }
    }
    public void unfreezePlayerCards(final int indexPlayer, final Player currentPlayer) {
        int i, j, k;
        if (indexPlayer == Cons.player2) {
            i = Cons.player2Back;
            k = Cons.player2Front;
        } else {
            i = Cons.player1Front;
            k = Cons.player1Back;
        }
        for (; i <= k; i++) {
            for (j = 0; j < Cons.nrCols; j++) {
                if (getEntry(i, j) != null) {
                    getEntry(i, j).freeze(false);
                }
            }
        }
        currentPlayer.getPlayerHero().setAttackUsed(false);
    }
    public int cardsOnRowCounter(final char row, final int indexPlayer) {
        int i = findNeededRow(indexPlayer, row);

        if (i == -1) {
            System.out.println("checkIfRowFull error");
            return 1;
        }

        int count = 0;
        for (int j = 0; j < Cons.nrCols; j++) {
            if (getEntry(i, j) != null) {
                count++;
            }
        }
        return count;
    }
    public static int findNeededRow(final int playerIdx, final char rowPos) {
        if (playerIdx == 2 && rowPos == 'F') {
            return 1;
        }
        if (playerIdx == 2 && rowPos == 'B') {
            return 0;
        }
        if (playerIdx == 1 && rowPos == 'F') {
            return 2;
        }
        if (playerIdx == 1 && rowPos == 'B') {
            return Cons.nrRows - 1;
        }
        return -1;
    }

}

class Game {
    private Player playerOne;
    private Table table;
    private Player playerTwo;
    private int seed;
    private int deckIndexPlayerOne;
    private int deckIndexPlayerTwo;
    private int playerTurn;
    private int nrRound = 1;
    private int playerStarting;

    private int affectedRow;

    Game(final Input inputData, final int gameId, final Player playerOne, final Player playerTwo) {
        table = new Table();
        StartGameInput gameInp = inputData.getGames().get(gameId).getStartGame();
        seed = gameInp.getShuffleSeed();
        System.out.println(seed);
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


        playerOne.setPlayerHero((Hero) Card.createTypeOfCard(gameInp.getPlayerOneHero()));
        playerTwo.setPlayerHero((Hero) Card.createTypeOfCard(gameInp.getPlayerTwoHero()));

        //initiates first round
        drawNewCard();

    }

    public Minion getElem(final int i, final int j) {
        return table.getEntry(i, j);
    }

    public int getPlayerTurn() {
        return playerTurn;
    }

    // at the end of a turn, the players cards get unfrozen, their restriction of attacking is reset
    public void endPlayerTurn() {
        table.unfreezePlayerCards(playerTurn, getPlayer(playerTurn));
        table.markCardAbleAttack(playerTurn);

        playerTurn = ((playerTurn == 1) ? 2 : 1);
        if (playerTurn == playerStarting) {
            nrRound++;
            startNewRound();
        }
    }

    private void startNewRound() {
        // at the start of each round, the mana is accorded to the players
        int aux = playerOne.getMana();
        playerOne.setMana(aux + Math.min(nrRound, Cons.maxMana));

        aux = playerTwo.getMana();
        playerTwo.setMana(aux + Math.min(nrRound, Cons.maxMana));
        //both of them get in hand first card available in their deck
        drawNewCard();

    }

    private void drawNewCard() {
        if (!playerOne.getChosenDeck().isEmpty()) {
            playerOne.addCardInHand(playerOne.getChosenDeck().remove(0));
        } else {
            System.out.println("empty deck 1 tried to draw card");
        }
        if (!playerTwo.getChosenDeck().isEmpty()) {
            playerTwo.addCardInHand(playerTwo.getChosenDeck().remove(0));
        } else {
            System.out.println("empty deck 2 tried to draw card");
        }
    }

    public Player getPlayer(final int index) {
        if (index == 1) {
            return playerOne;
        }
        if (index == 2) {
            return playerTwo;
        }
        System.out.println("Wrong player index");
        return null;
    }

    public String placeCard(final int handIndex) {
        Player player = getPlayer(playerTurn);
        ArrayList<Card> handCards = player.getCardsInHand();
        Card card;
        if (handIndex >= handCards.size()) {
            return "Invalid card index";
        }
        card = handCards.get(handIndex);
        if (card instanceof Environment) {
            return "Cannot place environment card on table.";
        }
        if (player.getMana() < card.getMana()) {
            return "Not enough mana to place card on table.";
        }
        if (table.cardsOnRowCounter(((Minion) card).findSittingRow(), playerTurn) == Cons.nrCols) {
            return "Cannot place card on table since row is full.";
        }
        // removes the card from hand
        card = handCards.remove(handIndex);

        placeCardOnRow((Minion) card, playerTurn);
        player.setMana(player.getMana() - card.getMana());
        //it's no error
        return null;
    }
    public void placeCardOnRow(final Minion card, final int playerIdx) {
        int i = Table.findNeededRow(playerIdx, card.findSittingRow());
        int j = 0;
        //find the first position available to insert the card
        for (j = 0; j < Cons.nrCols; j++) {
            if (table.getEntry(i, j) == null) {
                break;
            }
        }
        table.setEntry(i, j, card);
    }

    public ArrayList<ArrayList<Minion>> getCardsOnTable() {
        return table.printTable();
    }

    public Table getTable() {
        return table;
    }

    public ArrayList<Card> getCardsInHand(final int playerIdx) {
        return getPlayer(playerIdx).getCardsInHand();
    }

    public String useEnvironmentCard(final int handIdx, final int affectedRow) {
        this.affectedRow = affectedRow;
        Player player = getPlayer(playerTurn);
        Card card = getPlayer(playerTurn).getCardsInHand().get(handIdx);
        if (card instanceof Environment) {
            if (player.getMana() < card.getMana()) {
                return "Not enough mana to use environment card.";
            }
            if (playerTurn == 2 && (affectedRow == 0 || affectedRow == 1)) {
                return "Chosen row does not belong to the enemy.";
            }
            if (playerTurn == 1 && (affectedRow == 2 || affectedRow == Cons.nrRows - 1)) {
                return "Chosen row does not belong to the enemy.";
            }

            String error = ((Environment) card).useAbility(this);
            if (error == null) {
                player.setMana(player.getMana() - card.getMana());
                getPlayer(playerTurn).getCardsInHand().remove(handIdx); // deleting card after use
                for (int j = 0; j < Cons.nrCols; j++) {
                    if (table.getEntry(affectedRow, j) != null) {
                        if (table.getEntry(affectedRow, j).getHealth() <= 0) {
                            table.removeCard(affectedRow, j);
                            j--;
                        }
                    }
                }
            }
            //there is no error and the card was used
            return error;
        } else {
            return "Chosen card is not of type environment.";
        }
    }

    public int getAffectedRow() {
        return affectedRow;
    }

    public String cardUsesAttack(final ActionsInput actionsInput) {
        int enemyIdx = ((playerTurn == 1) ? 2 : 1);

        Minion attacker = getAttacker(actionsInput);
        Card attacked = getAttacked(actionsInput, enemyIdx);


        if (attacker == null || attacked == null) {
            return "Not a valid attacker/attacked Card";
        }
        if (!(attacked instanceof Hero)) {
            if (!belongToEnemy(actionsInput.getCardAttacked().getX())) {
                return "Attacked card does not belong to the enemy.";
            }
        }
        if (attacker.checkIfAttacked()) {
            return "Attacker card has already attacked this turn.";
        }
        if (attacker.findFreezeStatus()) {
            return "Attacker card is frozen.";
        }

        // if exist at least one tank and the attacked is not a tank
        if (table.existTank(enemyIdx)) {
            if (!(attacked instanceof Tank)) {
                return "Attacked card is not of type 'Tank'.";
            }
        }

        attacker.attack(attacked);
        if ((attacked instanceof Minion) && ((HasHealth) attacked).getHealth() <= 0) {
            int x = actionsInput.getCardAttacked().getX();
            int y = actionsInput.getCardAttacked().getY();
            table.removeCard(x, y);
        }
        if (attacked instanceof Hero) {
            if (((Hero) attacked).getHealth() <= 0) {
                getPlayer(playerTurn).setWins(getPlayer(playerTurn).getWins() + 1);
                if (playerTurn == Cons.player1) {
                    return "Player one killed the enemy hero.";
                } else {
                    return "Player two killed the enemy hero.";
                }
            }
        }
        return null;
    }
    public String cardUsesAbility(final ActionsInput actionsInput) {
        int x = actionsInput.getCardAttacker().getX();
        int y = actionsInput.getCardAttacker().getY();
        Minion attacker =  table.getEntry(x, y);
        if (attacker == null) {
            return "Not a valid attacker Card attacker" + x + " " + y;
        }

        int x2 = actionsInput.getCardAttacked().getX();
        int y2 = actionsInput.getCardAttacked().getY();
        Minion attacked = table.getEntry(x2, y2);

        if (attacked == null) {
            return "Not a valid attacked Card attacker" + x + " " + y;
        }

        if (attacker.findFreezeStatus()) {
            return "Attacker card is frozen.";
        }

        if (attacker.checkIfAttacked()) {
            return "Attacker card has already attacked this turn.";
        }

        if (checkIfHelperCard(attacker)) {
            if (belongToEnemy(actionsInput.getCardAttacked().getX())) {
                return "Attacked card does not belong to the current player.";
            }
        } else {
            if (!belongToEnemy(actionsInput.getCardAttacked().getX())) {
                return "Attacked card does not belong to the enemy.";
            }
        }
        int enemyIdx = ((playerTurn == 1) ? 2 : 1);
        if (table.existTank(enemyIdx) && !checkIfHelperCard(attacker)) {
            if (!(attacked instanceof Tank)) {
                return "Attacked card is not of type 'Tank'.";
            }
        }
        attacker.useAbility(attacked);
        attacker.setAttackUsed(true);

        if (attacked.getHealth() <= 0) {
            table.removeCard(x2, y2);
        }
        if (attacker.getHealth() <= 0) {
            table.removeCard(x, y);
        }
        return null;
    }
    public String useHeroAbility(final ActionsInput actionsInput) {
        Player player = getPlayer(getPlayerTurn());
        Hero hero = player.getPlayerHero();
        affectedRow = actionsInput.getAffectedRow();

        if (player.getMana() < hero.getMana()) {
            return "Not enough mana to use hero's ability.";
        }
        if (hero.checkIfAttacked()) {
            return "Hero has already attacked this turn.";
        }
        if ((hero instanceof LordRoyce) || (hero instanceof EmpressThorina)) {
            if (!belongToEnemy(affectedRow)) {
                return "Selected row does not belong to the enemy.";
            }
        }
        if ((hero instanceof GeneralKociraw) || (hero instanceof KingMudface)) {
            if (belongToEnemy(affectedRow)) {
                return "Selected row does not belong to the current player.";
            }
        }

        String error = hero.useAbility(this);
        hero.setAttackUsed(true);
        player.setMana(player.getMana() - hero.getMana());
        table.removeIfHealthZero(affectedRow);
        return null;
    }
    private Minion getAttacker(final ActionsInput actionsInput) {
        int x = actionsInput.getCardAttacker().getX();
        int y = actionsInput.getCardAttacker().getY();
        return table.getEntry(x, y);

    }
    private Card getAttacked(final ActionsInput actionsInput, final int enemyIdx) {
        if (actionsInput.getCommand().compareTo("useAttackHero") == 0) {
            return getPlayer(enemyIdx).getPlayerHero();
        }

        int x = actionsInput.getCardAttacked().getX();
        int y = actionsInput.getCardAttacked().getY();
        return table.getEntry(x, y);
    }
    private boolean checkIfHelperCard(final Minion card) {
        return card instanceof Disciple;
    }
    private boolean belongToEnemy(final int x) {
        int enemyIdx = ((playerTurn == 1) ? 2 : 1);
        if (enemyIdx == Cons.player1 && x != Cons.player1Front && x != Cons.player1Back) {
            return false;
        }
        if (enemyIdx == Cons.player2 && x != Cons.player2Back && x != Cons.player2Front) {
            return false;
        }
        return true;
    }
}

class Player {
    private int wins;
    private Hero playerHero;
    private ArrayList<Card> cardsInHand;
    private ArrayList<Card> chosenDeck;
    private ArrayList<ArrayList<Card>> allDecks;
    private int nrDecks;
    private int nrCardsInDeck;
    private int mana;

    Player(final DecksInput decksInp) {
        nrDecks = decksInp.getNrDecks();
        nrCardsInDeck = decksInp.getNrCardsInDeck();
        createDecks(decksInp.getDecks());
        nrCardsInDeck = decksInp.getNrCardsInDeck();
    }

    private void createDecks(final ArrayList<ArrayList<CardInput>> decksInp) {
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
                Card newCard = Card.createTypeOfCard(currentCardInp);
                if (newCard == null) {
                    System.out.println("Invalid card name\n");
                    exit(1);
                }
                allDecks.get(i).add(newCard);
            }
            i++;
        }
    }



    public Hero getPlayerHero() {
        return playerHero;
    }

    public void setPlayerHero(final Hero playerHero) {
        this.playerHero = playerHero;
    }

    public int getWins() {
        return wins;
    }
    public void setWins(final int value) {
        wins = value;
    }

    public void setScor(final int scor) {
        this.wins = scor;
    }

    public ArrayList<Card> getCardsInHand() {
        return cardsInHand;
    }

    public void addCardInHand(final Card newCard) {
        cardsInHand.add(newCard);
    }

    public ArrayList<Card> getChosenDeck() {
        return chosenDeck;
    }

    public void setChosenDeck(final Input inputData,final int playerIdx,
                              final int deckIdx, final int seed) {
        cardsInHand = new ArrayList<>();

        this.chosenDeck = new ArrayList<Card>();

        DecksInput deckInp;
        if (playerIdx == 1) {
            deckInp = inputData.getPlayerOneDecks();
        } else {
            deckInp = inputData.getPlayerTwoDecks();
        }

        for (CardInput currentCardInp : deckInp.getDecks().get(deckIdx)) {
            Card newCard = Card.createTypeOfCard(currentCardInp);
            chosenDeck.add(newCard);
        }
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
            if (card instanceof Environment) {
                environmentCards.add(card);
            }
        }
        return environmentCards;
    }
}

abstract class Environment extends Card implements SpecialAbility {
    Environment(final CardInput cardInp) {
        super(cardInp);
    }

    public String attack() {
        return null;
    }

    public String useAbility(final Game gameEnv) {
        return null;
    }

}

class Minion extends Card implements HasHealth {

    private int health;
    private int attackDamage;
    private char sittingRow;
    private boolean freezeStatus;


    Minion(final CardInput cardInp, final char row) {
        super(cardInp);
        attackDamage = cardInp.getAttackDamage();
        health = cardInp.getHealth();
        sittingRow = row;
    }

    Minion(final Minion minion) {
        health = minion.getHealth();
        attackDamage = minion.getAttackDamage();
        sittingRow = minion.findSittingRow();
        freezeStatus = minion.findFreezeStatus();
        super.setAttackUsed(minion.checkIfAttacked());
        super.setName(minion.getName());
        super.setDescription(minion.getDescription());
        super.setColors(minion.getColors());
        super.setMana(minion.getMana());
    }

    public void attack(final Card attacked) {
        if (attacked instanceof HasHealth) {
            int health = ((HasHealth) attacked).getHealth();
            ((HasHealth) attacked).setHealth(health - this.getAttackDamage());
            this.setAttackUsed(true);
        }

    }

    public void useAbility(final Minion attacked) {

    }

    public char findSittingRow() {
        return sittingRow;
    }

    public void freeze(final boolean status) {
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

    public void setHealth(final int health) {
        this.health = health;
    }
}
interface HasHealth {
    int getHealth();
    void setHealth(int health);
}
class Hero extends Card implements HasHealth {
    private int health;

    Hero(final CardInput cardInp) {
        super(cardInp);
        health = Cons.initialHealth;
    }
    Hero(final Hero heroOG) {
        health = heroOG.health;
        super.setColors(heroOG.getColors());
        super.setDescription(heroOG.getDescription());
        super.setMana(heroOG.getMana());
        super.setName(heroOG.getName());

    }

    public String attack() {

        return null;
    }

    public String useAbility(final Game gameEnv) {
        System.out.println("this should not be returned" + gameEnv.getAffectedRow());
        return null;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(final int health) {
        this.health = health;
    }

}

abstract class Card {
    private int mana;
    private String description;
    private ArrayList<String> colors;
    private String name;

    private boolean attackUse;

    public boolean checkIfAttacked() {
        return attackUse;
    }
    public void setAttackUsed(final boolean status) {
        attackUse = status;
    }

    Card(final CardInput cardInp) { // shallow copy
        this.name = cardInp.getName();
        this.colors = cardInp.getColors();
        this.description = cardInp.getDescription();
        this.mana = cardInp.getMana();
    }

    Card() {

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

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setMana(final int mana) {
        this.mana = mana;
    }

    public void setColors(final ArrayList<String> colors) {
        this.colors = colors;
    }

    public static Card createTypeOfCard(final CardInput cardInput) {
        String name = cardInput.getName();
        return switch (name) {
            case "The Ripper" -> new TheRipper(cardInput, 'F'); //FRONT_ROWer
            case "Miraj" -> new Miraj(cardInput, 'F');
            case "Goliath" -> new Tank(cardInput, 'F');
            case "Warden" -> new Tank(cardInput, 'F');
            case "Sentinel" -> new Minion(cardInput, 'B');
            case "Berserker" -> new Minion(cardInput, 'B');
            case "The Cursed One" -> new TheCursedOne(cardInput, 'B');
            case "Disciple" -> new Disciple(cardInput, 'B');
            case "Firestorm" -> new Firestorm(cardInput);
            case "Winterfell" -> new Winterfell(cardInput);
            case "Heart Hound" -> new HeartHound(cardInput);
            case "Lord Royce" -> new LordRoyce(cardInput);
            case "Empress Thorina" -> new EmpressThorina(cardInput);
            case "King Mudface" -> new KingMudface(cardInput);
            case "General Kocioraw" -> new GeneralKociraw(cardInput);
            default -> null;
        };
    }

}

interface SpecialAbility {
    //return value = error
    String useAbility(Game gameEnv);
}

class Miraj extends Minion {
    Miraj(final CardInput cardInp, final char row) {
        super(cardInp, row);
    }

    public void useAbility(final Minion attacked) {
        int aux = this.getHealth();
        this.setHealth(attacked.getHealth());
        attacked.setHealth(aux);
    }
}

class TheRipper extends Minion {
    TheRipper(final CardInput cardInp, final char row) {
        super(cardInp, row);
    }

    public void useAbility(final Minion attacked) {
        attacked.setAttackDamage(Math.max(attacked.getAttackDamage() - 2, 0));
    }
}

class Disciple extends Minion {
    Disciple(final CardInput cardInp, final char row) {
        super(cardInp, row);
    }

    public void useAbility(final Minion attacked) {
        attacked.setHealth(attacked.getHealth() + 2);
    }
}

class TheCursedOne extends Minion {
    TheCursedOne(final CardInput cardInp, final char row) {
        super(cardInp, row);
    }

    public void useAbility(final Minion attacked) {
        int aux = attacked.getHealth();
        attacked.setHealth(attacked.getAttackDamage());
        attacked.setAttackDamage(aux);
    }
}

class Tank extends Minion { // wrapper class for Goliath & Warden & other tanks if any
    Tank(final CardInput cardIn, final char row) {
        super(cardIn, row);
    }
}

class Firestorm extends Environment {

    Firestorm(final CardInput cardInput) {
        super(cardInput);
    }

    @Override
    public String useAbility(final Game gameEnv) {
        int affectedRow = gameEnv.getAffectedRow();
        int playerTurn = gameEnv.getPlayerTurn();
        Player player = gameEnv.getPlayer(playerTurn);

        int aux;
        for (int j = 0; j < Cons.nrCols; j++) {
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

    Winterfell(final CardInput cardInput) {
        super(cardInput);
    }

    @Override
    public String useAbility(final Game gameEnv) {
        int affectedRow = gameEnv.getAffectedRow();

        for (int j = 0; j < Cons.nrCols; j++) {
            if (gameEnv.getTable().getEntry(affectedRow, j) != null) {
                gameEnv.getTable().getEntry(affectedRow, j).freeze(true);
            }
        }
        return null;
    }
}

class HeartHound extends Environment {

    HeartHound(final CardInput cardInput) {
        super(cardInput);
    }

    @Override
    public String useAbility(final Game gameEnv) {
        int playerIdx = gameEnv.getPlayerTurn();
        int affectedRow = gameEnv.getAffectedRow();
        int maxHealth = -1;
        int affectedColumn = 0;

        for (int j = 0; j < Cons.nrCols; j++) {
            Minion itrCard = (Minion) gameEnv.getElem(affectedRow, j);
            if (itrCard != null && itrCard.getHealth() > maxHealth) {
                maxHealth = itrCard.getHealth();
                affectedColumn = j;
            }
        }
        // stolenCard = the card that was stolen from the other player
        Minion stolenCard = (Minion) gameEnv.getElem(affectedRow, affectedColumn);

        // if the number of cards on current player is already full, return error
        // stolenCard.getSittingRow returns the position (Front, Back) that
        // the card is supposed to sit
        if (gameEnv.getTable().cardsOnRowCounter(stolenCard.findSittingRow(), playerIdx) == Cons.nrCols)
            return "Cannot steal enemy card since the player's row is full.";

        stolenCard = (Minion) gameEnv.getTable().removeCard(affectedRow, affectedColumn);
        gameEnv.placeCardOnRow(stolenCard, playerIdx);
        //return no error
        return null;

    }
}

class LordRoyce extends Hero{
    LordRoyce(final CardInput cardInp) {
        super(cardInp);
    }

    @Override
    public String useAbility(Game gameEnv) {
        int row = gameEnv.getAffectedRow();

        Minion cardMaxDmg = gameEnv.getElem(row, 0);
        // if there is not a single card on the affected row, end function
        if (cardMaxDmg == null) {
            return null;
        }
        int posMaxDmg = 0;
        for (int j = 1; j < Cons.nrCols; j++ ) {
            if (gameEnv.getElem(row, j) != null) {
                if (cardMaxDmg.getAttackDamage() < gameEnv.getElem(row, j).getAttackDamage()) {
                    cardMaxDmg = gameEnv.getElem(row, j);
                    posMaxDmg = j;
                }
            }
        }
        gameEnv.getElem(row, posMaxDmg).freeze(true);
        return null;
    }
}

class EmpressThorina extends Hero {

    EmpressThorina(final CardInput cardInp) {
        super(cardInp);
    }

    @Override
    public String useAbility(Game gameEnv) {
        int row = gameEnv.getAffectedRow();
        Minion cardMaxHealth = gameEnv.getElem(row, 0);
        // if there is not a single card on the affected row, end function
        if (cardMaxHealth == null) {
            return null;
        }
        int posMaxHealth = 0;
        for (int j = 1; j < Cons.nrCols; j++ ) {
            if (gameEnv.getElem(row, j) != null) {
                if (cardMaxHealth.getHealth() < gameEnv.getElem(row, j).getHealth()) {
                    cardMaxHealth = gameEnv.getElem(row, j);
                    posMaxHealth = j;
                }
            }
        }
        gameEnv.getTable().removeCard(row, posMaxHealth);
        return null;
    }
}

class KingMudface extends Hero {

    KingMudface(final CardInput cardInp) {
        super(cardInp);
    }

    @Override
    public String useAbility(Game gameEnv) {
        int row = gameEnv.getAffectedRow();
        for (int j = 0; j < Cons.nrCols; j++) {
            Minion card = gameEnv.getElem(row, j);
            if (card != null) {
                card.setHealth(card.getHealth() + 1);
            }
        }
        return null;
    }
}

class GeneralKociraw extends Hero {

    GeneralKociraw(final CardInput cardInp) {
        super(cardInp);
    }

    @Override
    public String useAbility(Game gameEnv) {
        int row = gameEnv.getAffectedRow();
        for (int j = 0; j < Cons.nrCols; j++) {
            Minion card = gameEnv.getElem(row, j);
            if (card != null) {
                card.setAttackDamage(card.getAttackDamage() + 1);
            }
        }
        return null;
    }
}

interface Cons {
    int nrRows = 4;
    int nrCols = 5;
    int player2Back = 0;
    int player2Front = 1;
    int player1Front = 2;
    int player1Back = 3;
    int player1 = 1;
    int player2 = 2;
    int maxMana = 10;
    int initialHealth = 30;
}
