package ph.dds.service;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ph.dds.db.Database;
import ph.dds.model.*;

/** Real MySQL integration tests. Each test owns a temporary database, then drops it. */
class ReliefServiceTest {
    private static String password,options;
    private String schema;private Database db;private ReliefService service;private User admin,member;
    @BeforeAll static void server()throws Exception{
        options="?connectionTimeZone=Asia/Manila&allowPublicKeyRetrieval=true&useSSL=false";
        password=System.getenv("DDS_TEST_PASSWORD");
        Assumptions.assumeTrue(password!=null,"Set DDS_TEST_PASSWORD and DDS_TEST_PORT for isolated integration tests");
    }
    private static Connection root()throws SQLException{return DriverManager.getConnection("jdbc:mysql://127.0.0.1:"+System.getenv().getOrDefault("DDS_TEST_PORT","3306")+"/"+options,"root",password);}
    @BeforeEach void setup()throws Exception{
        schema="dds_test_"+UUID.randomUUID().toString().replace("-","");
        try(Connection c=root();Statement s=c.createStatement()){s.execute("CREATE DATABASE "+schema+" CHARACTER SET utf8mb4");}
        db=new Database("jdbc:mysql://127.0.0.1:"+System.getenv().getOrDefault("DDS_TEST_PORT","3306")+"/"+schema+options,"root",password);ph.dds.db.TestBootstrap.initialize(db);service=new ReliefService(db);
        admin=service.login("admin","Admin@DDS2026".toCharArray(),"ADMIN");member=service.login("member","Member@DDS2026".toCharArray(),"MEMBER");
    }
    @AfterEach void cleanup()throws Exception{if(schema!=null)try(Connection c=root();Statement s=c.createStatement()){s.execute("DROP DATABASE "+schema);}}
    private int scalar(String sql)throws SQLException{try(Connection c=db.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    private void execute(String sql)throws SQLException{try(Connection c=db.connect();Statement s=c.createStatement()){s.execute(sql);}}
    @Test void authenticationAndMemberAuthorization()throws Exception{
        assertNotNull(admin);assertNotNull(member);assertNull(service.login("admin","bad".toCharArray(),"ADMIN"));assertNull(service.login("member","Member@DDS2026".toCharArray(),"ADMIN"));
        assertEquals(1,service.families(member,1).size());assertEquals(1,service.families(member,1).get(0).id());assertEquals(1,service.history(member,1).length);
        User forged=new User(member.id(),member.username(),member.name(),"ADMIN",null);
        assertThrows(SecurityException.class,()->service.restock(forged,1,3,"test"));assertThrows(SecurityException.class,()->service.tableNames(member));
    }
    @Test void successfulDistributionAndDurableRestart()throws Exception{
        Distribution d=service.recordDistribution(admin,19,1,1,2,"Integration test");assertTrue(d.id()>0);assertEquals(118,scalar("SELECT quantity FROM relief_items WHERE item_id=1"));
        assertEquals(-2,scalar("SELECT quantity_change FROM stock_movements WHERE distribution_id="+d.id()));
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='DISTRIBUTE'"));
        ph.dds.db.TestBootstrap.initialize(db);assertEquals(19,new ReliefService(db).history(admin,1).length);assertEquals(118,scalar("SELECT quantity FROM relief_items WHERE item_id=1"));
    }
    @Test void insufficientAndDuplicateLeaveEverythingUnchanged()throws Exception{
        int count=scalar("SELECT COUNT(*) FROM distributions"),moves=scalar("SELECT COUNT(*) FROM stock_movements");
        assertThrows(IllegalArgumentException.class,()->service.recordDistribution(admin,19,3,1,9,"too many"));
        assertThrows(IllegalArgumentException.class,()->service.recordDistribution(admin,1,1,1,1,"duplicate"));
        assertEquals(count,scalar("SELECT COUNT(*) FROM distributions"));assertEquals(moves,scalar("SELECT COUNT(*) FROM stock_movements"));assertEquals(120,scalar("SELECT quantity FROM relief_items WHERE item_id=1"));assertEquals(8,scalar("SELECT quantity FROM relief_items WHERE item_id=3"));
    }
    @Test void databaseFailureRollsBackRecordStockMovementAndAudit()throws Exception{
        execute("CREATE TRIGGER reject_audit BEFORE INSERT ON audit_logs FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Injected test failure'");
        assertThrows(SQLException.class,()->service.recordDistribution(admin,19,2,1,3,"test rollback"));
        assertEquals(80,scalar("SELECT quantity FROM relief_items WHERE item_id=2"));assertEquals(0,scalar("SELECT COUNT(*) FROM distributions WHERE family_id=19"));assertEquals(0,scalar("SELECT COUNT(*) FROM stock_movements WHERE item_id=2 AND quantity_change<0"));
    }
    @Test void concurrentRequestsCannotOversell()throws Exception{
        execute("UPDATE relief_items SET quantity=1 WHERE item_id=3");
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        try{
            List<Future<Boolean>> results=new ArrayList<>();for(int family:new int[]{19,20})results.add(pool.submit(()->{start.await();try{service.recordDistribution(admin,family,3,2,1,"concurrent");return true;}catch(IllegalArgumentException ex){return false;}}));
            start.countDown();int successes=0;for(Future<Boolean> f:results)if(f.get(15,TimeUnit.SECONDS))successes++;
            assertEquals(1,successes);assertEquals(0,scalar("SELECT quantity FROM relief_items WHERE item_id=3"));assertEquals(1,scalar("SELECT COUNT(*) FROM distributions WHERE item_id=3 AND round_id=2"));
        }finally{pool.shutdownNow();}
    }
    @Test void familyValidationRoundScopeAndRestocking()throws Exception{
        assertThrows(IllegalArgumentException.class,()->service.recordDistribution(admin,19,1,1,0,"invalid"));
        assertThrows(IllegalArgumentException.class,()->service.saveFamily(admin,new Family(0,"","",0,"","","",null,null,"",false)));
        service.saveFamily(admin,new Family(0,"","Sample Test Family",4,"Bucana","Quimpo Boulevard","",null,null,"No coordinates",false));assertEquals(25,service.families(admin,1).size());
        service.restock(admin,3,5,"Donation receipt test");assertEquals(13,scalar("SELECT quantity FROM relief_items WHERE item_id=3"));
        service.recordDistribution(admin,1,1,2,1,"new round allowed");service.recordDistribution(admin,1,2,1,1,"different item allowed");
        assertEquals(2,service.history(member,1).length);assertTrue(service.families(member,2).get(0).assisted());
    }
    @Test void schemaIntegrityAndSafeTableViewer()throws Exception{
        assertEquals(9,service.tableNames(admin).size());assertFalse(Arrays.asList(service.inspectTable(admin,"users").columns()).contains("password_hash"));
        assertThrows(IllegalArgumentException.class,()->service.inspectTable(admin,"users; DROP TABLE families"));
        assertThrows(SQLException.class,()->execute("UPDATE relief_items SET quantity=-1 WHERE item_id=1"));
        assertThrows(SQLException.class,()->execute("INSERT INTO distributions(family_id,item_id,round_id,quantity,staff_id) VALUES(99999,1,1,1,1)"));
    }
}
