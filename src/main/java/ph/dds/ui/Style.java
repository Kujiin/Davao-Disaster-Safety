package ph.dds.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

final class Style {
    static final Color NAVY=new Color(0x192C43),BG=new Color(0xF4F6F8),INK=new Color(0x203149),MUTED=new Color(0x637184),GREEN=new Color(0x37664E),AMBER=new Color(0xFFF0DE);
    private Style(){}
    static void install(){
        Font font=new Font("Segoe UI",Font.PLAIN,14);
        for(Object key:UIManager.getDefaults().keySet().toArray())if(UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)UIManager.put(key,new javax.swing.plaf.FontUIResource(font));
        UIManager.put("Panel.background",BG);UIManager.put("Label.foreground",INK);
        UIManager.put("TextField.background",Color.WHITE);UIManager.put("ComboBox.background",Color.WHITE);UIManager.put("Table.selectionBackground",new Color(0xDCE5EF));UIManager.put("Table.selectionForeground",INK);
        UIManager.put("ScrollPane.border",new EmptyBorder(0,0,0,0));
    }
    static JPanel panel(LayoutManager layout){JPanel p=new JPanel(layout);p.setOpaque(false);return p;}
    static JPanel column(){JPanel p=panel(null);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));return p;}
    static JPanel card(){JPanel p=column();p.setOpaque(true);p.setBackground(Color.WHITE);p.setBorder(new EmptyBorder(22,24,22,24));return p;}
    static JLabel label(String text,int size,boolean bold){JLabel l=new JLabel(text);l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;}
    static JButton button(String name,Runnable action){JButton b=new JButton(name);b.setBackground(new Color(0x2C2C2C));b.setForeground(Color.WHITE);b.setOpaque(true);b.setBorder(new CompoundBorder(new LineBorder(new Color(0x2C2C2C),1,true),new EmptyBorder(9,14,9,14)));b.setFocusPainted(true);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setAlignmentX(Component.LEFT_ALIGNMENT);b.addActionListener(e->action.run());return b;}
    static void gap(JPanel p,int h){p.add(Box.createVerticalStrut(h));}
    static JTextField text(String value){JTextField f=new JTextField(value,22);f.setBorder(new CompoundBorder(new LineBorder(new Color(0xCED5DD)),new EmptyBorder(8,10,8,10)));return f;}
    static void field(JPanel form,String name,JComponent input){JLabel l=label(name,14,false);l.setLabelFor(input);form.add(l);gap(form,6);input.setAlignmentX(Component.LEFT_ALIGNMENT);input.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));form.add(input);gap(form,14);}
    static JTable table(String[] cols,Object[][] rows){
        JTable t=new JTable(new DefaultTableModel(rows,cols){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int column){for(Object[] row:rows)if(row[column]!=null)return row[column].getClass();return Object.class;}
        }){
            @Override public boolean getScrollableTracksViewportWidth(){return getParent()!=null&&getPreferredSize().width<getParent().getWidth();}
            @Override public String getToolTipText(java.awt.event.MouseEvent e){int r=rowAtPoint(e.getPoint()),c=columnAtPoint(e.getPoint());return r<0||c<0?null:java.util.Objects.toString(getValueAt(r,c),"");}
        };
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);t.setRowHeight(40);t.setShowVerticalLines(false);t.setGridColor(new Color(0xE9EDF1));t.setFillsViewportHeight(true);t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setAutoCreateRowSorter(true);
        DefaultTableCellRenderer cells=new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int col){
                super.getTableCellRendererComponent(table,value,selected,focus,row,col);
                setBorder(new EmptyBorder(0,8,0,8));setHorizontalAlignment(value instanceof Number?SwingConstants.RIGHT:SwingConstants.LEFT);return this;
            }
        };
        FontMetrics metrics=t.getFontMetrics(t.getFont());for(int col=0;col<cols.length;col++){
            int width=metrics.stringWidth(cols[col])+32;
            for(int row=0;row<Math.min(rows.length,100);row++)width=Math.max(width,metrics.stringWidth(java.util.Objects.toString(rows[row][col],""))+24);
            t.getColumnModel().getColumn(col).setPreferredWidth(Math.max(80,Math.min(360,width)));t.getColumnModel().getColumn(col).setCellRenderer(cells);
        }
        t.getTableHeader().setPreferredSize(new Dimension(10,42));t.getTableHeader().setBackground(new Color(0xEDF1F5));t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));return t;
    }
    static JScrollPane scroll(Component content){JScrollPane s=new JScrollPane(content);s.getVerticalScrollBar().setUnitIncrement(20);s.getViewport().setBackground(Color.WHITE);return s;}
    static String html(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
}
