package ph.dds.model;

public record ReliefItem(int id, String name, String category, String unit,
        int quantity, int threshold) {
    public boolean checkAvailability(int requested) { return requested > 0 && quantity >= requested; }
    @Override public String toString() { return name + " (" + quantity + " " + unit + " available)"; }
}
