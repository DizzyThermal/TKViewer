package com.gamemode.tkviewer.gui;

import com.gamemode.tkpartpicker.resources.PartInfo;
import com.gamemode.tkviewer.file_handlers.CmpFileHandler;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.resources.EffectImage;
import com.gamemode.tkviewer.resources.Part;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.RenderUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TKPartPickerGUI extends JFrame implements ActionListener {

    // GUI Icon
    Image clientIcon;

    // Menu Bar
    JMenuBar menuBar;

    JMenu fileMenu = new JMenu("File");
    JMenuItem exitMenuItem = new JMenuItem("Exit");

    // Content
    JPanel contentPanel;

    // Character Viewer Panel
    JPanel viewerPanel;

    // Options Panel
    JPanel optionsPanel;
    JComboBox partPicker;
    JComboBox palettePicker;
    ImageIcon viewerIcon;
    JButton changeMapButton = new JButton("Change Map");
    int mapId = 4549;

    JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(11, 0, 20, 1));
    JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(12, 0, 20, 1));

    // Part Picker Scroller
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
        characterPartInfo.put("Bodies", new PartInfo(3, 2, 6, -1, true, RenderUtils.createBodyRenderer()));
        characterPartInfo.put("Coats", new PartInfo(0, 2, 6,-1,false, RenderUtils.createCoatRenderer()));
        characterPartInfo.put("Shoes", new PartInfo(0, 0, 6,-1,false, RenderUtils.createShoeRenderer()));
        characterPartInfo.put("Mantles", new PartInfo(0, 0,6,-1,false, RenderUtils.createMantleRenderer()));

        characterPartInfo.put("Faces", new PartInfo(0, 2,6,-1,true, RenderUtils.createFaceRenderer()));
        characterPartInfo.put("Face Decorations", new PartInfo(0,2, 6,-1,false, RenderUtils.createFaceDecRenderer()));
        characterPartInfo.put("Hair", new PartInfo(0, 2,6,-1,true, RenderUtils.createHairRenderer()));
        characterPartInfo.put("Helmets", new PartInfo(0, 2,6,-1,false, RenderUtils.createHelmetRenderer()));

        characterPartInfo.put("Spears", new PartInfo(0, 1,6,-1,false, RenderUtils.createSpearRenderer()));
        characterPartInfo.put("Shields", new PartInfo(0, 2,9,-1,false, RenderUtils.createShieldRenderer()));
        characterPartInfo.put("Swords", new PartInfo(0, 2,6,-1,false, RenderUtils.createSwordRenderer()));
        characterPartInfo.put("Bows", new PartInfo(0, 2,3,-1,false, RenderUtils.createBowRenderer()));
        characterPartInfo.put("Fans", new PartInfo(0, 2,3,-1,false, RenderUtils.createFanRenderer()));

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
        JLabel partPickerLabel = new JLabel("Part Picker:");
        partPickerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        partPicker = new JComboBox(partPickerItemsList.toArray());
        partPicker.addActionListener(this);

        //   Add Palette List to ComboBox
        String[] palettePickerItems = new String[257];
        palettePickerItems[0] = "-- Default";
        for (int i = 1; i < palettePickerItems.length; i++) {
            palettePickerItems[i] = (i - 1) + "";
        }
        JLabel palettePickerLabel = new JLabel("Palette:");
        palettePickerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        palettePicker = new JComboBox(palettePickerItems);
        palettePicker.addActionListener(this);

        //   Add Part/Palette Comboboxes to Options Panel
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(partPickerLabel);
        optionsPanel.add(partPicker);
        optionsPanel.add(palettePickerLabel);
        optionsPanel.add(palettePicker);

        JPanel coordinatePanel = new JPanel(new FlowLayout());
        JLabel xSpinnerLabel = new JLabel("X:");
        coordinatePanel.add(xSpinnerLabel);
        coordinatePanel.add(xSpinner);
        JLabel ySpinnerLabel = new JLabel("  Y:");
        coordinatePanel.add(ySpinnerLabel);
        coordinatePanel.add(ySpinner);
        optionsPanel.add(coordinatePanel);

        changeMapButton.addActionListener(this);
        optionsPanel.add(changeMapButton);

        //   Add Character
        viewerIcon = new ImageIcon(renderCharacter());

        viewerPanel.add(new JLabel(viewerIcon));
        viewerPanel.add(optionsPanel);

        // Add Part Panel
        partPanel = new JPanel(new GridLayout(0, 6));
        partPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
        partScroller = new JScrollPane(partPanel);
        updatePartPanel("Bodies", 0, -1);


        // Add content to the panel
        contentPanel.add(viewerPanel);
        contentPanel.add(partScroller);

        // Add the content panel to the parent JFrame (this)
        this.add(contentPanel);
    }

    public void updatePartPanel(String partKey, Integer partNumber, int paletteIndex) {
        partPanel.removeAll();

        PartInfo partInfo = characterPartInfo.get(partKey);
        PartRenderer partRenderer = this.characterPartInfo.get(partKey).getPartRenderer();

        for (int i = 0; i < partRenderer.partDsc.partCount; i++) {
            Part part = partRenderer.partDsc.parts.get(i);
            int paletteId;
            if (paletteIndex < 0) {
                paletteId = (int)part.getPaletteId();
            } else {
                paletteId = paletteIndex;
            }
            BufferedImage partImage = partRenderer.renderPart(i, (int)part.getFrameIndex(), partInfo.getIconFrameIndex(), paletteId);

            JLabel jLabel = new JLabel(new ImageIcon(partImage));
            final int partIndex = i;
            jLabel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int currentPartIndex = partInfo.getPartIndex();
                    boolean shouldRender = partInfo.getShouldRender();

                    if (currentPartIndex == partIndex) {
                        if (!partKey.equals("Bodies") && !partKey.equals("Faces")) {
                            partInfo.setShouldRender(!shouldRender);
                        }
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
        partScroller.revalidate();
        partScroller.repaint();
    }

    public void clearWeapon(boolean allWeapons) {
        if (allWeapons) {
            this.characterPartInfo.get("Fans").setShouldRender(false);
            this.characterPartInfo.get("Swords").setShouldRender(false);
            extendHand(false);
        }

        this.characterPartInfo.get("Bows").setShouldRender(false);
        this.characterPartInfo.get("Spears").setShouldRender(false);
    }

    public void clearShield() {
        this.characterPartInfo.get("Shields").setShouldRender(false);
    }

    public void extendHand(boolean extendHand) {
        if (extendHand) {
            this.characterPartInfo.get("Bodies").setAnimationIndex(6);
            this.characterPartInfo.get("Coats").setAnimationIndex(6);
        } else {
            this.characterPartInfo.get("Bodies").setAnimationIndex(2);
            this.characterPartInfo.get("Coats").setAnimationIndex(2);
        }
    }

    public void syncParts(String partKey) {
        PartInfo partInfo = this.characterPartInfo.get(partKey);

        if (partKey.equals("Helmets")) {
            // Toggle Hair with Helmet
            this.characterPartInfo.get("Hair").setShouldRender(!partInfo.getShouldRender());
        } else if (partKey.equals("Bodies")) {
            this.characterPartInfo.get("Coats").setShouldRender(false);
        } else if (partKey.equals("Coats")) {
            this.characterPartInfo.get("Bodies").setShouldRender(!partInfo.getShouldRender());
        } else if (partKey.equals("Bows")) {
            if (partInfo.getShouldRender()) {
                clearWeapon(true);
                extendHand(true);
                partInfo.setShouldRender(true);
            }
        } else if (partKey.equals("Fans")) {
            if (partInfo.getShouldRender()) {
                clearWeapon(true);
                extendHand(true);
                partInfo.setShouldRender(true);
            } else {
                extendHand(false);
            }
        } else if (partKey.equals("Spears")) {
            if (partInfo.getShouldRender()) {
                clearWeapon(true);
                clearShield();
                extendHand(true);
                partInfo.setShouldRender(true);
            } else {
                extendHand(false);
            }
        } else if (partKey.equals("Swords")) {
            if (partInfo.getShouldRender()) {
                clearWeapon(true);
                extendHand(true);
                partInfo.setShouldRender(true);
            } else {
                extendHand(false);
            }
        }
    }

    public BufferedImage createBackground() {
        // Rhino LR - 18020
        return createBackground(1, 4, 4, 5);
    }

    public BufferedImage createBackground(int mapId, int x, int y, int width) {
        BufferedImage background;

        background = RenderUtils.createMapRenderer().renderCropped(mapId, x, y, width, width);

        return background;
    }

    public BufferedImage renderCharacter() {
        BufferedImage characterImage = createBackground(mapId, (int)xSpinner.getValue(), (int)ySpinner.getValue() , 5);
        Graphics2D graphicsObject = characterImage.createGraphics();

        List<List<EffectImage>> effImages = new ArrayList<List<EffectImage>>();
        for (Map.Entry<String, PartInfo> characterPartInfo : this.characterPartInfo.entrySet()) {
            String partKey = characterPartInfo.getKey();
            PartInfo partInfo = characterPartInfo.getValue();

            if (partInfo.getShouldRender()) {

                int partIndex = partInfo.getPartIndex();
                int animationIndex = partInfo.getAnimationIndex();
                int paletteIndex = partInfo.getPaletteIndex();

                List<EffectImage> effectImages;
                if (paletteIndex < 0) {
                    effectImages = partInfo.getPartRenderer().renderAnimation(partIndex, animationIndex);
                } else {
                    effectImages = partInfo.getPartRenderer().renderAnimation(partIndex, animationIndex, paletteIndex);
                }

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

            int paletteIndex = (partInfo.getPaletteIndex() + 1);
            this.palettePicker.setSelectedIndex(paletteIndex);

            String palette = this.palettePicker.getSelectedItem().toString();
            paletteIndex = (palette.contains("Default"))?-1:(Integer.parseInt(palette)-1);

            this.updatePartPanel(partKey, partInfo.getIconFrameIndex(), paletteIndex);
        } else if (ae.getSource() == this.palettePicker) {
            String partKey = this.partPicker.getSelectedItem().toString();
            PartInfo partInfo = this.characterPartInfo.get(partKey);

            String palette = this.palettePicker.getSelectedItem().toString();
            int paletteIndex = (palette.contains("Default"))?-1:(Integer.parseInt(palette)-1);
            partInfo.setPaletteIndex(paletteIndex+1);

            this.updatePartPanel(partKey, partInfo.getIconFrameIndex(), paletteIndex);
        } else if (ae.getSource() == this.exitMenuItem) {
            this.dispose();
        } else if (ae.getSource() == this.changeMapButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a NexusTK map");

            fileChooser.setCurrentDirectory(new File(Resources.NTK_MAP_DIRECTORY));
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter mapFilter = new FileNameExtensionFilter("Maps (*.cmp;*.map)", "cmp", "map");
            fileChooser.addChoosableFileFilter(mapFilter);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                // Get Map File
                File selectedFile = fileChooser.getSelectedFile();
                int newMapId = Integer.parseInt(selectedFile.getName().replaceAll("TK", "").replaceAll(".cmp", ""));

                CmpFileHandler cmpFileHandler = new CmpFileHandler(selectedFile);
                xSpinner.setModel(new SpinnerNumberModel(0, 0, cmpFileHandler.mapWidth - 5, 1));
                ySpinner.setModel(new SpinnerNumberModel(0, 0, cmpFileHandler.mapHeight - 5, 1));
                mapId = newMapId;
            }
        }
    }
}