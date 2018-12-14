package com.gamemode.tkviewer.gui;

import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.render.Renderer;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

//import javax.swing.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ViewFrame extends JFrame {

    Renderer renderer;
    String singular;
    String plural;

    Image clientIcon;

    public ViewFrame(String title, String singular, String plural) {
        // Configure Frame
        this.setTitle(title);
        this.singular = singular;
        this.plural = plural;
        this.setLayout(new FlowLayout());
        this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        this.clientIcon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource(Resources.CLIENT_ICON));
        this.setIconImage(this.clientIcon);
        this.setSize(800, 600);
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public ViewFrame(String title, String singular, String plural, PartRenderer partRenderer) {
        this(title, singular, plural);
        this.renderer = partRenderer;
        this.configure();
    }

    public ViewFrame(String title, String singular, String plural, TileRenderer tileRenderer) {
        this(title, singular, plural);
        this.renderer = tileRenderer;
        this.configure();
    }

    public void configure() {
        JPanel imagePanel = new JPanel();
        imagePanel.setBackground(Color.GRAY);
        imagePanel.setPreferredSize(new Dimension(600, 520));

        int count = this.renderer.getCount();
        String[] items = new String[count];
        for (int i = 0; i < count; i++) {
            items[i] = this.singular + " " + i;
        }
        JList list = new JList(items);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    imagePanel.removeAll();
                    imagePanel.revalidate();
                    imagePanel.repaint();

                    int idx = list.getSelectedIndex();
                    Image[] images = renderer.getFrames(idx);
                    for (int i = 0; i < images.length; i++) {
                        final int frameIndex = renderer.getFrameIndex(idx, i);
                        JLabel jLabel = new JLabel(new ImageIcon(images[i]));
                        jLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                super.mouseClicked(e);
                                if (SwingUtilities.isRightMouseButton(e)) {
                                    JDialog loadingNotification = new JDialog(ViewFrame.this, new StringBuilder("TKViewer" + " - " + singular + "[" + idx + ":" + frameIndex + "]").toString(), false);
                                    loadingNotification.setIconImage(clientIcon);
                                    JTextPane info = new JTextPane();
                                    info.setContentType("text/html");
                                    info.setText(renderer.getInfo(frameIndex));
                                    info.setEditable(false);
                                    info.setFont(new Font("Consolas", Font.BOLD, 12));
                                    loadingNotification.add(info);
                                    loadingNotification.setSize(new Dimension(480, 320));
                                    loadingNotification.setResizable(false);
                                    loadingNotification.setLocationRelativeTo(ViewFrame.this);
                                    loadingNotification.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    loadingNotification.setVisible(true);
                                }
                            }
                        });
                        imagePanel.add(jLabel);
                    }

                    revalidate();
                }
            }
        });

        JScrollPane scroller = new JScrollPane(list);
        scroller.setPreferredSize(new Dimension(150, 520));

        this.add(scroller);
        this.add(imagePanel);

        this.setVisible(true);

        // Add Menu
        JMenuBar imageMenuBar = new JMenuBar();
        this.setJMenuBar(imageMenuBar);

        // File > Close
        JMenuItem closeMenuItem = new JMenuItem("Close");
        closeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // File
        JMenu imageFileMenu = new JMenu("File");
        imageFileMenu.add(closeMenuItem);
        imageMenuBar.add(imageFileMenu);
    }
}
