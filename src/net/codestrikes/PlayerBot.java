package net.codestrikes;

import net.codestrikes.sdk.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerBot extends BotBase {

    private List<RoundContext> opponentsMoves = new ArrayList<RoundContext>();

    private int attackExpUsed(RoundContext context) {
        int attackExpCount = 0;

        for (Move element : context.getMyMoves().getAttacks()) {
            attackExpCount += element.getEnergy();
        }

        return attackExpCount;
    }

    private int defenceExpUsed(RoundContext context) {
        int defenceExpCount = 0;

        for (Move element : context.getMyMoves().getDefences()) {
            defenceExpCount += element.getEnergy();
        }

        return defenceExpCount;
    }

    private boolean checkExpExceed(RoundContext context) {
        return attackExpUsed(context) + defenceExpUsed(context) <= 12;
    }

    private int expLeft(RoundContext context) {
        return 12 - attackExpUsed(context) - defenceExpUsed(context);
    }

    private void useSpareExp(RoundContext context) {
        if (expLeft(context) >= 3) {
            context.getMyMoves().addAttack(Area.HookPunch);
        } else if (expLeft(context) < 3 && expLeft(context) >= 2) {
            context.getMyMoves().addAttack(Area.UppercutPunch);
        } else if (expLeft(context) == 1) {
            context.getMyMoves().addAttack(Area.LowKick);
        }
    }

    private void changeCurrentDefenceContext(RoundContext context, Area addElement) {

        // check if my current context don't contains defence I want to add
        if (!context.getMyMoves().hasDefence(addElement)) {
            // I need to check if I don't exceed EXP
            Move tmpMove = new Move(MoveType.Defense, addElement);

            if (expLeft(context) >= tmpMove.getEnergy()) {
                context.getMyMoves().addDefence(addElement);
            } else {
                // because EXP would be exceeded I need to delete enough elements to add necessary attack
                // we also need to check if my context contains any defence
                if (context.getMyMoves().getDefences().length > 0) {
                    for (Move element : context.getMyMoves().getAttacks()) {
                        context.getMyMoves().remove(element);
                        //check if it's possible to add more than once
                        for (int i = expLeft(context); i >= tmpMove.getEnergy(); i -= tmpMove.getEnergy()) {
                            context.getMyMoves().addDefence(addElement);
                        }
                        useSpareExp(context);
                        break;
                    }
                }
            }
        }
    }

    private void changeCurrentAttackContext(RoundContext context, Area addElement) {

        // check if my current context don't contains attack I want to add
        if (Arrays.stream(context.getMyMoves().getAttacks()).noneMatch(i -> i.getArea() == addElement)) {
            // I need to check if I don't exceed EXP
            Move tmpMove = new Move(MoveType.Attack, addElement);

            if (expLeft(context) >= tmpMove.getEnergy()) {
                context.getMyMoves().addAttack(addElement);
            } else {
                // because EXP would be exceeded I need to delete enough elements to add necessary attack
                // we also need to check if my context contains any defence
                if (context.getMyMoves().getAttacks().length > 0) {
                    for (Move element : context.getMyMoves().getAttacks()) {
                        context.getMyMoves().remove(element);
                        //check if it's possible to add more than once
                        for (int i = expLeft(context); i >= tmpMove.getEnergy() && i >= 0; i -= tmpMove.getEnergy()) {
                            context.getMyMoves().addAttack(addElement);
                        }
                        useSpareExp(context);
                        break;
                    }
                }
            }
        }
    }

    private void createAggressiveAttack(RoundContext context) {

        context.getMyMoves()
                .addAttack(Area.HookKick)
                .addAttack(Area.HookPunch)
                .addAttack(Area.UppercutPunch)
                .addAttack(Area.LowKick)
                .addAttack(Area.LowKick)
                .addAttack(Area.LowKick);
    }

    private void createMediumAttack(RoundContext context) {

        context.getMyMoves()
                .addAttack(Area.HookPunch)
                .addAttack(Area.HookPunch)
                .addAttack(Area.UppercutPunch);

        if (context.getLastOpponentMoves() != null && Arrays.stream(context.getLastOpponentMoves().getAttacks()).noneMatch(i -> i.getArea() == Area.HookKick)) {
            context.getMyMoves().addDefence(Area.HookKick);
        } else {
            context.getMyMoves().addDefence(Area.HookPunch);
        }
    }

    private void createTotalAttack(RoundContext context) {

        context.getMyMoves()
                .addAttack(Area.HookKick)
                .addAttack(Area.HookKick)
                .addAttack(Area.HookKick);
    }

    private void createFullDefence(RoundContext context) {

        context.getMyMoves()
                .addDefence(Area.HookKick)
                .addDefence(Area.HookPunch)
                .addDefence(Area.UppercutPunch);
    }

    private Area findMovesByAttackAnalyze() {

        List<Move> tmpList = new ArrayList<>();
        Map.Entry<Move, Long> mostPopularAttack;

        for (RoundContext element : opponentsMoves) {
            if (element.getLastOpponentMoves() != null) {
                tmpList.addAll(Arrays.asList(element.getLastOpponentMoves().getAttacks()));
            }
        }

        if (tmpList.size() > 0) {
            mostPopularAttack = (tmpList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null));

            return Objects.requireNonNull(mostPopularAttack).getKey().getArea();
        } else {
            return Area.HookKick;
        }
    }

    private Area findMovesByDefenceAnalyze() {

        List<Move> tmpList = new ArrayList<>();
        Map.Entry<Move, Long> leastPopularDefence;

        for (RoundContext element : opponentsMoves) {
            if (element.getLastOpponentMoves() != null) {
                tmpList.addAll(Arrays.asList(element.getLastOpponentMoves().getDefences()));
            }
        }

        if (tmpList.size() > 0) {
            leastPopularDefence = (tmpList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream().min(Map.Entry.comparingByValue()).orElse(null));

            return Objects.requireNonNull(leastPopularDefence).getKey().getArea();
        } else {
            return Area.HookKick;
        }
    }

    public MoveCollection nextMove(RoundContext context) {
        int myLifeLeft = context.getMyLifePoints();
        int opponentsLifeLeft = context.getOpponentLifePoints();

        if (opponentsMoves.size() == 3) {
            createFullDefence(context);
            opponentsMoves.add(context);

            return context.getMyMoves();
        }

        if (myLifeLeft >= opponentsLifeLeft) {
            if (context.getLastOpponentMoves() != null && !context.getLastOpponentMoves().hasDefence(Area.HookKick)) {
                createTotalAttack(context);
            } else {
                // I'll analyze opponents move to set up my attack and defence
                createMediumAttack(context);
                changeCurrentAttackContext(context, findMovesByDefenceAnalyze());
                changeCurrentDefenceContext(context, findMovesByAttackAnalyze());
            }

            // Only if I'll won by blocking everything besides LowKicks
        } else if (myLifeLeft - (100 - opponentsMoves.size()) * 12 > opponentsLifeLeft) {
            createFullDefence(context);
        }

        opponentsMoves.add(context);

        if (opponentsMoves.size() >= 100) {
            opponentsMoves = new ArrayList<>();
        }

        return context.getMyMoves();
    }

    public String toString() {
        return "Player Bot";
    }

}
