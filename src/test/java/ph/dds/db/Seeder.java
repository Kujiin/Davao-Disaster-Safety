package ph.dds.db;

import java.sql.*;
import ph.dds.service.Passwords;

final class Seeder {
    private Seeder() {}
    static void seed(Connection c) throws SQLException {
        c.setAutoCommit(false);
        try {
            try(Statement s=c.createStatement()) {
                s.executeUpdate("INSERT INTO disasters(disaster_id,name,disaster_type,location,event_date,notes) VALUES (1,'Demo flood response','Flood','Matina Crossing, Davao City','2026-10-09','Classroom demonstration only. This is not a report of an actual disaster.')");
                s.executeUpdate("INSERT INTO response_rounds(round_id,disaster_id,round_name) VALUES (1,1,'Round 1'),(2,1,'Round 2')");
                s.executeUpdate("INSERT INTO relief_items(item_id,item_name,category,unit,quantity,low_stock_threshold) VALUES (1,'Family food pack','Food','packs',120,20),(2,'Drinking water (6 L)','Water','bottles',80,15),(3,'Hygiene kit','Hygiene','kits',8,10),(4,'Sleeping mat','Shelter','mats',35,10)");
            }
            String[] names={"Ana Santos","Luis Reyes","Mia Cruz","Paolo Lim","Maria Dela Cruz","Jose Garcia","Elena Ramos","Carlo Mendoza","Rosa Aquino","Ben Torres","Liza Flores","Marco Villanueva","Celia Castillo","Ramon Bautista","Nina Navarro","Leo Fernandez","Sara Diaz","Dan Morales","Grace Mercado","Noel Perez","Irene Rivera","Alex Gonzales","Mila Tan","Joel Salvador"};
            try(PreparedStatement p=c.prepareStatement("INSERT INTO families(family_id,family_code,representative,member_count,barangay,street,latitude,longitude,location_note) VALUES(?,?,?,?,?,?,?,?,?)")) {
                for(int i=0;i<names.length;i++) {
                    p.setInt(1,i+1);p.setString(2,String.format("F-%03d",i+1));p.setString(3,names[i]);p.setInt(4,3+i%4);
                    p.setString(5,"Matina Crossing");p.setString(6,"MacArthur Highway");
                    p.setDouble(7,7.049+(i%6)*0.001);p.setDouble(8,125.574+(i/6)*0.0015);
                    p.setString(9,"Fictional demo household; approximate point, not a verified home address");p.addBatch();
                }p.executeBatch();
            }
            user(c,"admin","Barangay staff","Admin@DDS2026","ADMIN",null);
            user(c,"member","Ana Santos","Member@DDS2026","MEMBER",1);
            try(PreparedStatement p=c.prepareStatement("INSERT INTO distributions(family_id,item_id,round_id,quantity,staff_id,notes) VALUES(?,1,1,1,1,'Fictional demonstration record')")) {
                for(int i=1;i<=18;i++){p.setInt(1,i);p.addBatch();}p.executeBatch();
            }
            try(Statement s=c.createStatement()) {
                s.executeUpdate("INSERT INTO stock_movements(item_id,quantity_change,reason,staff_id) VALUES(1,138,'Opening demo balance',1),(2,80,'Opening demo balance',1),(3,8,'Opening demo balance',1),(4,35,'Opening demo balance',1)");
                s.executeUpdate("INSERT INTO stock_movements(item_id,quantity_change,reason,staff_id,distribution_id) SELECT item_id,-quantity,'Demo distribution',staff_id,distribution_id FROM distributions");
                s.executeUpdate("INSERT INTO audit_logs(user_id,action,details) VALUES(1,'SETUP','Created clearly fictional demonstration data')");
                s.executeUpdate("INSERT INTO schema_info VALUES('seeded','1')");
            }
            c.commit();
        }catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
    }
    static void user(Connection c,String username,String name,String password,String role,Integer family) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO users(username,display_name,password_hash,role,family_id) VALUES(?,?,?,?,?)")) {
            p.setString(1,username);p.setString(2,name);p.setString(3,Passwords.hash(password.toCharArray()));p.setString(4,role);p.setObject(5,family);p.executeUpdate();
        }
    }
}
