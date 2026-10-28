package ph.dds;

import java.nio.file.Path;
import javax.swing.*;
import ph.dds.db.Database;
import ph.dds.service.ReliefService;
import ph.dds.ui.SystemInterface;

public final class Main {
    private Main(){}
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            JFrame splash=new JFrame("DDS - Connecting to MySQL");JLabel message=new JLabel("Preparing Davao Disaster Safety...",SwingConstants.CENTER);
            message.setBorder(BorderFactory.createEmptyBorder(30,40,30,40));splash.add(message);splash.setSize(620,150);splash.setLocationRelativeTo(null);splash.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);splash.setVisible(true);
            new SwingWorker<Database,String>(){
                @Override protected Database doInBackground()throws Exception{return Database.open(Database.projectRoot(),this::publish);}
                @Override protected void process(java.util.List<String> updates){message.setText(updates.get(updates.size()-1));}
                @Override protected void done(){try{Database db=get();splash.dispose();new SystemInterface(new ReliefService(db),Database.projectRoot()).setVisible(true);}
                    catch(Exception ex){splash.dispose();Throwable cause=ex.getCause()==null?ex:ex.getCause();JOptionPane.showMessageDialog(null,cause.getMessage()+"\n\nOpen START-HERE.md in the project folder for setup help.","DDS startup",JOptionPane.ERROR_MESSAGE);}}
            }.execute();
        });
    }
}
