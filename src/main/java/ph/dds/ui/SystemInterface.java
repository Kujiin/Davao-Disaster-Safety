package ph.dds.ui;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import ph.dds.model.*;
import ph.dds.service.ReliefService;

/** Swing view/controller, corresponding to SystemInterface in the UML. */
public final class SystemInterface extends JFrame {
    private final ReliefService service;private final Path root;
    private User user;private ResponseRound round;private String page="Overview";
    private JPanel body;private JComboBox<ResponseRound> roundChoice;private JLabel busy;
    private List<Family> families=List.of();private List<ReliefItem> items=List.of();private Object[][] history=new Object[0][];
    private final java.util.Set<JButton> operationButtons=new HashSet<>();
    private boolean working;private int requestGeneration;
    private static final String[] HISTORY={"Record","Family ID","Representative","Relief item","Quantity","Unit","Recorded by","Date / time","Notes"};
    public SystemInterface(ReliefService service,Path root){
        super("DDS | Davao Disaster Safety");this.service=service;this.root=root;Style.install();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);setMinimumSize(new Dimension(1050,720));setSize(1200,820);setLocationRelativeTo(null);showLogin();
    }
    private <T> void task(Callable<T> work,Consumer<T> done){
        if(working)return;working=true;setOperationsEnabled(false);if(busy!=null)busy.setText("Working...");
        new SwingWorker<T,Void>(){
            @Override protected T doInBackground()throws Exception{return work.call();}
            @Override protected void done(){working=false;setOperationsEnabled(true);if(busy!=null)busy.setText("MySQL connected | Demo data");try{done.accept(get());}catch(Exception ex){Throwable c=ex;while(c.getCause()!=null)c=c.getCause();error(c.getMessage()==null?c.toString():c.getMessage());}}
        }.execute();
    }
    private void setOperationsEnabled(boolean enabled){operationButtons.forEach(b->b.setEnabled(enabled));if(roundChoice!=null)roundChoice.setEnabled(enabled);}
    private JButton action(String name,Runnable handler){JButton b=Style.button(name,handler);operationButtons.add(b);b.setEnabled(!working);return b;}
    private void error(String message){JOptionPane.showMessageDialog(this,message,"Please review",JOptionPane.WARNING_MESSAGE);}
    private void showLogin(){
        requestGeneration++;operationButtons.clear();roundChoice=null;user=null;
        JPanel login=Style.panel(new GridLayout(1,2,0,0));login.setOpaque(true);login.setBackground(Style.BG);
        JPanel identity=Style.column();identity.setOpaque(true);identity.setBackground(Style.NAVY);identity.setBorder(BorderFactory.createEmptyBorder(85,48,40,40));
        JLabel brand=Style.label("DAVAO DISASTER SAFETY",22,true);brand.setForeground(new Color(0xFFAA51));identity.add(brand);Style.gap(identity,24);
        JLabel slogan=Style.label("<html>Every family.<br>Every record.<br>Every assistance.</html>",34,true);slogan.setForeground(Color.WHITE);identity.add(slogan);Style.gap(identity,30);
        JLabel description=Style.label("<html>Davao Disaster Assistance<br>Tracking System</html>",19,false);description.setForeground(new Color(0xDCE5EF));identity.add(description);Style.gap(identity,24);
        JLabel copy=Style.label("<html>Organize family records, track relief goods,<br>and see who still needs support.</html>",15,false);copy.setForeground(new Color(0xDCE5EF));identity.add(copy);
        JPanel right=Style.panel(new GridBagLayout());JPanel form=Style.card();form.setPreferredSize(new Dimension(440,570));form.add(Style.label("Welcome to DDS",30,true));Style.gap(form,12);form.add(Style.label("Sign in to your workspace.",15,false));Style.gap(form,26);
        JTextField username=Style.text("admin");JPasswordField password=new JPasswordField(22);password.setBorder(username.getBorder());Style.field(form,"Username",username);Style.field(form,"Password",password);
        JButton admin=action("Sign in as admin",()->signIn(username,password,"ADMIN"));JButton member=action("Sign in as member",()->signIn(username,password,"MEMBER"));form.add(admin);Style.gap(form,12);form.add(member);Style.gap(form,28);
        JLabel hint=Style.label("<html><b>demo accounts</b><br>Admin: admin / Admin@DDS2026<br>Member: member / Member@DDS2026<br><br>All household records are fictional.</html>",13,false);hint.setForeground(Style.MUTED);form.add(hint);right.add(form);login.add(identity);login.add(right);
        setContentPane(login);getRootPane().setDefaultButton(admin);password.addActionListener(e->admin.doClick());revalidate();repaint();
    }
    private void signIn(JTextField username,JPasswordField password,String role){
        String name=username.getText();char[] secret=password.getPassword();password.setText("");
        task(()->service.login(name,secret,role),found->{if(found==null){error("Username, password, or selected role is incorrect.");return;}user=found;task(service::rounds,rounds->{if(rounds.isEmpty()){error("No response rounds are configured.");return;}round=rounds.get(0);buildShell(rounds);refresh();});});
    }
    private void buildShell(List<ResponseRound> rounds){
        operationButtons.clear();JPanel shell=Style.panel(new BorderLayout());shell.setOpaque(true);shell.setBackground(Style.BG);
        JPanel top=Style.column();JPanel bar=Style.panel(new BorderLayout(20,0));bar.setOpaque(true);bar.setBackground(Style.NAVY);bar.setBorder(BorderFactory.createEmptyBorder(18,24,18,24));
        JLabel brand=Style.label("DDS  /  DAVAO DISASTER SAFETY",17,true);brand.setForeground(Color.WHITE);bar.add(brand,BorderLayout.WEST);
        JPanel account=Style.panel(new FlowLayout(FlowLayout.RIGHT,18,0));JLabel who=Style.label(user.role()+" / "+user.name(),13,false);who.setForeground(new Color(0xDCE5EF));account.add(who);account.add(action("Log out",()->{if(!working)showLogin();}));bar.add(account,BorderLayout.EAST);top.add(bar);
        JPanel nav=Style.panel(new BorderLayout());nav.setBorder(BorderFactory.createEmptyBorder(10,24,8,24));JPanel links=Style.panel(new FlowLayout(FlowLayout.LEFT,7,0));
        String[] pages=user.isAdmin()?new String[]{"Overview","Families","Record assistance","Supplies","History","Map","Database tables"}:new String[]{"My family assistance","Map"};
        page=pages[0];for(String name:pages)links.add(action(name,()->{page=name;refresh();}));
        nav.add(links,BorderLayout.CENTER);top.add(nav);
        JPanel selector=Style.panel(new FlowLayout(FlowLayout.LEFT,10,3));selector.setBorder(BorderFactory.createEmptyBorder(0,24,4,24));selector.add(Style.label("Response round",13,true));roundChoice=new JComboBox<>(rounds.toArray(ResponseRound[]::new));roundChoice.setSelectedItem(round);selector.add(roundChoice);roundChoice.addActionListener(e->{if(!working){round=(ResponseRound)roundChoice.getSelectedItem();refresh();}});top.add(selector);
        shell.add(top,BorderLayout.NORTH);body=Style.panel(new BorderLayout(20,20));body.setBorder(BorderFactory.createEmptyBorder(18,28,20,28));shell.add(body,BorderLayout.CENTER);
        busy=Style.label("MySQL connected | Demo data",12,false);busy.setForeground(Style.MUTED);busy.setBorder(BorderFactory.createEmptyBorder(4,28,10,28));shell.add(busy,BorderLayout.SOUTH);setContentPane(shell);getRootPane().setDefaultButton(null);revalidate();repaint();
    }
    private record Snapshot(List<Family> families,List<ReliefItem> items,Object[][] history){}
    private void refresh(){
        if(user==null)return;final User actor=user;final int currentRound=round.id();final int gen=++requestGeneration;
        task(()->new Snapshot(service.families(actor,currentRound),actor.isAdmin()?service.items(actor):List.of(),service.history(actor,currentRound)),data->{
            if(gen!=requestGeneration||user!=actor)return;families=data.families();items=data.items();history=data.history();renderPage();
        });
    }
    private void heading(String title){
        JPanel h=Style.column();h.add(Style.label(title,28,true));Style.gap(h,7);JLabel sub=Style.label(round.disaster()+" | "+round.location()+" | "+round.name(),14,false);sub.setForeground(Style.MUTED);h.add(sub);body.add(h,BorderLayout.NORTH);
    }
    private void renderPage(){
        body.removeAll();
        switch(page){case "Overview"->overview();case "Families"->familyRecords();case "Record assistance"->distributionForm();case "Supplies"->supplies();case "History"->history();case "Map"->map();case "Database tables"->databaseTables();default->member();}
        body.revalidate();body.repaint();
    }
    private void overview(){
        heading("Response overview");JPanel grid=Style.panel(new GridBagLayout());GridBagConstraints c=new GridBagConstraints();c.fill=GridBagConstraints.BOTH;c.weighty=1;c.insets=new Insets(0,0,20,20);
        long assisted=families.stream().filter(Family::assisted).count();int count=families.size();
        JPanel summary=Style.card();summary.add(Style.label("CURRENT RESPONSE",12,true));Style.gap(summary,18);summary.add(Style.label(count+" families registered",32,true));Style.gap(summary,16);summary.add(Style.label(assisted+" received aid  /  "+(count-assisted)+" awaiting assistance",21,false));Style.gap(summary,14);JLabel percent=Style.label((count==0?0:Math.round(assisted*100.0/count))+"% received at least one item this round",14,false);percent.setForeground(Style.GREEN);summary.add(percent);
        c.gridx=0;c.gridy=0;c.weightx=.65;grid.add(summary,c);
        JPanel alert=Style.card();alert.setBackground(Style.AMBER);alert.add(Style.label("SUPPLIES NEED ATTENTION",12,true));Style.gap(alert,18);List<ReliefItem> low=items.stream().filter(i->i.quantity()<=i.threshold()).toList();ReliefItem first=low.isEmpty()?null:low.get(0);alert.add(Style.label(first==null?"Stock levels look good":first.quantity()+" "+first.name().toLowerCase(Locale.ROOT)+" left",23,true));Style.gap(alert,14);alert.add(action("View supplies",()->{page="Supplies";refresh();}));
        c.gridx=1;c.weightx=.35;c.insets=new Insets(0,0,20,0);grid.add(alert,c);
        JPanel tasks=Style.card();tasks.add(Style.label("Manage assistance",22,true));for(String[] link:new String[][]{{"Family records","Families"},{"Check maps","Map"},{"Record assistance","Record assistance"},{"Available supplies","Supplies"}}){Style.gap(tasks,10);tasks.add(action(link[0],()->{page=link[1];refresh();}));}
        c.gridx=0;c.gridy=1;c.weightx=.65;c.insets=new Insets(0,0,0,20);grid.add(tasks,c);
        JPanel about=Style.card();about.add(Style.label("A clear record of support",22,true));Style.gap(about,16);about.add(Style.label("<html>One family, item and round per record.<br><br>Stock and assistance save together.<br><br>Review history and MySQL tables.<br><br><font color='#637184'>Classroom demo: fictional households.<br>Places use real Davao City names.</font></html>",15,false));c.gridx=1;c.weightx=.35;c.insets=new Insets(0,0,0,0);grid.add(about,c);body.add(grid,BorderLayout.CENTER);
    }
    private void familyRecords(){
        heading("Family records");JPanel p=Style.panel(new BorderLayout(10,12));JPanel tools=Style.panel(new FlowLayout(FlowLayout.LEFT,8,0));JTextField search=Style.text("");search.setColumns(25);search.getAccessibleContext().setAccessibleName("Search family records");tools.add(Style.label("Search",14,true));tools.add(search);
        Object[][] rows=families.stream().map(f->new Object[]{f.code(),f.representative(),f.members(),f.barangay(),f.street(),f.assisted()?"Received item(s)":"Awaiting aid"}).toArray(Object[][]::new);
        JTable table=Style.table(new String[]{"Family ID","Representative","Members","Barangay","Street","Round status"},rows);
        TableRowSorter<?> sorter=(TableRowSorter<?>)table.getRowSorter();search.getDocument().addDocumentListener(new DocumentListener(){private void filter(){sorter.setRowFilter(search.getText().isBlank()?null:RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(search.getText())));}@Override public void insertUpdate(DocumentEvent e){filter();}@Override public void removeUpdate(DocumentEvent e){filter();}@Override public void changedUpdate(DocumentEvent e){filter();}});
        tools.add(action("Register family",()->familyDialog(null)));tools.add(action("Edit selected",()->{int selected=table.getSelectedRow();if(selected<0){error("Select a family first.");return;}familyDialog(families.get(table.convertRowIndexToModel(selected)));}));p.add(tools,BorderLayout.NORTH);p.add(Style.scroll(table),BorderLayout.CENTER);body.add(p,BorderLayout.CENTER);
    }
    private void familyDialog(Family original){
        JDialog dialog=new JDialog(this,original==null?"Register family":"Edit family",true);JPanel f=Style.card();
        JTextField name=Style.text(original==null?"":original.representative()),street=Style.text(original==null?"":original.street()),contact=Style.text(original==null?"":original.contact());
        JComboBox<String> barangay=new JComboBox<>(new String[]{"Matina Crossing","Matina Aplaya","Matina Pangi","Bucana","Talomo Proper","Buhangin Proper","Sasa","Tigatto","Ma-a","Bago Gallera"});barangay.setEditable(true);if(original!=null)barangay.setSelectedItem(original.barangay());
        JSpinner count=new JSpinner(new SpinnerNumberModel(original==null?1:original.members(),1,100,1));
        JTextField lat=Style.text(original==null||original.latitude()==null?"":original.latitude().toString()),lon=Style.text(original==null||original.longitude()==null?"":original.longitude().toString());
        Style.field(f,"Family representative *",name);Style.field(f,"Number of family members *",count);Style.field(f,"Davao City barangay *",barangay);Style.field(f,"Street / sitio / purok *",street);Style.field(f,"Contact number (optional)",contact);
        JPanel coords=Style.panel(new GridLayout(1,2,15,0));JPanel a=Style.column(),b=Style.column();Style.field(a,"Latitude (optional)",lat);Style.field(b,"Longitude (optional)",lon);coords.add(a);coords.add(b);coords.setAlignmentX(Component.LEFT_ALIGNMENT);coords.setMaximumSize(new Dimension(Integer.MAX_VALUE,82));f.add(coords);
        JLabel note=Style.label("<html>Leave both coordinates empty if unknown.<br>Coordinates are approximate; confirm before operational use.</html>",12,false);note.setForeground(Style.MUTED);f.add(note);Style.gap(f,18);
        JButton save=action("Save family",()->{
            try{count.commitEdit();Double latitude=lat.getText().isBlank()?null:Double.valueOf(lat.getText().trim());Double longitude=lon.getText().isBlank()?null:Double.valueOf(lon.getText().trim());
                Family data=new Family(original==null?0:original.id(),original==null?"":original.code(),name.getText(),(int)count.getValue(),Objects.toString(barangay.getSelectedItem(),""),street.getText(),contact.getText(),latitude,longitude,"User-entered approximate location",false);
                task(()->{service.saveFamily(user,data);return true;},ok->{dialog.dispose();refresh();});
            }catch(Exception ex){error("Review the family details: "+ex.getMessage());}
        });f.add(save);dialog.add(Style.scroll(f));dialog.setSize(580,690);dialog.setLocationRelativeTo(this);dialog.setVisible(true);
    }
    private void distributionForm(){
        heading("Record assistance");JPanel columns=Style.panel(new GridLayout(1,2,24,0));JPanel f=Style.card();
        JComboBox<Family> family=new JComboBox<>(families.toArray(Family[]::new));JComboBox<ReliefItem> item=new JComboBox<>(items.toArray(ReliefItem[]::new));JSpinner quantity=new JSpinner(new SpinnerNumberModel(1,1,1000000,1));JTextField notes=Style.text("");
        Style.field(f,"1. Select family",family);Style.field(f,"2. Check relief item",item);Style.field(f,"3. Enter quantity",quantity);Style.field(f,"Notes (optional, up to 500 characters)",notes);
        JLabel details=Style.label("",13,false);details.setForeground(Style.MUTED);f.add(details);Style.gap(f,18);
        Runnable update=()->{Family selected=(Family)family.getSelectedItem();ReliefItem supply=(ReliefItem)item.getSelectedItem();details.setText("<html>"+(selected==null?"No family available":Style.html(selected.barangay()+", "+selected.street()))+"<br>"+(supply==null?"No supply available":supply.quantity()+" "+Style.html(supply.unit())+" currently in stock")+"</html>");};family.addActionListener(e->update.run());item.addActionListener(e->update.run());update.run();
        f.add(action("4. Record distribution",()->{
            try{quantity.commitEdit();}catch(java.text.ParseException ex){error("Enter a whole-number quantity.");return;}
            Family selected=(Family)family.getSelectedItem();ReliefItem supply=(ReliefItem)item.getSelectedItem();if(selected==null||supply==null){error("Register a family and a relief item first.");return;}
            int q=(int)quantity.getValue();
            if(JOptionPane.showConfirmDialog(this,"Give "+q+" "+supply.unit()+" of "+supply.name()+" to "+selected.representative()+"?\n"+round+"\nThis will record assistance and deduct stock.","Confirm distribution",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            task(()->service.recordDistribution(user,selected.id(),supply.id(),round.id(),q,notes.getText()),saved->{JOptionPane.showMessageDialog(this,"Assistance recorded. Receipt #"+saved.id()+"\nQuantity: "+saved.quantity()+"\nInventory has been updated.","Assistance saved",JOptionPane.INFORMATION_MESSAGE);page="History";refresh();});
        }));
        JPanel guide=Style.card();guide.add(Style.label("A safe, complete record",22,true));Style.gap(guide,20);guide.add(Style.label("<html>Select family → Check item → Enter quantity<br><br>Record distribution → Save → Update inventory<br><br><b>If stock is sufficient</b><br>Save the record and deduct stock together.<br><br><b>If stock is insufficient</b><br>Show a warning. Save nothing.<br><br><b>If this item was already given this round</b><br>Show a duplicate warning. Save nothing.</html>",15,false));columns.add(f);columns.add(guide);body.add(Style.scroll(columns),BorderLayout.CENTER);
    }
    private void supplies(){
        heading("Available supplies");JPanel p=Style.panel(new BorderLayout(10,12));Object[][] rows=items.stream().map(i->new Object[]{i.id(),i.name(),i.category(),i.quantity(),i.unit(),i.threshold(),i.quantity()<=i.threshold()?"Low stock":"Available"}).toArray(Object[][]::new);JTable table=Style.table(new String[]{"ID","Item","Category","In stock","Unit","Alert at","Status"},rows);
        JPanel tools=Style.panel(new FlowLayout(FlowLayout.LEFT,8,0));tools.add(action("Receive stock",()->{int row=table.getSelectedRow();if(row<0){error("Select an item first.");return;}restockDialog(items.get(table.convertRowIndexToModel(row)));}));tools.add(action("Add relief item",this::addItemDialog));tools.add(action("Refresh",this::refresh));p.add(tools,BorderLayout.NORTH);p.add(Style.scroll(table),BorderLayout.CENTER);body.add(p,BorderLayout.CENTER);
    }
    private void restockDialog(ReliefItem item){
        JPanel f=Style.column();JSpinner amount=new JSpinner(new SpinnerNumberModel(1,1,1000000,1));JTextField reason=Style.text("");Style.field(f,"Quantity received ("+item.unit()+")",amount);Style.field(f,"Source / reason *",reason);
        if(JOptionPane.showConfirmDialog(this,f,"Receive stock: "+item.name(),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{amount.commitEdit();int q=(int)amount.getValue();task(()->{service.restock(user,item.id(),q,reason.getText());return true;},ok->refresh());}catch(java.text.ParseException ex){error("Enter a whole-number quantity.");}
    }
    private void addItemDialog(){
        JPanel f=Style.column();JTextField name=Style.text(""),category=Style.text(""),unit=Style.text("");JSpinner threshold=new JSpinner(new SpinnerNumberModel(10,0,1000000,1));Style.field(f,"Item name *",name);Style.field(f,"Category *",category);Style.field(f,"Unit (packs, kits, bottles...) *",unit);Style.field(f,"Low stock alert threshold",threshold);
        if(JOptionPane.showConfirmDialog(this,f,"Add relief item",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{threshold.commitEdit();task(()->{service.addItem(user,name.getText(),category.getText(),unit.getText(),(int)threshold.getValue());return true;},ok->refresh());}catch(java.text.ParseException ex){error("Enter a whole-number threshold.");}
    }
    private void history(){
        heading("Assistance history");JPanel p=Style.panel(new BorderLayout(10,12));JTable t=Style.table(HISTORY,history);JPanel tools=Style.panel(new FlowLayout(FlowLayout.LEFT));tools.add(action("Export this round to CSV",()->exportCsv(t)));tools.add(Style.label(history.length+" records shown (latest 5,000 maximum)",13,false));p.add(tools,BorderLayout.NORTH);p.add(Style.scroll(t),BorderLayout.CENTER);body.add(p,BorderLayout.CENTER);
    }
    private void member(){
        heading("My family assistance");JPanel p=Style.panel(new BorderLayout(10,18));JPanel info=Style.card();if(families.isEmpty())info.add(Style.label("No family record linked to this account.",20,true));else{Family f=families.get(0);info.add(Style.label(f.representative()+" | "+f.code(),23,true));Style.gap(info,12);info.add(Style.label(f.members()+" members | "+f.barangay()+" | "+f.street(),14,false));Style.gap(info,12);info.add(Style.label(f.assisted()?"Received item(s) this round. See details below.":"Awaiting assistance in this round.",16,true));}p.add(info,BorderLayout.NORTH);p.add(Style.scroll(Style.table(HISTORY,history)),BorderLayout.CENTER);body.add(p,BorderLayout.CENTER);
    }
    private void map(){
        heading("Map");JPanel p=Style.panel(new BorderLayout(12,12));MapPanel map=new MapPanel(families,root);JPanel controls=Style.panel(new FlowLayout(FlowLayout.LEFT,8,0));controls.add(action("+",()->map.changeZoom(1)));controls.add(action("−",()->map.changeZoom(-1)));controls.add(action("Davao / Matina",map::home));JComboBox<Family> focus=new JComboBox<>(families.stream().filter(f->f.latitude()!=null).toArray(Family[]::new));focus.setSelectedIndex(-1);focus.setPreferredSize(new Dimension(310,35));focus.getAccessibleContext().setAccessibleName("Focus family marker");focus.addActionListener(e->map.focus((Family)focus.getSelectedItem()));controls.add(focus);p.add(controls,BorderLayout.NORTH);p.add(map,BorderLayout.CENTER);
        JPanel footer=Style.column();JLabel legend=Style.label("Green: received item(s) | Orange: awaiting aid | Fictional households; approximate demo points.",13,false);footer.add(legend);Style.gap(footer,5);JLabel attribution=Style.label("<html><a href=''>© OpenStreetMap contributors</a> | Online street map, not live disaster tracking. No API key needed.</html>",12,false);attribution.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));attribution.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){try{Desktop.getDesktop().browse(URI.create("https://www.openstreetmap.org/copyright"));}catch(IOException ex){error("Map credit: https://www.openstreetmap.org/copyright");}}});footer.add(attribution);p.add(footer,BorderLayout.SOUTH);body.add(p,BorderLayout.CENTER);
    }
    private void databaseTables(){
        heading("MySQL database tables");JPanel p=Style.panel(new BorderLayout(10,12));JPanel tools=Style.panel(new FlowLayout(FlowLayout.LEFT,10,0));JComboBox<String> names=new JComboBox<>();tools.add(Style.label("Table",14,true));tools.add(names);JLabel version=Style.label("",12,false);tools.add(version);p.add(tools,BorderLayout.NORTH);JPanel tableHolder=Style.panel(new BorderLayout());p.add(tableHolder,BorderLayout.CENTER);p.add(Style.label("Read-only. First 500 rows per table. Password hashes are excluded from the users view.",12,false),BorderLayout.SOUTH);body.add(p,BorderLayout.CENTER);
        task(()->new Object[]{service.tableNames(user),service.serverVersion(user)},result->{
            @SuppressWarnings("unchecked") List<String> tables=(List<String>)result[0];version.setText(result[1].toString());tables.forEach(names::addItem);
            Runnable show=()->{String selected=(String)names.getSelectedItem();if(selected==null)return;task(()->service.inspectTable(user,selected),data->{tableHolder.removeAll();tableHolder.add(Style.scroll(Style.table(data.columns(),data.rows())));tableHolder.revalidate();tableHolder.repaint();});};
            names.addActionListener(e->show.run());show.run();
        });
    }
    private void exportCsv(JTable t){
        JFileChooser chooser=new JFileChooser(root.toFile());chooser.setSelectedFile(root.resolve("assistance-round-"+round.id()+".csv").toFile());if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path file=chooser.getSelectedFile().toPath();if(Files.exists(file)&&JOptionPane.showConfirmDialog(this,"Replace the existing file?","Export",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        StringBuilder csv=new StringBuilder("\uFEFF");for(int i=-1;i<t.getRowCount();i++){for(int j=0;j<t.getColumnCount();j++){if(j>0)csv.append(',');String value=i<0?t.getColumnName(j):Objects.toString(t.getValueAt(i,j),"");if(value.matches("^[=+@\\-].*"))value="'"+value;csv.append('"').append(value.replace("\"","\"\"")).append('"');}csv.append("\r\n");}
        task(()->{Files.writeString(file,csv.toString(),StandardCharsets.UTF_8);return file;},saved->JOptionPane.showMessageDialog(this,"Export saved:\n"+saved));
    }
}
