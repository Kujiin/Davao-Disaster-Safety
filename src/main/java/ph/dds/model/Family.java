package ph.dds.model;

/** Family participant in the supplied UML. Coordinates may be absent. */
public record Family(int id, String code, String representative, int members,
        String barangay, String street, String contact, Double latitude,
        Double longitude, String locationNote, boolean assisted) {
    @Override public String toString() { return code + " - " + representative; }
}
