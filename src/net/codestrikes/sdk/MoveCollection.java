package net.codestrikes.sdk;

public class MoveCollection extends ReadonlyMoveCollection {
    public MoveCollection addAttack(Area area) {
        Move move = new Move(MoveType.Attack, area);
        moveList.add(move);
        return this;
    }

    public MoveCollection addDefence(Area area) {
        Move move = new Move(MoveType.Defense, area);
        moveList.add(move);
        return this;
    }

    public MoveCollection remove(Move move) {
        moveList.remove(move);
        return this;
    }

}
