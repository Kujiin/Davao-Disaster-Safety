package ph.dds.model;

public record User(int id, String username, String name, String role, Integer familyId) {
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
