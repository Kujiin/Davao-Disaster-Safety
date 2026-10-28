package ph.dds.db;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** JDBC connection to the separately installed MySQL database named ddsdatabase. */
public final class DBConnection {
    public static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ddsdatabase?connectionTimeZone=Asia/Manila&connectTimeout=5000&socketTimeout=15000&allowPublicKeyRetrieval=true&useSSL=false";
    private DBConnection() {}
    public static Connection getConnection() throws SQLException {
        try { return database(Database.projectRoot()).connect(); }
        catch(IOException ex) { throw new SQLException("Cannot read config/database.properties",ex); }
    }
    public static Database database(Path root) throws IOException {
        Properties p=new Properties();
        Path file=root.resolve("config/database.properties");
        if(Files.exists(file))try(InputStream in=Files.newInputStream(file)){p.load(in);}
        return new Database(value(p,"db.url","DDS_DB_URL",DEFAULT_URL),
                value(p,"db.user","DDS_DB_USER","root"),value(p,"db.password","DDS_DB_PASSWORD",""));
    }
    private static String value(Properties p,String key,String env,String fallback) {
        String override=System.getenv(env);
        return override==null?p.getProperty(key,fallback):override;
    }
}
