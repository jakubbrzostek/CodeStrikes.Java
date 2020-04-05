package net.codestrikes;

import net.codestrikes.sdk.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class PlayerBot extends BotBase {
    private int myScoreTotal = 0;
    private int opponentScoreTotal = 0;
//    private int roundCounter = 100;

    private Map<Area, Integer> attackEnergyMap = Map.ofEntries(
            entry(Area.HookKick, 4),
            entry(Area.HookPunch, 3),
            entry(Area.UppercutPunch, 2),
            entry(Area.LowKick, 1)
    );

    private Map<Area, Integer> defenceEnergyMap = Map.ofEntries(
            entry(Area.HookKick, 4),
            entry(Area.HookPunch, 4),
            entry(Area.UppercutPunch, 4),
            entry(Area.LowKick, 4)
    );


    private List<RoundContext> opponentsMoves = new ArrayList<RoundContext>();

//    private Area changeDefence(Area oldDefence) {
//        return (oldDefence == Area.HookKick) ? Area.HookPunch : Area.HookKick;
//    }

    private Area createRandomAttack() {
        return new Random().nextDouble() > 0.5d ? Area.LowKick : Area.HookPunch;
    }

    private void changeCurrentContext(RoundContext context, Area deleteElement, Area addElement) {

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
        Map.Entry<Move, Long> mostPopularMove;

        for (RoundContext element : opponentsMoves) {
            if (element.getLastOpponentMoves() != null) {
                tmpList.addAll(Arrays.asList(element.getLastOpponentMoves().getAttacks()));
            }
        }

        mostPopularMove = (tmpList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).orElse(null));

        return Objects.requireNonNull(mostPopularMove).getKey().getArea();
    }

    private Area findMovesByDefenceAnalyze() {

        List<Move> tmpList = new ArrayList<>();
        Map.Entry<Move, Long> mostPopularMove;

        for (RoundContext element : opponentsMoves) {
            if (element.getLastOpponentMoves() != null) {
                tmpList.addAll(Arrays.asList(element.getLastOpponentMoves().getDefences()));
            }
        }

        mostPopularMove = (tmpList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().min(Comparator.comparing(Map.Entry::getValue)).orElse(null));

        return Objects.requireNonNull(mostPopularMove).getKey().getArea();
    }

    public MoveCollection nextMove(RoundContext context) {
        int myLifeLeft = context.getMyLifePoints();
        int opponentsLifeLeft = context.getOpponentLifePoints();
        myScoreTotal += context.getMyDamage();
        opponentScoreTotal += context.getOpponentDamage();

        if (myLifeLeft > opponentsLifeLeft) {
            if (Arrays.stream(context.getLastOpponentMoves().getDefences()).noneMatch(i -> i.getArea() == Area.HookKick)) {
                createTotalAttack(context);
            } else {
                createAggressiveAttack(context);
            }

            //If my defence don't contains most popular opponent attack, change my defence
        } else if (!context.getMyMoves().hasDefence(findMovesByAttackAnalyze())) {
            Area defence = Area.UppercutPunch;
            if (context.getMyMoves().getDefences() == null) {
                defence = findMovesByAttackAnalyze();
                context.getMyMoves().addDefence(defence);
            } else {
                defence = findMovesByAttackAnalyze();
            }
        }
        //If my attack don't contains least popular opponent defence, change my attack
        else if (Arrays.stream(context.getMyMoves().getAttacks()).noneMatch(i -> i.getArea() == findMovesByDefenceAnalyze())) {

        }

        // Only if I'll won by blocking everything besides LowKicks
        else if (myLifeLeft - opponentsMoves.size() * 12 > opponentsLifeLeft) {
            context.getMyMoves()
                    .addDefence(Area.HookKick)
                    .addDefence(Area.HookPunch)
                    .addDefence(Area.UppercutPunch);
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
