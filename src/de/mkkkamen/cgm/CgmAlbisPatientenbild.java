package de.mkkkamen.cgm;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class CgmAlbisPatientenbild extends JFrame implements ActionListener, WebcamPanel.Painter {
  @SuppressWarnings("FieldCanBeLocal") private final String gdtPath = "C:\\CGM\\ALBIS\\picimport\\picimport.gdt";
  @SuppressWarnings("FieldCanBeLocal") private final String targetPath = "N:\\ALBIS\\Db\\Bild\\";
  private final Dimension dimension = WebcamResolution.SVGA.getSize();
  private final Webcam webcam = Webcam.getDefault();
  private final WebcamPanel panel = new WebcamPanel(webcam, true);
  private final WebcamPanel.Painter painter;
  @SuppressWarnings("WeakerAccess") JTextField jTextField;

  @SuppressWarnings("WeakerAccess") int imgWidth = 285;
  @SuppressWarnings("WeakerAccess ")int imgHeight = 384;

  public CgmAlbisPatientenbild() {
    super();

    setTitle("CGM ALBIS Patientenbild");
    setLayout(new FlowLayout(FlowLayout.CENTER));
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setPreferredSize(new Dimension(dimension.width, dimension.height+80));
    setResizable(false);
    setAlwaysOnTop(true);
    setBackground(Color.LIGHT_GRAY);

    webcam.setCustomViewSizes(dimension);
    webcam.setViewSize(dimension);

    panel.setPreferredSize(dimension);
    panel.setOpaque(true);
    panel.setBackground(Color.LIGHT_GRAY);
    panel.setPainter(this);

    painter = panel.getDefaultPainter();

    String sPatientNo = "";
    StringBuilder sPatientName = new StringBuilder();
    JLabel jTextLabel;
    JLabel jLabel = new JLabel("Patientennummer eingeben:");
    jTextField = new JTextField("", 15);

    final JButton jButtonSaveExit = new JButton();
    jButtonSaveExit.addActionListener(this);
    jButtonSaveExit.setFocusable(true);
    jButtonSaveExit.setText("Abspeichern & Beenden");

    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(gdtPath));
      String line  = bufferedReader.readLine();
      while( line != null ) {
        String sLine = line.substring(3);
        if( sLine.startsWith("3000") ) {
          sPatientNo = sLine.replaceFirst("3000", "");
        }
        if( sLine.startsWith("3101") ) {
          sPatientName.append(sLine.replaceFirst("3101", "")).append(", ");
        }
        if( sLine.startsWith("3102") ) {
          sPatientName.append(sLine.replaceFirst("3102", "")).append(" * ");
        }
        if( sLine.startsWith("3103") ) {
          sPatientName.append(sLine.replaceFirst("3103", ""));
        }
        line = bufferedReader.readLine();
      }
      bufferedReader.close();
      File gdtFile = new File(gdtPath);
      boolean bSuccess = gdtFile.delete();
      if( !bSuccess ) {
        JOptionPane.showMessageDialog(panel, "Die GDT-Datei konnte nicht gelöscht werden.");
      }
    }
    catch( IOException e ) {
      JOptionPane.showMessageDialog(panel, "Der aktuelle Patient kann nicht ausgelesen werden, GDT nicht vorhanden. Bitte Patientennummer manuell eingeben.");
    }

    jTextField.setText(sPatientNo);
    jTextLabel = new JLabel(sPatientName.toString());

    jTextField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
          e.consume();
        }
      }

      @Override
      public void keyPressed(KeyEvent e) {
        if( e.getKeyCode() == KeyEvent.VK_ENTER) {
          jButtonSaveExit.doClick();
        }
      }
    });

    JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panel1.setMinimumSize(dimension);
    panel1.add(jLabel);
    panel1.add(jTextField);

    JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panel2.setMinimumSize(dimension);
    panel2.add(jTextLabel);

    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
    panel3.setMinimumSize(dimension);
    panel3.add(jButtonSaveExit);

    add(panel);
    add(panel1);
    add(panel2);
    add(panel3);

    pack();
    setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        e.printStackTrace();
      }
      new CgmAlbisPatientenbild();
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      BufferedImage subImage = webcam.getImage().getSubimage( ((dimension.width-imgWidth) / 2), ((dimension.height-imgHeight) / 2), imgWidth, imgHeight);
      ImageIO.write(resize(subImage), "JPG", new File(targetPath + jTextField.getText() + ".jpg"));
    }
    catch (IOException exception) {
      JOptionPane.showMessageDialog(panel, "Das Bild konnte nicht abgelegt werden. Ist der Pfad vorhanden?");
    }

    webcam.close();
    System.exit(0);
  }

  @Override
  public void paintPanel(WebcamPanel panel, Graphics2D g2) {
    if (painter != null) {
      painter.paintPanel(panel, g2);
    }
  }

  @Override
  public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {
    if (painter != null) {
      painter.paintImage(panel, image, g2);
    }

    g2.setStroke(new BasicStroke(3.0f));
    g2.setPaint(Color.RED);
    g2.drawRect(((dimension.width-imgWidth) / 2), ((dimension.height-imgHeight) / 2), imgWidth, imgHeight);
  }

  private static BufferedImage resize(BufferedImage img) {
    Image tmp = img.getScaledInstance(190, 256, Image.SCALE_SMOOTH);
    BufferedImage image = new BufferedImage(190, 256, BufferedImage.TYPE_INT_RGB);

    Graphics2D g2d = image.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();

    return image;
  }
}