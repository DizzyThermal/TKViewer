package com.gamemode.tkviewer;

import com.gamemode.tkviewer.file_handlers.*;
import com.gamemode.tkviewer.gui.ViewFrame;
import com.gamemode.tkviewer.render.MapRenderer;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.render.SObjRenderer;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GUI extends JFrame implements ActionListener {

    Image clientIcon;

    JMenuBar menuBar;

    JMenu fileMenu = new JMenu("File");
    JMenu openMenu = new JMenu("Open");
    JMenuItem openMapMenuItem = new JMenuItem("Map File (*.cmp | *.map)");
    JMenuItem exitMenuItem = new JMenuItem("Exit");

    JMenu viewMenu = new JMenu("View");
    JMenuItem viewBodyMenuItem = new JMenuItem("Bodies");
    JMenuItem viewBowMenuItem = new JMenuItem("Bows");
    JMenuItem viewCoatMenuItem = new JMenuItem("Coats");
    JMenuItem viewFaceMenuItem = new JMenuItem("Faces");
    JMenuItem viewFanMenuItem = new JMenuItem("Fans");
    JMenuItem viewHairMenuItem = new JMenuItem("Hair");
    JMenuItem viewHelmetMenuItem = new JMenuItem("Helmets");
    JMenuItem viewMantleMenuItem = new JMenuItem("Mantles");
    JMenuItem viewSpearMenuItem = new JMenuItem("Spears");
    JMenuItem viewShoesMenuItem = new JMenuItem("Shoes");
    JMenuItem viewShieldMenuItem = new JMenuItem("Shields");
    JMenuItem viewSwordMenuItem = new JMenuItem("Swords");

    // Renderers
    MapRenderer mapRenderer;
    PartRenderer bodyRenderer;
    PartRenderer bowRenderer;
    PartRenderer coatRenderer;
    PartRenderer faceRenderer;
    PartRenderer fanRenderer;
    PartRenderer hairRenderer;
    PartRenderer helmetRenderer;
    PartRenderer mantleRenderer;
    PartRenderer spearRenderer;
    PartRenderer shoesRenderer;
    PartRenderer shieldRenderer;
    PartRenderer swordRenderer;

    BufferedImage map;
    Image scaledMap;

    File saveFile;

    Dimension MAX_IMAGE_DIMENSIONS = new Dimension(800, 600);

    public GUI(String title) {
        super(title);
        this.setPreferredSize(new Dimension(640, 480));
        this.clientIcon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("client_icon.png"));
        this.setIconImage(this.clientIcon);

        initMenu();
    }

    public void initMenu() {
        // Add Menu
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        // File > Open > Map File
        openMapMenuItem.addActionListener(this);
        openMenu.add(openMapMenuItem);

        // File > Exit
        exitMenuItem.addActionListener(this);

        // File
        fileMenu.add(openMenu);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        // View > Bodies
        viewBodyMenuItem.addActionListener(this);
        viewMenu.add(viewBodyMenuItem);

        // View > Bows
        viewBowMenuItem.addActionListener(this);
        viewMenu.add(viewBowMenuItem);

        // View > Coats
        viewCoatMenuItem.addActionListener(this);
        viewMenu.add(viewCoatMenuItem);

        // View > Faces
        viewFaceMenuItem.addActionListener(this);
        viewMenu.add(viewFaceMenuItem);

        // View > Fans
        viewFanMenuItem.addActionListener(this);
        viewMenu.add(viewFanMenuItem);

        // View > Hair
        viewHairMenuItem.addActionListener(this);
        viewMenu.add(viewHairMenuItem);

        // View > Helmet
        viewHelmetMenuItem.addActionListener(this);
        viewMenu.add(viewHelmetMenuItem);

        // View > Mantles
        viewMantleMenuItem.addActionListener(this);
        viewMenu.add(viewMantleMenuItem);

        // View > Spears
        viewSpearMenuItem.addActionListener(this);
        viewMenu.add(viewSpearMenuItem);

        // View > Shoes
        viewShoesMenuItem.addActionListener(this);
        viewMenu.add(viewShoesMenuItem);

        // View > Shields
        viewShieldMenuItem.addActionListener(this);
        viewMenu.add(viewShieldMenuItem);

        // View > Swords
        viewSwordMenuItem.addActionListener(this);
        viewMenu.add(viewSwordMenuItem);

        menuBar.add(viewMenu);
    }

    public Dimension getScaledDimensions(Dimension currentDimensions, Dimension maxDimensions) {
        double widthScale = maxDimensions.getWidth() / currentDimensions.getWidth();
        double heightScale = maxDimensions.getHeight() / currentDimensions.getHeight();

        // If Image is already under maxDimensions
        if (widthScale >= 1 && heightScale >= 1) {
            return currentDimensions;
        }

        double scale = Math.min(widthScale, heightScale);

        return new Dimension((int)(currentDimensions.getWidth() * scale), (int)(currentDimensions.getHeight() * scale));
    }

    public void saveMap(File outputFile) {
        try {
            ImageIO.write(map, "png", outputFile);
        } catch (IOException ioe) {
            System.out.println("Error writing");
        }
    }

    public void showLoadingDialog(String message, Resources.GUI_LOADING_FUNCTION loadingFunction) {
        JDialog loadingNotification = new JDialog(this, "TKViewer", true);
        loadingNotification.setTitle("TKViewer");
        loadingNotification.setIconImage(this.clientIcon);

        loadingNotification.add(new JLabel(message, SwingConstants.CENTER));
        loadingNotification.setSize(new Dimension(300, 75));
        loadingNotification.setResizable(false);
        loadingNotification.setLocationRelativeTo(this);
        SwingWorker loadingWorker = new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                switch(loadingFunction) {
                    case BODIES:
                        loadBodyResources();
                        break;
                    case BOWS:
                        loadBowResources();
                        break;
                    case COATS:
                        loadCoatResources();
                        break;
                    case FACES:
                        loadFaceResources();
                        break;
                    case FANS:
                        loadFanResources();
                        break;
                    case HAIR:
                        loadHairResources();
                        break;
                    case HELMETS:
                        loadHelmetResources();
                        break;
                    case MANTLES:
                        loadMantleResources();
                        break;
                    case MAPS:
                        loadMapResources();
                        break;
                    case SPEARS:
                        loadSpearResources();
                        break;
                    case SHIELDS:
                        loadShieldResources();
                        break;
                    case SHOES:
                        loadShoesResources();
                        break;
                    case SWORDS:
                        loadSwordResources();
                        break;
                }

                return true;
            }

            @Override
            protected void done() {
                loadingNotification.setVisible(false);
                loadingNotification.dispose();
            }
        };
        loadingWorker.execute();
        loadingNotification.setVisible(true);
    }

    public void closeWindow(JFrame frame) {
        frame.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        // Open Map
        if (ae.getSource() == this.openMapMenuItem) {
            // Initialize Map Data if needed
            if (this.mapRenderer == null) {
                showLoadingDialog("Loading map resources, please wait...", Resources.GUI_LOADING_FUNCTION.MAPS);
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select a NexusTK map");

            Path nexusMaps = Paths.get(System.getProperty("user.home"), "Documents", "NexusTK", "Maps");
            fileChooser.setCurrentDirectory(nexusMaps.toFile());
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter mapFilter = new FileNameExtensionFilter("Maps (*.cmp;*.map)", "cmp", "map");
            fileChooser.addChoosableFileFilter(mapFilter);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                // Get Map File
                File selectedFile = fileChooser.getSelectedFile();
                String fileExtension = FilenameUtils.getExtension(selectedFile.getAbsolutePath());

                // Render Map
                if (fileExtension.equals("cmp") || fileExtension.equals("map")) {
                    JDialog renderloadingNotification = new JDialog(this, "TKViewer", true);
                    renderloadingNotification.setTitle("TKViewer");
                    renderloadingNotification.setIconImage(this.clientIcon);

                    renderloadingNotification.add(new JLabel("Rendering " + selectedFile.getName() + ", please wait...", SwingConstants.CENTER));
                    renderloadingNotification.setSize(new Dimension(300, 75));
                    renderloadingNotification.setResizable(false);
                    renderloadingNotification.setLocationRelativeTo(this);
                    SwingWorker renderLoadingWorker = new SwingWorker<Boolean, Integer>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            if (fileExtension.equals("cmp")) {
                                map = mapRenderer.renderMap(new CmpFileHandler(selectedFile));
                            } else if (fileExtension.equals("map")) {
                                map = mapRenderer.renderMap(new MapFileHandler(selectedFile));
                            }

                            return true;
                        }

                        @Override
                        protected void done() {
                            renderloadingNotification.setVisible(false);
                            renderloadingNotification.dispose();
                        }
                    };
                    renderLoadingWorker.execute();
                    renderloadingNotification.setVisible(true);
                }

                // Display Map
                JFrame mapFrame = new JFrame(selectedFile.getName());
                mapFrame.setIconImage(clientIcon);
                Dimension currentDimensions = new Dimension(((BufferedImage) map).getWidth(), ((BufferedImage) map).getHeight());
                Dimension scaledDimensions = getScaledDimensions(currentDimensions, Toolkit.getDefaultToolkit().getScreenSize());
                if (!currentDimensions.equals(scaledDimensions)) {
                    scaledMap = map.getScaledInstance((int)scaledDimensions.getWidth(), (int)scaledDimensions.getHeight(), Image.SCALE_SMOOTH);
                }
                JLabel label = new JLabel(new ImageIcon((scaledMap != null)?scaledMap:map));
                mapFrame.setSize(this.MAX_IMAGE_DIMENSIONS);
                mapFrame.add(label);
                mapFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                mapFrame.pack();
                mapFrame.setVisible(true);

                // Add Menu
                JMenuBar imageMenuBar = new JMenuBar();
                mapFrame.setJMenuBar(imageMenuBar);

                // File > Save (Full Quality)
                JMenuItem saveMenuItem = new JMenuItem("Save (Full Quality)");
                saveMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser saveFileChooser = new JFileChooser();
                        saveFileChooser.setDialogTitle("Save Full Quality Map");
                        saveFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                        saveFileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().getParentFile(), FilenameUtils.getBaseName(fileChooser.getSelectedFile().getName()) + ".png"));
                        int result = saveFileChooser.showDialog(mapFrame, "Save");
                        if (result == JFileChooser.APPROVE_OPTION) {
                            // Get Map File
                            saveFile = saveFileChooser.getSelectedFile();
                            if (FilenameUtils.getExtension(saveFile.getName()).equalsIgnoreCase("xml")) {
                                // filename is OK as-is
                            } else {
                                saveFile = new File(saveFile.getParentFile(), FilenameUtils.getBaseName(saveFile.getName()) + ".png");
                            }

                            JDialog saveMapLoadingNotification = new JDialog(mapFrame, "TKViewer", true);
                            saveMapLoadingNotification.setTitle("TKViewer");
                            saveMapLoadingNotification.setIconImage(clientIcon);

                            saveMapLoadingNotification.add(new JLabel("Saving " + FilenameUtils.getBaseName(saveFile.getName()) + ".png " + ", please wait...", SwingConstants.CENTER));
                            saveMapLoadingNotification.setSize(new Dimension(300, 75));
                            saveMapLoadingNotification.setResizable(false);
                            saveMapLoadingNotification.setLocationRelativeTo(mapFrame);
                            SwingWorker saveLoadingWorker = new SwingWorker<Boolean, Integer>() {
                                @Override
                                protected Boolean doInBackground() throws Exception {
                                    saveMap(saveFile);

                                    return true;
                                }

                                @Override
                                protected void done() {
                                    saveMapLoadingNotification.setVisible(false);
                                    saveMapLoadingNotification.dispose();
                                }
                            };
                            saveLoadingWorker.execute();
                            saveMapLoadingNotification.setVisible(true);

                            JOptionPane.showMessageDialog(mapFrame, "Map saved successfully!", "TKViewer", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });

                // File > Close
                JMenuItem closeMenuItem = new JMenuItem("Close");
                closeMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        scaledMap = null;
                        closeWindow(mapFrame);
                    }
                });

                // File
                JMenu imageFileMenu = new JMenu("File");
                imageFileMenu.add(saveMenuItem);
                imageFileMenu.add(closeMenuItem);
                imageMenuBar.add(imageFileMenu);
            }
        } else if (ae.getSource() == this.viewBodyMenuItem) {
            // Initialize Body Data if needed
            if (this.bodyRenderer == null) {
                showLoadingDialog("Loading body resources, please wait...", Resources.GUI_LOADING_FUNCTION.BODIES);
            }

            new ViewFrame("Bodies", "Body", "Bodies", this.bodyRenderer);
        } else if (ae.getSource() == this.viewBowMenuItem) {
            // Initialize Bow Data if needed
            if (this.bowRenderer == null) {
                showLoadingDialog("Loading bow resources, please wait...", Resources.GUI_LOADING_FUNCTION.BOWS);
            }

            new ViewFrame("Bows", "Bow", "Bows", this.bowRenderer);
        } else if (ae.getSource() == this.viewCoatMenuItem) {
            // Initialize Coat Data if needed
            if (this.coatRenderer == null) {
                showLoadingDialog("Loading coat resources, please wait...", Resources.GUI_LOADING_FUNCTION.COATS);
            }

            new ViewFrame("Coats", "Coat", "Coats", this.coatRenderer);
        } else if (ae.getSource() == this.viewFaceMenuItem) {
            // Initialize Face Data if needed
            if (this.faceRenderer == null) {
                showLoadingDialog("Loading face resources, please wait...", Resources.GUI_LOADING_FUNCTION.FACES);
            }

            new ViewFrame("Faces", "Face", "Faces", this.faceRenderer);
        } else if (ae.getSource() == this.viewFanMenuItem) {
            // Initialize Fan Data if needed
            if (this.fanRenderer == null) {
                showLoadingDialog("Loading fan resources, please wait...", Resources.GUI_LOADING_FUNCTION.FANS);
            }

            new ViewFrame("Fans", "Fan", "Fans", this.fanRenderer);
        } else if (ae.getSource() == this.viewHairMenuItem) {
            // Initialize Hair Data if needed
            if (this.hairRenderer == null) {
                showLoadingDialog("Loading hair resources, please wait...", Resources.GUI_LOADING_FUNCTION.HAIR);
            }

            new ViewFrame("Hair", "Hair", "Hair", this.hairRenderer);
        } else if (ae.getSource() == this.viewHelmetMenuItem) {
            // Initialize Hair Data if needed
            if (this.helmetRenderer == null) {
                showLoadingDialog("Loading helmet resources, please wait...", Resources.GUI_LOADING_FUNCTION.HELMETS);
            }

            new ViewFrame("Helmets", "Helmet", "Helmets", this.helmetRenderer);
        } else if (ae.getSource() == this.viewMantleMenuItem) {
            // Initialize Fan Data if needed
            if (this.mantleRenderer == null) {
                showLoadingDialog("Loading mantle resources, please wait...", Resources.GUI_LOADING_FUNCTION.MANTLES);
            }

            new ViewFrame("Mantles", "Mantle", "Mantles", this.mantleRenderer);
        } else if (ae.getSource() == this.viewSpearMenuItem) {
            // Initialize Spear Data if needed
            if (this.spearRenderer == null) {
                showLoadingDialog("Loading spear resources, please wait...", Resources.GUI_LOADING_FUNCTION.SPEARS);
            }

            new ViewFrame("Spears", "Spear", "Spears", this.spearRenderer);
        } else if (ae.getSource() == this.viewShieldMenuItem) {
            // Initialize Shield Data if needed
            if (this.shieldRenderer == null) {
                showLoadingDialog("Loading shield resources, please wait...", Resources.GUI_LOADING_FUNCTION.SHIELDS);
            }

            new ViewFrame("Shields", "Shield", "Shields", this.shieldRenderer);
        } else if (ae.getSource() == this.viewShoesMenuItem) {
            // Initialize Shoes Data if needed
            if (this.shoesRenderer == null) {
                showLoadingDialog("Loading shoes resources, please wait...", Resources.GUI_LOADING_FUNCTION.SHOES);
            }

            new ViewFrame("Shoes", "Shoe", "Shoes", this.shoesRenderer);
        } else if (ae.getSource() == this.viewSwordMenuItem) {
            // Initialize Sword Data if needed
            if (this.swordRenderer == null) {
                showLoadingDialog("Loading sword resources, please wait...", Resources.GUI_LOADING_FUNCTION.SWORDS);
            }

            new ViewFrame("Swords", "Sword", "Swords", this.swordRenderer);
        } else if (ae.getSource() == this.exitMenuItem) {
            System.exit(0);
        }
    }

    public void loadMapResources() {
        // Extract Required Map Files
        FileUtils.extractMapFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Tile Renderer (for AB (Ground) Tiles)
        TileRenderer tileRenderer =
                new TileRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getTileEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));
        // Static Object Renderer (for C (Static Object -- SObj) Tiles)
        SObjRenderer sObjRenderer = new SObjRenderer(new TileRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getTileCEpfs(Resources.DATA_DIRECTORY)), new PalFileHandler(new File(Resources.DATA_DIRECTORY, "TileC.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "TILEC.TBL"))), new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl")));
        // Map Renderer from TileRenderer and SObjRenderer
        mapRenderer = new MapRenderer(tileRenderer, sObjRenderer);
    }

    public void loadBodyResources() {
        // Extract Required Body Files
        FileUtils.extractBodyFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Body Resources
        bodyRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getBodyEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Body.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Body.dsc")));
    }

    public void loadBowResources() {
        // Extract Required Bow Files
        FileUtils.extractBowFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Bow Resources
        bowRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getBowEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Bow.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Bow.dsc")));
    }

    public void loadCoatResources() {
        // Extract Required Coat Files
        FileUtils.extractCoatFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Coat Resources
        coatRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getCoatEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Coat.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Coat.dsc")));
    }

    public void loadFaceResources() {
        // Extract Required Face Files
        FileUtils.extractFaceFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Face Resources
        faceRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getFaceEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Face.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Face.dsc")));
    }

    public void loadFanResources() {
        // Extract Required Fan Files
        FileUtils.extractFanFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Fan Resources
        fanRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getFanEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Fan.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Fan.dsc")));
    }

    public void loadHairResources() {
        // Extract Required Hair Files
        FileUtils.extractHairFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Hair Resources
        hairRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getHairEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Hair.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Hair.dsc")));
    }

    public void loadHelmetResources() {
        // Extract Required Helmet Files
        FileUtils.extractHelmetFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Helmet Resources
        helmetRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getHelmetEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Helmet.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Helmet.dsc")));
    }

    public void loadMantleResources() {
        // Extract Required Mantle Files
        FileUtils.extractMantleFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Mantle Resources
        mantleRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getMantleEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Mantle.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Mantle.dsc")));
    }

    public void loadSpearResources() {
        // Extract Required Spear Files
        FileUtils.extractSpearFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Spear Resources
        spearRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getSpearEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Spear.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Spear.dsc")));
    }

    public void loadShieldResources() {
        // Extract Required Shield Files
        FileUtils.extractShieldFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Shield Resources
        shieldRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getShieldEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Shield.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Shield.dsc")));
    }

    public void loadShoesResources() {
        // Extract Required Shoes Files
        FileUtils.extractShoesFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Shoes Resources
        shoesRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getShoesEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Shoes.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Shoes.dsc")));
    }

    public void loadSwordResources() {
        // Extract Required Sword Files
        FileUtils.extractSwordFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Sword Resources
        swordRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getSwordEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Sword.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Sword.dsc")));
    }
}