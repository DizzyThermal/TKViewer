package com.gamemode.tkviewer.gui;

import com.gamemode.tkpartpicker.resources.PartInfo;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.EffectImage;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TKPartPickerGUI extends JFrame implements ActionListener {

    Image clientIcon;

    JMenuBar menuBar;

    JMenu fileMenu = new JMenu("File");
    JMenuItem exitMenuItem = new JMenuItem("Exit");

    JComboBox partPicker;
    JPanel contentPanel;

    JPanel viewerPanel;
    ImageIcon viewerIcon;

    Integer tickValue = 0;

    // Renderers
    PartRenderer bodyRenderer;
    PartRenderer bowRenderer;
    PartRenderer coatRenderer;
    PartRenderer faceRenderer;
    PartRenderer faceDecRenderer;
    PartRenderer fanRenderer;
    PartRenderer hairRenderer;
    PartRenderer helmetRenderer;
    PartRenderer mantleRenderer;
    PartRenderer spearRenderer;
    PartRenderer shoeRenderer;
    PartRenderer shieldRenderer;
    PartRenderer swordRenderer;

    private static final String[] partPickerItems = {
            "Bodies",
            "Bows",
            "Coats",
            "Faces",
            "Face Decorations",
            "Fans",
            "Hair",
            "Helmets",
            "Mantles",
            "Spears",
            "Shoes",
            "Shields",
            "Swords"
    };
    LinkedHashMap<String, PartInfo> characterPartInfo;

    public TKPartPickerGUI(String title) {
        super(title);
        this.setPreferredSize(new Dimension(640, 480));
        this.clientIcon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("client_icon.png"));
        this.setIconImage(this.clientIcon);

        characterPartInfo = new LinkedHashMap<String, PartInfo>();
        characterPartInfo.put("Bodies", new PartInfo(0, 2, true, RenderUtils.createBodyRenderer()));
        characterPartInfo.put("Bows", new PartInfo(0, 0,false, RenderUtils.createBowRenderer()));
        characterPartInfo.put("Coats", new PartInfo(0, 0,false, RenderUtils.createCoatRenderer()));
        characterPartInfo.put("Faces", new PartInfo(0, 18,true, RenderUtils.createFaceRenderer()));
        characterPartInfo.put("Face Decorations", new PartInfo(0, 0,false, RenderUtils.createFaceDecRenderer()));
        characterPartInfo.put("Fans", new PartInfo(0, 0,false, RenderUtils.createFanRenderer()));
        characterPartInfo.put("Hair", new PartInfo(0, 18,true, RenderUtils.createHairRenderer()));
        characterPartInfo.put("Helmets", new PartInfo(0, 0,false, RenderUtils.createHelmetRenderer()));
        characterPartInfo.put("Mantles", new PartInfo(0, 0,false, RenderUtils.createMantleRenderer()));
        characterPartInfo.put("Spears", new PartInfo(0, 0,false, RenderUtils.createSpearRenderer()));
        characterPartInfo.put("Shoes", new PartInfo(0, 0,false, RenderUtils.createShoeRenderer()));
        characterPartInfo.put("Shields", new PartInfo(0, 0,false, RenderUtils.createShieldRenderer()));
        characterPartInfo.put("Swords", new PartInfo(0, 0,false, RenderUtils.createSwordRenderer()));

        initMenu();
        initPanel();

        SwingWorker loadingWorker = new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                while (!this.isCancelled()) {
                    Thread.sleep(250);

                    viewerPanel.removeAll();

                    viewerPanel.add(new JLabel(new ImageIcon(renderCharacter())));

                    viewerPanel.revalidate();
                    viewerPanel.repaint();
                }

                return true;
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
        contentPanel = new JPanel(new FlowLayout());

        // Add Viewer Panel
        viewerPanel = new JPanel(new FlowLayout());

        viewerIcon = new ImageIcon(renderCharacter());
        viewerPanel.add(new JLabel(viewerIcon));

        // Add Part List to ComboBox
        partPicker = new JComboBox(partPickerItems);
        partPicker.addActionListener(this);

        // Add content to the panel
        contentPanel.add(viewerPanel);
        contentPanel.add(partPicker);

        // Add the content panel to the parent JFrame (this)
        this.add(contentPanel);
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

        int number = 0;
        for (Map.Entry<String, PartInfo> characterPartInfo : this.characterPartInfo.entrySet()) {
            String partKey = characterPartInfo.getKey();
            PartInfo partInfo = characterPartInfo.getValue();

            if (partInfo.getShouldRender()) {
                int partIndex = partInfo.getPartIndex();
                int animationIndex = partInfo.getAnimationIndex();

                List<EffectImage> effectImages = partInfo.getPartRenderer().renderAnimation(partIndex, animationIndex);

                graphicsObject.drawImage(effectImages.get(tickValue).getImage(), null, 50, 50);
                System.out.println(tickValue);
                if (tickValue >= 1) {
                    tickValue = 0;
                } else {
                    tickValue++;
                }
            }
        }
        tickValue++;

        return characterImage;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == this.partPicker) {
            tickValue = 0;
            renderCharacter();
        } else if (ae.getSource() == this.exitMenuItem) {
            this.dispose();
            System.out.println();
        }
    }
}