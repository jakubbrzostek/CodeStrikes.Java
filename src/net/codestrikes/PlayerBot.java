package net.codestrikes;

import net.codestrikes.sdk.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerBot extends BotBase {
    private Area defence = Area.UppercutPunch;
    private int myLifeLeft = 0;
    private int opponentsLifeLeft = 0;
    private int myScoreTotal = 0;
    private int opponentScoreTotal = 0;
    private int roundCounter = 100;

    public List<RoundContext> opponentsMoves = new ArrayList<RoundContext>();

    private Area changeDefence(Area oldDefence) {
        return (oldDefence == Area.HookKick) ? Area.HookPunch : Area.HookKick;
    }

    private Area createRandomAttack() {
        return new Random().nextDouble() > 0.5d ? Area.LowKick : Area.HookPunch;
    }

    private void createAggressiveAttack(RoundContext context) {

        context.getMyMoves()
                .addAttack(Area.HookKick)
                .addAttack(Area.HookPunch)
                .addAttack(Area.LowKick)
                .addAttack(Area.LowKick)
                .addAttack(Area.LowKick)
                .addAttack(Area.UppercutPunch);
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
                .entrySet().stream().min(Comparator.comparing(Map.Entry::getValue)).orElse(null));

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
                .entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).orElse(null));

        return Objects.requireNonNull(mostPopularMove).getKey().getArea();
    }

    public MoveCollection nextMove(RoundContext context) {
        myLifeLeft = context.getMyLifePoints();
        opponentsLifeLeft = context.getOpponentLifePoints();
        myScoreTotal += context.getMyDamage();
        opponentScoreTotal += context.getOpponentDamage();

        if (opponentsMoves.size() < 99) {
            createAggressiveAttack(context);
        } else if (myScoreTotal >= opponentScoreTotal) {
            context.getMyMoves().addAttack(createRandomAttack()); // 3 attacks, 0 defence

            //If my defence don't contains most popular opponent attack, change my defence
        } else if (!context.getMyMoves().hasDefence(findMovesByAttackAnalyze())) {
            if (context.getMyMoves().getDefences() == null) {
                defence = findMovesByAttackAnalyze();
                context.getMyMoves().addDefence(defence);
            } else {
                defence = findMovesByAttackAnalyze();
            }
        }
        //If my attack don't contains least popular opponent defence, change my attack
        else if (Arrays.stream(context.getMyMoves().getAttacks()).noneMatch(i -> i.getArea() == findMovesByDefenceAnalyze())){

        }

        // Only if I'll won by blocking everything besides LowKicks
        else if (myLifeLeft - opponentsMoves.size() > opponentsLifeLeft) {
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
