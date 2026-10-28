package ph.dds.model;

public record ResponseRound(int id, String disaster, String name, String location) {
    @Override public String toString() { return disaster + " / " + name; }
}
