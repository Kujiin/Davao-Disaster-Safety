package ph.dds.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import ph.dds.model.Family;

/** Native Swing slippy map. No JavaScript, browser, JavaFX or API key. */
public final class MapPanel extends JPanel {
    private int zoom=14; private double lat=7.052,lon=125.581;
    private final List<Family> families;
    private final Path cache;
    private final Map<String,BufferedImage> images=Collections.synchronizedMap(new LinkedHashMap<>(256,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<String,BufferedImage> e){return size()>256;}});
    private final Set<String> pending=ConcurrentHashMap.newKeySet();
    private final Map<String,Long> failures=new ConcurrentHashMap<>();
    private final ThreadPoolExecutor pool=new ThreadPoolExecutor(2,2,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(64),r->{Thread t=new Thread(r,"DDS-map-tile");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy());
    private Point drag; private volatile boolean closed; private String status="Drag to pan. Scroll to zoom. Online street map.";
    public MapPanel(List<Family> families,Path projectRoot){
        this.families=List.copyOf(families);this.cache=projectRoot.resolve("runtime/map-cache");setPreferredSize(new Dimension(880,428));setBackground(new Color(0xE7ECEF));
        setToolTipText("Approximate demo family locations; not live disaster data");
        MouseAdapter mouse=new MouseAdapter(){
            @Override public void mousePressed(MouseEvent e){drag=e.getPoint();}
            @Override public void mouseDragged(MouseEvent e){if(drag!=null){double x=x(lon,zoom)-(e.getX()-drag.x),y=y(lat,zoom)-(e.getY()-drag.y);lon=longitude(x,zoom);lat=latitude(y,zoom);clamp();drag=e.getPoint();repaint();}}
            @Override public void mouseWheelMoved(MouseWheelEvent e){changeZoom(-e.getWheelRotation());}
            @Override public void mouseReleased(MouseEvent e){drag=null;}
        };addMouseListener(mouse);addMouseMotionListener(mouse);addMouseWheelListener(mouse);
    }
    public void focus(Family f){if(f!=null&&f.latitude()!=null){lat=f.latitude();lon=f.longitude();zoom=16;repaint();}}
    public void home(){lat=7.052;lon=125.581;zoom=14;repaint();}
    public void changeZoom(int delta){zoom=Math.max(11,Math.min(18,zoom+delta));repaint();}
    private void clamp(){lat=Math.max(-85,Math.min(85,lat));lon=Math.max(-180,Math.min(180,lon));}
    private static double x(double lon,int z){return (lon+180)/360*(1<<z)*256;}
    private static double y(double lat,int z){double a=Math.toRadians(lat);return (1-Math.log(Math.tan(a)+1/Math.cos(a))/Math.PI)/2*(1<<z)*256;}
    private static double longitude(double x,int z){return x/((1<<z)*256)*360-180;}
    private static double latitude(double y,int z){return Math.toDegrees(Math.atan(Math.sinh(Math.PI*(1-2*y/((1<<z)*256)))));}
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        double left=x(lon,zoom)-getWidth()/2.0,top=y(lat,zoom)-getHeight()/2.0;
        int tx=(int)Math.floor(left/256),ty=(int)Math.floor(top/256);
        for(int cx=tx;cx<=Math.floor((left+getWidth())/256);cx++)for(int cy=ty;cy<=Math.floor((top+getHeight())/256);cy++){
            if(cx<0||cy<0||cx>=(1<<zoom)||cy>=(1<<zoom))continue;
            String key=zoom+"/"+cx+"/"+cy;BufferedImage image=images.get(key);int px=(int)(cx*256-left),py=(int)(cy*256-top);
            if(image!=null)g.drawImage(image,px,py,256,256,null);
            else {g.setColor(new Color(0xD9E0E5));g.drawRect(px,py,256,256);if(!closed&&isShowing())request(key);}
        }
        for(Family f:families){if(f.latitude()==null)continue;int px=(int)(x(f.longitude(),zoom)-left),py=(int)(y(f.latitude(),zoom)-top);if(px<0||py<0||px>getWidth()||py>getHeight())continue;
            g.setColor(f.assisted()?Style.GREEN:new Color(0xC37A26));g.fillOval(px-7,py-7,14,14);g.setColor(Color.WHITE);g.setStroke(new BasicStroke(2));g.drawOval(px-7,py-7,14,14);
            if(zoom>=16){g.setFont(new Font("Segoe UI",Font.BOLD,12));g.setColor(Style.NAVY);g.drawString(f.code(),px+10,py+4);}
        }
        g.setColor(new Color(255,255,255,235));g.fillRect(0,0,getWidth(),29);g.setColor(Style.INK);g.setFont(new Font("Segoe UI",Font.PLAIN,12));g.drawString(status,12,20);g.dispose();
    }
    private void request(String key){
        if(failures.getOrDefault(key,0L)>System.currentTimeMillis()||!pending.add(key))return;
        try{pool.execute(()->{
            try{
                Path file=cache.resolve(key+".png");BufferedImage image=null;
                if(Files.exists(file)&&System.currentTimeMillis()-Files.getLastModifiedTime(file).toMillis()<7L*24*60*60*1000)image=ImageIO.read(file.toFile());
                if(image==null){
                    String pattern=System.getProperty("dds.map.tileUrl","https://tile.openstreetmap.org/{tile}.png");
                    HttpURLConnection c=(HttpURLConnection)URI.create(pattern.replace("{tile}",key)).toURL().openConnection();
                    c.setRequestProperty("User-Agent","DavaoDisasterSafety/1.0 (Group2 Java Swing classroom project)");c.setConnectTimeout(5000);c.setReadTimeout(6000);
                    try{
                        if(c.getResponseCode()!=200)throw new IOException("Tile service returned "+c.getResponseCode());
                        byte[] bytes;try(InputStream in=c.getInputStream()){bytes=in.readNBytes(2_000_000);}
                        image=ImageIO.read(new ByteArrayInputStream(bytes));if(image==null)throw new IOException("Invalid map image");
                        Files.createDirectories(file.getParent());Files.write(file,bytes);
                    }finally{c.disconnect();}
                }
                if(!closed)images.put(key,image);
                SwingUtilities.invokeLater(()->{status="Online street map | Drag to pan | Scroll to zoom | Approximate demo markers";repaint();});
            }catch(Exception ex){failures.put(key,System.currentTimeMillis()+60000);SwingUtilities.invokeLater(()->{status="Map tiles unavailable. Check internet; cached tiles and family markers remain visible.";repaint();});}
            finally{pending.remove(key);}
        });}catch(RejectedExecutionException ex){pending.remove(key);}
    }
    @Override public void removeNotify(){closed=true;pool.shutdownNow();super.removeNotify();}
}
