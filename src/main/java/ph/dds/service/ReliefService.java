package ph.dds.service;

import java.sql.*;
import java.util.*;
import ph.dds.db.Database;
import ph.dds.model.*;

/** Application operations and authorization, independent of Swing screens. */
public final class ReliefService {
    private final Database db;
    public ReliefService(Database db){this.db=db;}
    public User login(String username,char[] password,String role) throws SQLException {
        try(Connection c=db.connect();PreparedStatement p=c.prepareStatement("SELECT * FROM users WHERE username=? AND active=TRUE")) {
            p.setString(1,username.trim());
            try(ResultSet r=p.executeQuery()) {
                if(r.next()&&Passwords.verify(password,r.getString("password_hash"))&&role.equals(r.getString("role")))
                    return new User(r.getInt("user_id"),r.getString("username"),r.getString("display_name"),r.getString("role"),(Integer)r.getObject("family_id"));
            }return null;
        }finally{Arrays.fill(password,'\0');}
    }
    private void requireAdmin(Connection c,User actor) throws SQLException {
        if(actor==null) throw new SecurityException("Sign in first.");
        try(PreparedStatement p=c.prepareStatement("SELECT role,active FROM users WHERE user_id=?")) {
            p.setInt(1,actor.id());try(ResultSet r=p.executeQuery()) {
                if(!r.next()||!r.getBoolean("active")||!"ADMIN".equals(r.getString("role"))) throw new SecurityException("Only an administrator can change these records.");
            }
        }
    }
    private Integer allowedFamily(Connection c,User actor) throws SQLException {
        if(actor==null) throw new SecurityException("Sign in first.");
        try(PreparedStatement p=c.prepareStatement("SELECT role,family_id,active FROM users WHERE user_id=?")) {
            p.setInt(1,actor.id());try(ResultSet r=p.executeQuery()) {
                if(!r.next()||!r.getBoolean("active")) throw new SecurityException("Account is unavailable.");
                if("ADMIN".equals(r.getString("role"))) return null;
                Integer id=(Integer)r.getObject("family_id");
                if(id==null) throw new SecurityException("No family is linked to this member account.");
                return id;
            }
        }
    }
    public List<ResponseRound> rounds() throws SQLException {
        List<ResponseRound> rows=new ArrayList<>();
        try(Connection c=db.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT r.round_id,d.name,r.round_name,d.location FROM response_rounds r JOIN disasters d USING(disaster_id) ORDER BY round_id")) {
            while(r.next())rows.add(new ResponseRound(r.getInt(1),r.getString(2),r.getString(3),r.getString(4)));
        }return rows;
    }
    public List<Family> families(User actor,int round) throws SQLException {
        List<Family> rows=new ArrayList<>();
        try(Connection c=db.connect()) {
            Integer family=allowedFamily(c,actor);
            try(PreparedStatement p=c.prepareStatement("SELECT f.*,EXISTS(SELECT 1 FROM distributions d WHERE d.family_id=f.family_id AND d.round_id=?) assisted FROM families f"+(family==null?"":" WHERE f.family_id=?")+" ORDER BY f.family_id")) {
                p.setInt(1,round);if(family!=null)p.setInt(2,family);
                try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new Family(r.getInt("family_id"),r.getString("family_code"),r.getString("representative"),r.getInt("member_count"),r.getString("barangay"),r.getString("street"),r.getString("contact"),nullableDouble(r,"latitude"),nullableDouble(r,"longitude"),r.getString("location_note"),r.getBoolean("assisted")));}
            }
        }return rows;
    }
    private static Double nullableDouble(ResultSet r,String column) throws SQLException {double d=r.getDouble(column);return r.wasNull()?null:d;}
    public List<ReliefItem> items(User actor) throws SQLException {
        List<ReliefItem> rows=new ArrayList<>();
        try(Connection c=db.connect()){requireAdmin(c,actor);
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM relief_items ORDER BY item_id")) {
                while(r.next())rows.add(new ReliefItem(r.getInt("item_id"),r.getString("item_name"),r.getString("category"),r.getString("unit"),r.getInt("quantity"),r.getInt("low_stock_threshold")));
            }
        }return rows;
    }
    public void saveFamily(User actor,Family f) throws SQLException {
        if(f.representative().isBlank()||f.representative().length()>120||f.barangay().isBlank()||f.barangay().length()>100||f.street().isBlank()||f.street().length()>180||f.contact().length()>30||f.members()<1||f.members()>100)
            throw new IllegalArgumentException("Enter a representative, barangay, street, and 1-100 members. Keep contact details under 30 characters.");
        if((f.latitude()==null)!=(f.longitude()==null))throw new IllegalArgumentException("Enter both coordinates, or leave both empty.");
        if(f.latitude()!=null&&(!Double.isFinite(f.latitude())||!Double.isFinite(f.longitude())||f.latitude()<6.8||f.latitude()>7.7||f.longitude()<125.0||f.longitude()>125.8))
            throw new IllegalArgumentException("Enter valid coordinates in the Davao City area (latitude 6.8-7.7, longitude 125.0-125.8).");
        try(Connection c=db.connect()){requireAdmin(c,actor);c.setAutoCommit(false);
            try {
                String sql=f.id()==0?"INSERT INTO families(representative,member_count,barangay,street,contact,latitude,longitude,location_note,family_code) VALUES(?,?,?,?,?,?,?,?,?)":"UPDATE families SET representative=?,member_count=?,barangay=?,street=?,contact=?,latitude=?,longitude=?,location_note=? WHERE family_id=?";
                try(PreparedStatement p=c.prepareStatement(sql)) {
                    p.setString(1,f.representative().trim());p.setInt(2,f.members());p.setString(3,f.barangay().trim());p.setString(4,f.street().trim());p.setString(5,f.contact().trim());p.setObject(6,f.latitude());p.setObject(7,f.longitude());p.setString(8,f.locationNote());
                    if(f.id()==0)p.setString(9,"F-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT));else p.setInt(9,f.id());
                    if(p.executeUpdate()!=1)throw new SQLException("Family no longer exists.");
                }
                audit(c,actor,"SAVE_FAMILY",f.id()==0?"Registered family": "Updated family #"+f.id());c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }
    }
    /** UML ALT: lock stock, reject insufficient quantity, otherwise commit record + stock together. */
    public Distribution recordDistribution(User actor,int family,int item,int round,int quantity,String notes) throws SQLException {
        if(quantity<1)throw new IllegalArgumentException("Quantity must be greater than zero.");
        if(notes.length()>500)throw new IllegalArgumentException("Keep notes to 500 characters.");
        try(Connection c=db.connect()){requireAdmin(c,actor);c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("SELECT quantity FROM relief_items WHERE item_id=? FOR UPDATE")) {
                    p.setInt(1,item);try(ResultSet r=p.executeQuery()) {
                        if(!r.next())throw new IllegalArgumentException("Choose a relief item.");
                        int available=r.getInt(1);
                        if(available<quantity)throw new IllegalArgumentException("Insufficient quantity. Requested "+quantity+"; only "+available+" available. Nothing was saved.");
                    }
                }
                long id;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO distributions(family_id,item_id,round_id,quantity,staff_id,notes) VALUES(?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    p.setInt(1,family);p.setInt(2,item);p.setInt(3,round);p.setInt(4,quantity);p.setInt(5,actor.id());p.setString(6,notes.trim());p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){r.next();id=r.getLong(1);}
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE relief_items SET quantity=quantity-? WHERE item_id=?")){p.setInt(1,quantity);p.setInt(2,item);p.executeUpdate();}
                movement(c,actor,item,-quantity,"Distribution #"+id,id);
                audit(c,actor,"DISTRIBUTE","Distribution #"+id+", family #"+family+", item #"+item+", quantity "+quantity);
                c.commit();return new Distribution(id,family,item,round,quantity);
            }catch(SQLException ex){c.rollback();if(ex.getErrorCode()==1062)throw new IllegalArgumentException("Duplicate assistance: this family already received this item in this round. Nothing was saved.");throw ex;}
            catch(RuntimeException ex){c.rollback();throw ex;}
        }
    }
    public void restock(User actor,int item,int quantity,String reason) throws SQLException {
        if(quantity<1||quantity>1000000||reason.isBlank()||reason.length()>400)throw new IllegalArgumentException("Enter 1-1,000,000 units and a reason (up to 400 characters).");
        try(Connection c=db.connect()){requireAdmin(c,actor);c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("UPDATE relief_items SET quantity=quantity+? WHERE item_id=?")){p.setInt(1,quantity);p.setInt(2,item);if(p.executeUpdate()!=1)throw new IllegalArgumentException("Item not found.");}
                movement(c,actor,item,quantity,reason,null);audit(c,actor,"RESTOCK","Item #"+item+", added "+quantity+": "+reason);c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }
    }
    public void addItem(User actor,String name,String category,String unit,int threshold) throws SQLException {
        if(name.isBlank()||name.length()>100||category.isBlank()||category.length()>60||unit.isBlank()||unit.length()>30||threshold<0)throw new IllegalArgumentException("Enter an item name, category, unit and non-negative threshold.");
        try(Connection c=db.connect()){requireAdmin(c,actor);c.setAutoCommit(false);try {
            try(PreparedStatement p=c.prepareStatement("INSERT INTO relief_items(item_name,category,unit,low_stock_threshold) VALUES(?,?,?,?)")){p.setString(1,name.trim());p.setString(2,category.trim());p.setString(3,unit.trim());p.setInt(4,threshold);p.executeUpdate();}
            audit(c,actor,"ADD_ITEM",name);c.commit();
        }catch(Exception ex){c.rollback();throw ex;}}
    }
    private static void movement(Connection c,User actor,int item,int change,String reason,Long distribution) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO stock_movements(item_id,quantity_change,reason,staff_id,distribution_id) VALUES(?,?,?,?,?)")){p.setInt(1,item);p.setInt(2,change);p.setString(3,reason);p.setInt(4,actor.id());p.setObject(5,distribution);p.executeUpdate();}
    }
    private static void audit(Connection c,User actor,String action,String details) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO audit_logs(user_id,action,details) VALUES(?,?,?)")){p.setInt(1,actor.id());p.setString(2,action);p.setString(3,details);p.executeUpdate();}
    }
    public Object[][] history(User actor,int round) throws SQLException {
        try(Connection c=db.connect()){
            Integer family=allowedFamily(c,actor);
            try(PreparedStatement p=c.prepareStatement("SELECT d.distribution_id,f.family_code,f.representative,i.item_name,d.quantity,i.unit,u.display_name,d.distributed_at,d.notes FROM distributions d JOIN families f USING(family_id) JOIN relief_items i USING(item_id) JOIN users u ON u.user_id=d.staff_id WHERE d.round_id=?"+(family==null?"":" AND d.family_id=?")+" ORDER BY d.distributed_at DESC,d.distribution_id DESC")) {
                p.setInt(1,round);if(family!=null)p.setInt(2,family);return rows(p,5000);
            }
        }
    }
    public List<String> tableNames(User actor) throws SQLException {
        try(Connection c=db.connect()){requireAdmin(c,actor);List<String> names=new ArrayList<>();try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SHOW TABLES")){while(r.next())names.add(r.getString(1));}return names;}
    }
    public record TableData(String[] columns,Object[][] rows){}
    public TableData inspectTable(User actor,String table) throws SQLException {
        if(!tableNames(actor).contains(table))throw new IllegalArgumentException("Unknown table.");
        try(Connection c=db.connect()){requireAdmin(c,actor);
            // Never display password hashes, even in the professor's table viewer.
            String columns=table.equals("users")?"user_id,username,display_name,role,family_id,active":"*";
            try(PreparedStatement p=c.prepareStatement("SELECT "+columns+" FROM `"+table+"` LIMIT 500" );ResultSet r=p.executeQuery()) {
                ResultSetMetaData m=r.getMetaData();String[] headings=new String[m.getColumnCount()];for(int i=0;i<headings.length;i++)headings[i]=m.getColumnLabel(i+1);
                return new TableData(headings,read(r,500));
            }
        }
    }
    private static Object[][] rows(PreparedStatement p,int max) throws SQLException {try(ResultSet r=p.executeQuery()){return read(r,max);}}
    private static Object[][] read(ResultSet r,int max) throws SQLException {List<Object[]> list=new ArrayList<>();int n=r.getMetaData().getColumnCount();while(r.next()&&list.size()<max){Object[] row=new Object[n];for(int i=0;i<n;i++)row[i]=r.getObject(i+1);list.add(row);}return list.toArray(Object[][]::new);}
    public String serverVersion(User actor) throws SQLException {try(Connection c=db.connect()){requireAdmin(c,actor);return c.getMetaData().getDatabaseProductName()+" "+c.getMetaData().getDatabaseProductVersion();}}
}
