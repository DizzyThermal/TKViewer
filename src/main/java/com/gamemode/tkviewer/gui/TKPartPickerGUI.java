package com.gamemode.tkviewer.gui;

import com.gamemode.tkpartpicker.resources.PartInfo;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.EffectImage;
import com.gamemode.tkviewer.resources.Part;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;
import com.gamemode.tkviewer.utilities.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TKPartPickerGUI extends JFrame implements ActionListener {

    Image clientIcon;

    JMenuBar menuBar;

    JMenu fileMenu = new JMenu("File");
    JMenuItem exitMenuItem = new JMenuItem("Exit");

    JPanel contentPanel;

    JPanel viewerPanel;
    JComboBox partPicker;
    ImageIcon viewerIcon;

    JScrollPane partScroller;
    JPanel partPanel;

    Integer tickValue = 0;

    LinkedHashMap<String, PartInfo> characterPartInfo;

    int partValue = 0;

    public TKPartPickerGUI(String title) {
        super(title);
        this.setPreferredSize(new Dimension(640, 480));
        this.clientIcon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("client_icon.png"));
        this.setIconImage(this.clientIcon);

        characterPartInfo = new LinkedHashMap<String, PartInfo>();
        characterPartInfo.put("Bodies", new PartInfo(3, 2, 6, true, RenderUtils.createBodyRenderer()));
        characterPartInfo.put("Coats", new PartInfo(0, 0, 6,false, RenderUtils.createCoatRenderer()));
        characterPartInfo.put("Shoes", new PartInfo(0, 0, 6,false, RenderUtils.createShoeRenderer()));
        characterPartInfo.put("Mantles", new PartInfo(0, 0,6,false, RenderUtils.createMantleRenderer()));

        characterPartInfo.put("Faces", new PartInfo(0, 2,6,true, RenderUtils.createFaceRenderer()));
        characterPartInfo.put("Face Decorations", new PartInfo(3,6, 0,false, RenderUtils.createFaceDecRenderer()));
        characterPartInfo.put("Hair", new PartInfo(0, 2,6,true, RenderUtils.createHairRenderer()));
        characterPartInfo.put("Helmets", new PartInfo(0, 2,6,false, RenderUtils.createHelmetRenderer()));

        characterPartInfo.put("Spears", new PartInfo(0, 1,6,false, RenderUtils.createSpearRenderer()));
        characterPartInfo.put("Shields", new PartInfo(0, 2,6,false, RenderUtils.createShieldRenderer()));
        characterPartInfo.put("Swords", new PartInfo(0, 2,6,false, RenderUtils.createSwordRenderer()));
        characterPartInfo.put("Bows", new PartInfo(0, 2,3,false, RenderUtils.createBowRenderer()));
        characterPartInfo.put("Fans", new PartInfo(0, 2,3,false, RenderUtils.createFanRenderer()));

        initMenu();
        initPanel();

        SwingWorker loadingWorker = new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                while (true) {
                    Thread.sleep(250);
                    tickValue++;

                    viewerPanel.remove(0);
                    viewerPanel.add(new JLabel(new ImageIcon(renderCharacter())), 0);

                    viewerPanel.revalidate();
                    viewerPanel.repaint();
                }
            }

            @Override
            protected void done() {
                System.exit(0);
            }
        };
        loadingWorker.execute();
    }

    public void initMenu() {
        // Add Menu
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        // File > Exit
        exitMenuItem.addActionListener(this);

        // File
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
    }

    public void initPanel() {
        // Create content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Add Viewer Panel
        viewerPanel = new JPanel(new FlowLayout());

        //   Add Part List to ComboBox
        String[] partPickerItems = new String[characterPartInfo.size()];
        int partPickerIndex = 0;
        for (Map.Entry<String, PartInfo> characterPartInfo : this.characterPartInfo.entrySet()) {
            partPickerItems[partPickerIndex++] = characterPartInfo.getKey();
        }
        List<String> partPickerItemsList = new ArrayList<String>(Arrays.asList(partPickerItems));
        Collections.sort(partPickerItemsList);
        partPicker = new JComboBox(partPickerItemsList.toArray());
        partPicker.addActionListener(this);
        //   Add Character
        viewerIcon = new ImageIcon(renderCharacter());

        viewerPanel.add(new JLabel(viewerIcon));
        viewerPanel.add(partPicker);

        // Add Part Panel
        partPanel = new JPanel(new FlowLayout());
        partPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
        partPanel.setPreferredSize(new Dimension(800, 600));

        partScroller = new JScrollPane(partPanel);
        updatePartPanel("Bodies", 0);

        // Add content to the panel
        contentPanel.add(viewerPanel);
        contentPanel.add(partScroller);

        // Add the content panel to the parent JFrame (this)
        this.add(contentPanel);
    }

    public void updatePartPanel(String partKey, Integer partNumber) {
        partPanel.removeAll();

        PartInfo partInfo = characterPartInfo.get(partKey);
        PartRenderer partRenderer = this.characterPartInfo.get(partKey).getPartRenderer();
        for (int i = 0; i < partRenderer.partDsc.partCount; i++) {
            Part part = partRenderer.partDsc.parts.get(i);
            BufferedImage partImage = partRenderer.renderPart(i, (int)part.getFrameIndex(), partInfo.getIconFrameIndex(), (int) part.getPaletteId());

            JLabel jLabel = new JLabel(new ImageIcon(partImage));
            final int partIndex = i;
            jLabel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int currentPartIndex = partInfo.getPartIndex();
                    boolean shouldRender = partInfo.getShouldRender();

                    if (currentPartIndex == partIndex) {
                        partInfo.setShouldRender(!shouldRender);
                    } else {
                        partInfo.setShouldRender(true);
                        partInfo.setPartIndex(partIndex);
                    }

                    syncParts(partKey);
                }

                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
            });

            partPanel.add(jLabel);
        }

        partPanel.revalidate();
        partPanel.repaint();
    }

    public void syncParts(String partKey) {
        PartInfo partInfo = this.characterPartInfo.get(partKey);
        if (partKey.equals("Helmets")) {
            this.characterPartInfo.get("Hair").setShouldRender(false);
        } else if (partKey.equals("Hair")) {
            this.characterPartInfo.get("Helmets").setShouldRender(false);
        } else if (partKey.equals("Bodies")) {
            this.characterPartInfo.get("Coats").setShouldRender(false);
        } else if (partKey.equals("Coats")) {
            this.characterPartInfo.get("Bodies").setShouldRender(false);
        } else if (partKey.equals("Bows")) {
            this.characterPartInfo.get("Fans").setShouldRender(false);
            this.characterPartInfo.get("Shields").setShouldRender(false);
            this.characterPartInfo.get("Spears").setShouldRender(false);
            this.characterPartInfo.get("Swords").setShouldRender(false);
        } else if (partKey.equals("Fans")) {
            if (partInfo.getShouldRender()) {
                this.characterPartInfo.get("Bodies").setAnimationIndex(6);
                this.characterPartInfo.get("Coats").setAnimationIndex(6);
            } else {
                this.characterPartInfo.get("Bodies").setAnimationIndex(2);
                this.characterPartInfo.get("Coats").setAnimationIndex(2);
            }
            this.characterPartInfo.get("Bows").setShouldRender(false);
            this.characterPartInfo.get("Spears").setShouldRender(false);
            this.characterPartInfo.get("Swords").setShouldRender(false);
        } else if (partKey.equals("Shields")) {
            this.characterPartInfo.get("Bows").setShouldRender(false);
            this.characterPartInfo.get("Spears").setShouldRender(false);
        } else if (partKey.equals("Spears")) {
            if (partInfo.getShouldRender()) {
                this.characterPartInfo.get("Bodies").setAnimationIndex(6);
                this.characterPartInfo.get("Coats").setAnimationIndex(6);
            } else {
                this.characterPartInfo.get("Bodies").setAnimationIndex(2);
                this.characterPartInfo.get("Coats").setAnimationIndex(2);
            }
            this.characterPartInfo.get("Bows").setShouldRender(false);
            this.characterPartInfo.get("Fans").setShouldRender(false);
            this.characterPartInfo.get("Shields").setShouldRender(false);
            this.characterPartInfo.get("Spears").setShouldRender(false);
            this.characterPartInfo.get("Swords").setShouldRender(false);
        } else if (partKey.equals("Swords")) {
            if (partInfo.getShouldRender()) {
                this.characterPartInfo.get("Bodies").setAnimationIndex(6);
                this.characterPartInfo.get("Coats").setAnimationIndex(6);
            } else {
                this.characterPartInfo.get("Bodies").setAnimationIndex(2);
                this.characterPartInfo.get("Coats").setAnimationIndex(2);
            }
            this.characterPartInfo.get("Bows").setShouldRender(false);
            this.characterPartInfo.get("Fans").setShouldRender(false);
            this.characterPartInfo.get("Spears").setShouldRender(false);
        }
    }

    public BufferedImage createGrassBackground() {
        int grassWidth = 4;

        BufferedImage grassBackground = new BufferedImage(Resources.TILE_DIM * grassWidth, Resources.TILE_DIM * grassWidth, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphicsObject = grassBackground.createGraphics();

        TileRenderer tileRenderer = new TileRenderer();
        BufferedImage tile = tileRenderer.renderTile(21); // Grass Patch

        for (int i = 0; i < grassWidth; i++) {
            for (int j = 0; j < grassWidth; j++) {
                graphicsObject.drawImage(tile, null, (i * Resources.TILE_DIM), (j * Resources.TILE_DIM));
            }
        }

        return grassBackground;
    }

    public BufferedImage renderCharacter() {
        // 192 x 192
        BufferedImage characterImage = createGrassBackground();
        Graphics2D graphicsObject = characterImage.createGraphics();

        List<List<EffectImage>> effImages = new ArrayList<List<EffectImage>>();
        for (Map.Entry<String, PartInfo> characterPartInfo : this.characterPartInfo.entrySet()) {
            String partKey = characterPartInfo.getKey();
            PartInfo partInfo = characterPartInfo.getValue();

            if (partInfo.getShouldRender()) {

                int partIndex = partInfo.getPartIndex();
                int animationIndex = partInfo.getAnimationIndex();

                List<EffectImage> effectImages = partInfo.getPartRenderer().renderAnimation(partIndex, animationIndex);

                effImages.add(effectImages);
            }
        }

        List<EffectImage> effectImages = RenderUtils.aggregateAnimations(effImages);

        BufferedImage drawing = effectImages.get(tickValue % effectImages.size()).getImage();
        int backgroundWidth = characterImage.getWidth();
        int backgroundHeight = characterImage.getHeight();
        int width = drawing.getWidth();
        int height = drawing.getHeight();
        graphicsObject.drawImage(drawing, null, (backgroundWidth / 2) - (width / 2), (backgroundHeight / 2) - (height / 2));

        return characterImage;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == this.partPicker) {
            String partKey = this.partPicker.getSelectedItem().toString();
            PartInfo partInfo = this.characterPartInfo.get(partKey);
            this.updatePartPanel(partKey, partInfo.getIconFrameIndex());
        } else if (ae.getSource() == this.exitMenuItem) {
            this.dispose();
            System.out.println();
        }
    }
}