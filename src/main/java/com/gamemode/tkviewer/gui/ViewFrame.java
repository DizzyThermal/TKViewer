package com.gamemode.tkviewer.gui;

import com.gamemode.tkviewer.render.*;
import com.gamemode.tkviewer.render.Renderer;
import com.gamemode.tkviewer.resources.EffectImage;
import com.gamemode.tkviewer.resources.Part;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

//import javax.swing.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ViewFrame extends JFrame implements ActionListener {

    Renderer renderer;
    String singular;
    String plural;

    Image clientIcon;
    JPanel imagePanel;
    JList list;

    JRadioButton framesButton;
    JRadioButton animationsButton;

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

    public ViewFrame(String title, String singular, String plural, EffectRenderer effectRenderer) {
        this(title, singular, plural);
        this.renderer = effectRenderer;
        this.configure(false);
    }

    public ViewFrame(String title, String singular, String plural, MobRenderer mobRenderer) {
        this(title, singular, plural);
        this.renderer = mobRenderer;
        this.configure(false);
    }

    public ViewFrame(String title, String singular, String plural, PartRenderer partRenderer) {
        this(title, singular, plural);
        this.renderer = partRenderer;
        this.configure(false);
    }

    public ViewFrame(String title, String singular, String plural, TileRenderer tileRenderer) {
        this(title, singular, plural, tileRenderer, false);
    }

    public ViewFrame(String title, String singular, String plural, TileRenderer tileRenderer, boolean useEpfCount) {
        this(title, singular, plural);
        this.renderer = tileRenderer;
        this.configure(useEpfCount);
    }

    public void configure(boolean useEpfCount) {
        this.setLayout(new BorderLayout());
        imagePanel = new JPanel();
        imagePanel.setBackground(Color.GRAY);
        imagePanel.setPreferredSize(new Dimension(600, 520));

        int count = this.renderer.getCount(useEpfCount);
        String[] items = new String[count];
        for (int i = 0; i < count; i++) {
            items[i] = this.singular + " " + i;
        }
        list = new JList(items);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int idx = list.getSelectedIndex();
                    if (renderer instanceof EffectRenderer) {
                        if (framesButton.isSelected()) {
                            renderFrames(idx);
                        } else {
                            renderEffectAnimations(idx);
                        }
                    } else if (renderer instanceof PartRenderer) {
                        if (framesButton.isSelected()) {
                            renderFrames(idx);
                        } else {
                            renderPartAnimations(idx);
                        }
                    } else {
                        renderFrames(idx);
                    }
                }
            }
        });

        JScrollPane scroller = new JScrollPane(list);
        scroller.setPreferredSize(new Dimension(150, 520));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setBorder(new LineBorder(Color.BLACK));
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 36));

        framesButton = new JRadioButton("Frames");
        animationsButton = new JRadioButton("Animations");
        animationsButton.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(framesButton);
        group.add(animationsButton);

        framesButton.addActionListener(this);
        animationsButton.addActionListener(this);

        statusPanel.add(framesButton);
        statusPanel.add(animationsButton);

        this.add(scroller, BorderLayout.WEST);
        this.add(imagePanel, BorderLayout.CENTER);
        this.add(statusPanel, BorderLayout.SOUTH);

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

    private void clearImagePanel() {
        imagePanel.removeAll();
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    public void renderEffectAnimations(int index) {
        clearImagePanel();

        // Get Effect Images
        List<EffectImage> images = ((EffectRenderer) renderer).renderEffect(index);

        // Create GIF in Temp Directory
        if (!new File(Resources.EFFECT_ANIMATION_DIRECTORY).exists()) {
            new File(Resources.EFFECT_ANIMATION_DIRECTORY).mkdirs();
        }
        String gifPath = (Resources.EFFECT_ANIMATION_DIRECTORY + File.separator + "effect-" + index + ".gif");
        if (!new File(gifPath).exists()) {
            FileUtils.exportGifFromImages(images, gifPath);
        }

        // Add GIF to imagePanel
        if (new File(gifPath).exists()) {
            Icon gifIcon = new ImageIcon(gifPath);
            JLabel jLabel = new JLabel(gifIcon);
            imagePanel.add(jLabel);
        } else {
            System.err.println("Couldn't find file: " + gifPath);
        }

        revalidate();
    }


    public void renderPartAnimations(int index) {
        clearImagePanel();

        // Create Part Animations in Temp Directory
        File outputDirectory = new File(Resources.PART_ANIMATION_DIRECTORY + File.separator + plural);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        PartRenderer partRenderer = ((PartRenderer) renderer);

        List<String> gifPaths = new ArrayList<String>();
        Part part = partRenderer.partDsc.parts.get(index);
        for (int i = 0; i < part.getChunks().size(); i++) {
            List<EffectImage> chunkImages = partRenderer.renderAnimation(index, i);
            if (chunkImages.size() != 0) {
                String gifPath = outputDirectory + File.separator + singular + "-" + index + "-" + i + "-.gif";
                FileUtils.exportGifFromImages(chunkImages, gifPath);
                gifPaths.add(gifPath);
            }
        }

        for (int i = 0; i < gifPaths.size(); i++) {
            // Add GIF to imagePanel
            String gifPath = gifPaths.get(i);
            if (new File(gifPath).exists()) {
                Icon gifIcon = new ImageIcon(gifPath);
                JLabel jLabel = new JLabel(gifIcon);
                imagePanel.add(jLabel);
            } else {
                System.err.println("Couldn't find file: " + gifPath);
            }
        }

        revalidate();
    }


    public void renderFrames(int index) {
        clearImagePanel();

        Image[] images = renderer.getFrames(index);
        for (int i = 0; i < images.length; i++) {
            final int frameIndex = renderer.getFrameIndex(index, i);
            JLabel jLabel = new JLabel(new ImageIcon(images[i]));
            jLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JDialog loadingNotification = new JDialog(ViewFrame.this, new StringBuilder("TKViewer" + " - " + singular + "[" + index + ":" + frameIndex + "]").toString(), false);
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

    @Override
    public void actionPerformed(ActionEvent ae) {
        int listIndex = list.getSelectedIndex();

        if (listIndex == -1) {
            // Don't render anything until a list selection is made
            return;
        }

        if (ae.getSource() == this.framesButton) {
            this.renderFrames(listIndex);
        } else if (ae.getSource() == this.animationsButton) {
            if (renderer instanceof EffectRenderer) {
                this.renderEffectAnimations(listIndex);
            } else if (renderer instanceof PartRenderer) {
                this.renderPartAnimations(listIndex);
            }
        }
    }
}
