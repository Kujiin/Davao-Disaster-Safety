package ph.dds.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ph.dds.db.Database;
import ph.dds.service.ReliefService;

/** Optional visible Swing smoke check. Invoke with -Ddds.uiTest=true. */
class SwingSmokeTest {
    private SystemInterface window;private Path folder;
    private final List<String> errors=new ArrayList<>();
    @Test void checkScreensAndMemberView()throws Exception{
        Assumptions.assumeTrue(Boolean.getBoolean("dds.uiTest"),"Opt in to visible Swing checks with -Ddds.uiTest=true");
        Database db=Database.open(Database.projectRoot(),s->{});folder=Database.projectRoot().resolve("docs/screenshots");Files.createDirectories(folder);
        SwingUtilities.invokeAndWait(()->{window=new SystemInterface(new ReliefService(db),Database.projectRoot());window.setVisible(true);});
        try{
            capture("01-sign-in");
            edt(()->{find(JPasswordField.class).get(0).setText("Admin@DDS2026");button("Sign in as admin").doClick();});
            waitFor(()->has("Response overview"));capture("02-overview");
            for(String[] page:new String[][]{{"Families","Family records","03-families"},{"Record assistance","Record assistance","04-distribution"},{"Supplies","Available supplies","05-supplies"},{"History","Assistance history","06-history"},{"Database tables","MySQL database tables","07-database"}}){
                edt(()->button(page[0]).doClick());waitIdle();waitFor(()->has(page[1]));Thread.sleep(180);capture(page[2]);
            }
            edt(()->button("Map").doClick());waitIdle();waitFor(()->!find(MapPanel.class).isEmpty());Thread.sleep(5500);capture("08-map");
            edt(()->button("Log out").doClick());waitFor(()->has("Welcome to DDS"));
            edt(()->{find(JTextField.class).stream().filter(f->!(f instanceof JPasswordField)).findFirst().orElseThrow().setText("member");find(JPasswordField.class).get(0).setText("Member@DDS2026");button("Sign in as member").doClick();});
            waitFor(()->has("My family assistance"));waitIdle();capture("09-member");assertFalse(has("Database tables"));assertFalse(has("Supplies"));assertEquals(1,find(JTable.class).get(0).getRowCount());
        }finally{edt(()->window.dispose());}
        assertTrue(errors.isEmpty(),String.join("\n",errors));
    }
    private void edt(Runnable r)throws Exception{SwingUtilities.invokeAndWait(r);}
    private <T extends Component> List<T> find(Class<T> type){List<T> all=new ArrayList<>();walk(window,type,all);return all;}
    private static <T extends Component> void walk(Component component,Class<T> type,List<T> all){if(type.isInstance(component))all.add(type.cast(component));if(component instanceof Container c)for(Component child:c.getComponents())walk(child,type,all);}
    private JButton button(String name){return find(JButton.class).stream().filter(b->name.equals(b.getText())).findFirst().orElseThrow();}
    private boolean has(String label){return find(JLabel.class).stream().anyMatch(l->label.equals(l.getText()));}
    private void waitFor(java.util.function.BooleanSupplier condition)throws Exception{for(int i=0;i<200;i++){AtomicReference<Boolean> value=new AtomicReference<>(false);edt(()->value.set(condition.getAsBoolean()));if(value.get())return;Thread.sleep(100);}fail("Screen did not finish loading");}
    private void waitIdle()throws Exception{Thread.sleep(200);waitFor(()->find(JButton.class).stream().filter(b->"Log out".equals(b.getText())).allMatch(Component::isEnabled));}
    private void capture(String name)throws Exception{
        edt(()->{try{double scale=window.getGraphicsConfiguration().getDefaultTransform().getScaleX();BufferedImage image=new BufferedImage((int)(window.getWidth()*scale),(int)(window.getHeight()*scale),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.scale(scale,scale);window.paint(g);g.dispose();ImageIO.write(image,"png",folder.resolve(name+".png").toFile());}catch(Exception ex){throw new RuntimeException(ex);}});
    }
}
