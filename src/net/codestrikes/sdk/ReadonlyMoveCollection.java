package net.codestrikes.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReadonlyMoveCollection {
    protected ArrayList<Move> moveList;

    public ReadonlyMoveCollection() {
        moveList = new ArrayList<>();
    }

    public Move[] getMoves() {
        return moveList.toArray(new Move[moveList.size()]);
    }

    public Move[] getAttacks() {
        List<Move> list = moveList.stream().filter(x -> x.getType() == MoveType.Attack).collect(Collectors.toList());
        return list.toArray(new Move[list.size()]);
    }

    public Move[] getDefences() {
        List<Move> list = moveList.stream().filter(x -> x.getType() == MoveType.Defense).collect(Collectors.toList());
        return list.toArray(new Move[list.size()]);
    }

    public boolean hasDefence(Area area) {
        return moveList.stream().anyMatch(x -> x.getType() == MoveType.Defense && x.getArea() == area);
    }

    public int sumEnergy() {
        return moveList.stream().mapToInt(x -> x.getEnergy()).sum();
    }
}
