package ph.dds.db;
import java.io.*;
import java.sql.*;
public final class TestBootstrap {
 private final Database db; private TestBootstrap(Database db){this.db=db;}
 public static void initialize(Database db)throws Exception{new TestBootstrap(db).initialize();}
 private Connection connect()throws SQLException{return db.connect();}
    private void initialize() throws Exception {
        try(Connection c=connect(); Statement s=c.createStatement()) {
            try(ResultSet r=s.executeQuery("SELECT GET_LOCK('dds_schema_init',30)")) {
                r.next(); if(r.getInt(1)!=1) throw new SQLException("Another DDS instance is setting up. Try again shortly.");
            }
            try {
                String schema;
                try(InputStream in=Database.class.getResourceAsStream("/schema.sql")) {
                    if(in==null) throw new IOException("Missing schema.sql");
                    schema=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
                }
                for(String sql:schema.split(";")) if(!sql.isBlank()) s.execute(sql);
                try(ResultSet r=s.executeQuery("SELECT setting_value FROM schema_info WHERE setting_key='seeded'")) {
                    if(r.next()) return;
                }
                Seeder.seed(c);
            } finally { s.execute("SELECT RELEASE_LOCK('dds_schema_init')"); }
        }
    }
}