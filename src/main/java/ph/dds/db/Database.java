package ph.dds.db;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

/** JDBC only. No in-memory substitute: every application record lives in MySQL. */
public final class Database {
    private final String url,user,password;
    public Database(String url,String user,String password) {
        this.url=url; this.user=user; this.password=password;
    }
    public Connection connect() throws SQLException { return DriverManager.getConnection(url,user,password); }
    public static Path projectRoot() {
        Path p=Path.of(System.getProperty("user.dir")).toAbsolutePath();
        if(Files.exists(p.resolve("pom.xml"))) return p;
        try {
            Path jar=Path.of(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path parent=Files.isDirectory(jar)?jar:jar.getParent();
            if(parent.getFileName().toString().equals("target")) return parent.getParent();
        } catch(Exception ignored) {}
        return p;
    }
    public static Database open(Path root, Consumer<String> status) throws Exception {
        status.accept("Connecting to MySQL / ddsdatabase...");
        Database db=DBConnection.database(root);
        try(Connection c=db.connect();Statement s=c.createStatement()) {
            try(ResultSet r=s.executeQuery("SELECT setting_value FROM schema_info WHERE setting_key='seeded'")) {
                if(!r.next())throw new SQLException("Import sql/ddsdatabase.sql into MySQL before running DDS.");
            }
            for(String table:new String[]{"families","users","disasters","response_rounds","relief_items","distributions","stock_movements","audit_logs"})
                try(ResultSet ignored=s.executeQuery("SELECT 1 FROM `"+table+"` LIMIT 0")){}
        } catch(SQLException ex) {
            throw new SQLException("Could not open ddsdatabase. Start your MySQL server, import sql/ddsdatabase.sql, and check config/database.properties (host, port, username, password).\nMySQL: "+ex.getMessage(),ex);
        }
        return db;
    }
}
