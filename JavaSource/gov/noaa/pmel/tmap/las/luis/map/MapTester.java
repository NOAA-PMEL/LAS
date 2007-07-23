package gov.noaa.pmel.tmap.las.luis.map;

/**
 * <p>Title: LAS Map Tester</p>
 * <p>Description: </p>
 * <p>Copyright: (c) 2003 Joe Sirott</p>
 * @author Joe Sirott
 * @version 1.0
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.io.File;
import java.io.FileInputStream;
import java.awt.geom.Rectangle2D;

public class MapTester {
  JTextField ranges[] = new JTextField[4];
  JTextField iranges[] = new JTextField[4];
  MapGenerator gen;
  double xlo,xhi,ylo,yhi;
  int ixlo,ixhi,iylo,iyhi;
  int zoom = 1;
  double centerx, centery;

  public MapTester() throws Exception {
    JFrame f = new JFrame("MapTester");
    f.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {System.exit(0);}
      });
//    gen = new MapGenerator("java_0_world.gif", 400, 200);
    File fin = new File("java_0_world.gif");
    FileInputStream fstr = new FileInputStream(fin);
    byte[] bytes = new byte[(int)fin.length()];
    fstr.read(bytes);
    gen = new MapGenerator(bytes, 320,160);

    f.getContentPane().add("Center", gen);

    ranges[0] = new JTextField("60");
    ranges[1] = new JTextField("298");
    ranges[2] = new JTextField("-45");
    ranges[3] = new JTextField("43");

    Box mainPanel = Box.createVerticalBox();

    Box panel = Box.createHorizontalBox();
    mainPanel.add(panel);
    JLabel t = new JLabel("xlo");
    panel.add(t);
    panel.add(ranges[0]);
    t = new JLabel("xhi");
    panel.add(t);
    panel.add(ranges[1]);
    t = new JLabel("ylo");
    panel.add(t);
    panel.add(ranges[2]);
    t = new JLabel("yhi");
    panel.add(t);
    panel.add(ranges[3]);

    panel = Box.createHorizontalBox();
    mainPanel.add(panel);
    JButton button = new JButton("setMarker");
    panel.add(button);
    button.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e){
	  updateRanges(true);
	  gen.setMarkerCoordinates(xlo,xhi,ylo,yhi);
          centerx = (xlo + xhi)/2.0; centery = (ylo + yhi)/2.0;
	  gen.repaint();
	}
      });

    button = new JButton("setView");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateRanges();
        gen.setViewWindow(xlo, xhi, ylo, yhi);
        gen.repaint();
      }
    });

    button = new JButton("reset");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setRanges("-180", "180", "-90", "90");
        gen.setViewWindow( -180, 180, -90, 90);
        gen.setMarkerCoordinates( -180, 180, -90, 90);
        gen.repaint();
      }
    });

    panel = Box.createHorizontalBox();
    mainPanel.add(panel);
    button = new JButton("zoom in");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoom += 1;
        gen.setZoom(zoom);
        gen.repaint();
      }
    });

    button = new JButton("zoom out");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (zoom <= 1){
          return;
        }
        zoom -= 1;
        gen.setZoom(zoom);
        gen.repaint();
     }
    });

    panel = Box.createHorizontalBox();
    mainPanel.add(panel);
    button = new JButton("pan up");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setPan(MapGenerator.UP);
        gen.repaint();
      }
    });

    button = new JButton("pan down");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setPan(MapGenerator.DOWN);
        gen.repaint();
      }
    });

    button = new JButton("pan left");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setPan(MapGenerator.LEFT);
        gen.repaint();
      }
    });

    button = new JButton("pan right");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setPan(MapGenerator.RIGHT);
        gen.repaint();
      }
    });

    panel = Box.createHorizontalBox();
    mainPanel.add(panel);
    button = new JButton("cross marker");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setMarkerMode(MapGenerator.MODE_CROSS);
        gen.repaint();
      }
    });

    button = new JButton("pt marker");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setMarkerMode(MapGenerator.MODE_PT);
        gen.repaint();
      }
    });

    button = new JButton("x line marker");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setMarkerMode(MapGenerator.MODE_X);
        gen.repaint();
     }
    });


    button = new JButton("y line marker");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setMarkerMode(MapGenerator.MODE_Y);
        gen.repaint();
      }
    });

    button = new JButton("rect marker");
    panel.add(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gen.setMarkerMode(MapGenerator.MODE_XY);
        gen.repaint();
      }
    });

    panel = Box.createHorizontalBox();
    mainPanel.add(panel);
    button = new JButton("set restricted");
    panel.add(button);
    button.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       updateRanges();
       gen.setRestrictedCoordinates(xlo,xhi,ylo,yhi);
       gen.repaint();
     }
   });

   button = new JButton("clear restricted");
   panel.add(button);
   button.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       gen.clearRestrictedCoordinates();
       gen.repaint();
     }
   });

   iranges[0] = new JTextField("241");
   iranges[1] = new JTextField("42");
   iranges[2] = new JTextField("53");
   iranges[3] = new JTextField("90");

   panel = Box.createHorizontalBox();
   mainPanel.add(panel);
   t = new JLabel("ixlo");
   panel.add(t);
   panel.add(iranges[0]);
   t = new JLabel("ixhi");
   panel.add(t);
   panel.add(iranges[1]);
   t = new JLabel("iylo");
   panel.add(t);
   panel.add(iranges[2]);
   t = new JLabel("iyhi");
   panel.add(t);
   panel.add(iranges[3]);

   button = new JButton("set marker(image)");
   panel.add(button);
   button.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       updateIranges();
       gen.setMarkerCoordinatesFromImage(ixlo,ixhi,iylo,iyhi);
       gen.repaint();
     }
   });

   button = new JButton("get current int image)");
   panel.add(button);
   button.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       setIranges();
       gen.repaint();
     }
   });

   button = new JButton("setLastMarker");
   panel.add(button);
   button.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       updateIranges();
       gen.setLastMarkerCoordinateFromImage(ixhi,iyhi);
       gen.repaint();
     }
   });



   f.getContentPane().add("South", mainPanel);
   f.pack();
   f.show();
   updateRanges();
   centerx = (xlo + xhi)/2.0; centery = (ylo + yhi)/2.0;
 }

  void setRanges(String r0, String r1, String r2, String r3){
    ranges[0].setText(r0);
    ranges[1].setText(r1);
    ranges[2].setText(r2);
    ranges[3].setText(r3);
  }

  void updateRanges() {
    updateRanges(false);
  }

  void updateRanges(boolean allowZeroWidth) {
    String sxlo = ranges[0].getText();
    String sxhi = ranges[1].getText();
    String sylo = ranges[2].getText();
    String syhi = ranges[3].getText();
    double nxlo,nxhi,nylo,nyhi;
    try {
      nxlo = Double.parseDouble(sxlo);
      nxhi = Double.parseDouble(sxhi);
      nylo = Double.parseDouble(sylo);
      nyhi = Double.parseDouble(syhi);
    } catch (NumberFormatException e){
      e.printStackTrace();
      return;
    }
    try {
      MapGenerator.checkCoordinates(nxlo,nxhi,nylo,nyhi, allowZeroWidth);
    } catch (MapGeneratorException e){
      System.out.println("Invalid map coordinates");
      return;
    }
    xlo = nxlo; xhi = nxhi; ylo = nylo; yhi = nyhi;
  }

  void updateIranges() {
    String sxlo = iranges[0].getText();
    String sxhi = iranges[1].getText();
    String sylo = iranges[2].getText();
    String syhi = iranges[3].getText();
    int nxlo,nxhi,nylo,nyhi;
    try {
      nxlo = Integer.parseInt(sxlo);
      nxhi = Integer.parseInt(sxhi);
      nylo = Integer.parseInt(sylo);
      nyhi = Integer.parseInt(syhi);
    } catch (NumberFormatException e){
      e.printStackTrace();
      return;
    }
    try {
      gen.checkIntCoordinates(nxlo,nxhi,nylo,nyhi);
    } catch (MapGeneratorException e){
      System.out.println("Invalid map coordinates");
      return;
    }
    ixlo = nxlo; ixhi = nxhi; iylo = nylo; iyhi = nyhi;
  }

  void setIranges() {
    Rectangle2D r = gen.getMarkerCoordinatesFromImage();
    iranges[0].setText(Integer.toString((int)r.getX()));
    iranges[1].setText(Integer.toString((int)(r.getX() + r.getWidth())));
    iranges[2].setText(Integer.toString((int)r.getY()));
    iranges[3].setText(Integer.toString((int)(r.getY() + r.getHeight())));
  }

  public static void main(String[] args){
    try {
      new MapTester();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }


}
