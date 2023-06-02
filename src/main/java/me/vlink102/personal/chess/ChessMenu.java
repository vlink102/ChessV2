package me.vlink102.personal.chess;

import com.github.weisj.darklaf.ui.tooltip.DarkDefaultToolTipBorder;
import com.neovisionaries.i18n.CountryCode;
import me.vlink102.personal.GameSelector;
import me.vlink102.personal.Menu;
import me.vlink102.personal.chess.classroom.Classroom;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.internal.PlaceholderAreaField;
import me.vlink102.personal.chess.internal.PlaceholderPassField;
import me.vlink102.personal.chess.internal.PlaceholderTextField;
import me.vlink102.personal.chess.internal.networking.CommunicationHandler;
import me.vlink102.personal.chess.internal.networking.DataThread;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Challenge;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;

import static me.vlink102.personal.chess.Chess.createSocialMenu;

public class ChessMenu extends Menu {
    public static final Map<CountryCode, Image> FLAGS = new HashMap<>();
    public static final String PROFILE_PLACEHOLDER = "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";
    public static String IDENTIFIER = null;
    public static JFrame frame;
    private static List<Chess> instances;
    private static List<Classroom> classroomInstances;
    public final Image testing;
    public final Image board;
    private final Font emojis;

    @SuppressWarnings("SpellCheckingInspection")
    public ChessMenu() {
        loadFlags();
        this.testing = Move.getResource("/testing.png");
        this.board = Move.getResource("/board.png");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream emojiStream = classLoader.getResourceAsStream("fonts/emojis.ttf");
        try {
            this.emojis = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(emojiStream));
            ge.registerFont(this.emojis);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }

        classroomInstances = new ArrayList<>();
        instances = new ArrayList<>();
        CommunicationHandler handler = new CommunicationHandler("ulucl02v8dm4l3qm", "bf5v9fiyfc6bqge4qrz1-mysql.services.clever-cloud.com", "bf5v9fiyfc6bqge4qrz1", 3306);

        if (validLogin()) {
            handler.establishConnection(IDENTIFIER);
            CommunicationHandler.loadProfiles();
            initUI();
        } else {
            close();
        }
    }

    public static void openOther(String uuid) {
        CommunicationHandler.ProfileCache newCache = CommunicationHandler.getProfile(uuid);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel usernameLabel = new JLabel("Username");
        JLabel username = new JLabel(newCache.name());
        JPanel usernamePanel = ChessMenu.CreateChessGame.getCouplePanel(usernameLabel, username, true);

        JLabel ageLabel = new JLabel("Age");
        JLabel age = new JLabel(String.valueOf(newCache.age()));
        JPanel agePanel = ChessMenu.CreateChessGame.getCouplePanel(ageLabel, age, true);

        JLabel location = new JLabel("Country");
        JComboBox<CountryCode> locationComboBox = GameSelector.locationJComboBox;
        locationComboBox.setSelectedItem(CountryCode.valueOf(newCache.location().equals("") ? "UNDEFINED" : newCache.location()));
        locationComboBox.setEnabled(false);
        JPanel locationPanel = CreateChessGame.getCouplePanel(location, locationComboBox, true);

        JLabel aboutMe = new JLabel("About me");
        PlaceholderAreaField aboutMeArea = new PlaceholderAreaField("", 7, 20);
        aboutMeArea.setText(newCache.about());
        aboutMeArea.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(aboutMeArea);
        scrollPane.setBorder(new DarkDefaultToolTipBorder());

        JPanel aboutMePanel = CreateChessGame.getCouplePanel(aboutMe, scrollPane, true);

        final String[] pfp = {newCache.profilePic()};

        JLabel icon = new JLabel();
        icon.setIcon(new ImageIcon(fromBase64(pfp[0]).getScaledInstance(100, 100, Image.SCALE_SMOOTH)));

        panel.add(usernamePanel);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(agePanel);
        panel.add(locationPanel);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(aboutMePanel);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());

        panel.setBorder(new EmptyBorder(0, 20, 0, 0));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrapper.add(icon, gbc);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.NORTHEAST;
        wrapper.add(panel, gbc2);
        JOptionPane.showOptionDialog(frame, wrapper, newCache.name() + "'s Profile", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }

    public static void openProfileMenu(String uuid) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel usernameLabel = new JLabel("Username");
        PlaceholderTextField usernameTextField = new PlaceholderTextField(20);
        usernameTextField.setEnabled(false);
        usernameTextField.setPlaceholder("Username");
        JPanel usernamePanel = ChessMenu.CreateChessGame.getCouplePanel(usernameLabel, usernameTextField, true);

        JLabel passwordLabel = new JLabel("Password");
        String password = CommunicationHandler.getPass(uuid);
        PlaceholderTextField passwordField = new PlaceholderTextField(20);
        passwordField.setPlaceholder("•".repeat(Objects.requireNonNull(password).length()));
        passwordField.setEnabled(false);
        passwordField.addPropertyChangeListener(evt -> {
            if (evt.getNewValue() != null) {
                switch (evt.getNewValue().toString()) {
                    case "false" -> passwordField.setText(null);
                    case "true" -> passwordField.setText(password);
                }
            }
        });
        JPanel passwordPanel = ChessMenu.CreateChessGame.getCouplePanel(passwordLabel, passwordField, true);

        JLabel age = new JLabel("Age");
        SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, 100, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        spinner.setEnabled(false);
        JPanel agePanel = CreateChessGame.getCouplePanel(age, spinner, true);

        JLabel location = new JLabel("Country");
        JComboBox<CountryCode> locationComboBox = GameSelector.locationJComboBox;
        locationComboBox.setSelectedItem(null);
        locationComboBox.setEnabled(false);
        JPanel locationPanel = CreateChessGame.getCouplePanel(location, locationComboBox, true);

        JLabel aboutMe = new JLabel("About me");
        PlaceholderAreaField aboutMeArea = new PlaceholderAreaField("", 7, 20);
        aboutMeArea.setPlaceholder("Something interesting...");
        aboutMeArea.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(aboutMeArea);
        scrollPane.setBorder(new DarkDefaultToolTipBorder());

        JPanel aboutMePanel = CreateChessGame.getCouplePanel(aboutMe, scrollPane, true);

        final String[] pfp = {""};

        JButton resetProfilePic = new JButton("Reset profile picture");
        resetProfilePic.setEnabled(false);
        resetProfilePic.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pfp[0] = ChessMenu.PROFILE_PLACEHOLDER;
                JOptionPane.showMessageDialog(null, "You successfully reset your profile picture.\nThis will only update if you save your changes.", "Success!", JOptionPane.INFORMATION_MESSAGE, null);
            }
        });

        JButton profilePic = new JButton("Change profile picture");
        profilePic.setEnabled(false);

        profilePic.addActionListener(new AbstractAction() {
            final JFileChooser chooser = new JFileChooser();

            @Override
            public void actionPerformed(ActionEvent e) {
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        String name = f.getName();
                        return (f.isDirectory() || (name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".jpg")) && (f.length() <= (1024L * 1024L)));
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                };
                chooser.addChoosableFileFilter(filter);
                chooser.setFileFilter(filter);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.showOpenDialog(panel);
                File file = chooser.getSelectedFile();
                if (file == null || file.isDirectory()) {
                    return;
                }
                try {
                    byte[] imageContent = FileUtils.readFileToByteArray(file);
                    pfp[0] = Base64.getEncoder().encodeToString(imageContent);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
        });

        JPanel profilePicButtons = CreateChessGame.getCouplePanel(resetProfilePic, profilePic, true);

        JLabel icon = new JLabel();
        JButton editButton = new JButton("Edit Profile");
        JButton saveChanges = new JButton("Save Changes");
        saveChanges.setEnabled(false);
        final boolean[] v = {true};
        editButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEnabledStates(usernameTextField, passwordField, spinner, locationComboBox, aboutMeArea, scrollPane, saveChanges, profilePic, resetProfilePic, editButton, v, uuid, pfp, icon);
            }
        });

        saveChanges.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (usernameTextField.getText().length() <= 3) {
                    JOptionPane.showMessageDialog(panel, "Username is too short", "Could not save changes", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (passwordField.getText().length() <= 3) {
                    JOptionPane.showMessageDialog(panel, "Password is too short", "Could not save changes", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String loc = locationComboBox.getSelectedItem() == null ? "UNDEFINED" : locationComboBox.getSelectedItem().toString();
                CommunicationHandler.setString("name", usernameTextField.getText());
                CommunicationHandler.setString("password", passwordField.getText());
                CommunicationHandler.setNumber("age", (int) spinner.getValue());
                CommunicationHandler.setString("location", loc);
                CommunicationHandler.setString("about", aboutMeArea.getText());
                CommunicationHandler.setString("pfp", pfp[0].equals("") ? ChessMenu.PROFILE_PLACEHOLDER : pfp[0]);

                CommunicationHandler.cachedProfiles.put(uuid, new CommunicationHandler.ProfileCache(pfp[0], aboutMeArea.getText(), usernameTextField.getText(), (int) spinner.getValue(), loc));

                refresh(usernameTextField, passwordField, uuid, spinner, locationComboBox, aboutMeArea, pfp, icon);
                updateEnabledStates(usernameTextField, passwordField, spinner, locationComboBox, aboutMeArea, scrollPane, saveChanges, profilePic, resetProfilePic, editButton, v, uuid, pfp, icon);
                JOptionPane.showMessageDialog(panel, "Changes Saved!", "Saved changes successfully", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        refresh(usernameTextField, passwordField, uuid, spinner, locationComboBox, aboutMeArea, pfp, icon);

        JPanel options = CreateChessGame.getCouplePanel(editButton, saveChanges, true);

        panel.add(usernamePanel);
        panel.add(passwordPanel);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(agePanel);
        panel.add(locationPanel);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(aboutMePanel);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(profilePicButtons);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 30)));
        panel.add(options);

        panel.setBorder(new EmptyBorder(0, 20, 0, 0));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrapper.add(icon, gbc);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.NORTHEAST;
        wrapper.add(panel, gbc2);

        JOptionPane.showOptionDialog(frame, wrapper, "Your Profile", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }

    private static void updateEnabledStates(PlaceholderTextField usernameTextField, PlaceholderTextField passwordField, JSpinner spinner, JComboBox<CountryCode> locationComboBox, PlaceholderAreaField aboutMeArea, JScrollPane scrollPane, JButton saveChanges, JButton profilePic, JButton resetProfilePic, JButton editButton, boolean[] v, String uuid, String[] pfp, JLabel icon) {
        usernameTextField.setEnabled(!usernameTextField.isEnabled());
        passwordField.setEnabled(!passwordField.isEnabled());
        spinner.setEnabled(!spinner.isEnabled());
        locationComboBox.setEnabled(!locationComboBox.isEnabled());
        aboutMeArea.setEnabled(!aboutMeArea.isEnabled());
        scrollPane.setEnabled(!scrollPane.isEnabled());
        saveChanges.setEnabled(!saveChanges.isEnabled());
        profilePic.setEnabled(!profilePic.isEnabled());
        resetProfilePic.setEnabled(!resetProfilePic.isEnabled());
        editButton.setText(v[0] ? "Revert Changes" : "Edit Profile");

        refresh(usernameTextField, passwordField, uuid, spinner, locationComboBox, aboutMeArea, pfp, icon);

        v[0] = !v[0];
    }

    private static void refresh(PlaceholderTextField usernameTextField, PlaceholderTextField passwordField, String uuid, JSpinner spinner, JComboBox<CountryCode> locationComboBox, PlaceholderAreaField aboutMeArea, String[] pfp, JLabel icon) {
        CommunicationHandler.ProfileCache newCache = CommunicationHandler.getProfile(uuid);
        usernameTextField.setText(newCache.name());
        passwordField.setText(CommunicationHandler.getPass(uuid));
        spinner.setValue(newCache.age());
        String loc = newCache.location();
        locationComboBox.setSelectedItem(CountryCode.valueOf(loc.equals("") ? "UNDEFINED" : loc));
        aboutMeArea.setText(newCache.about());
        pfp[0] = newCache.profilePic();
        icon.setIcon(new ImageIcon(fromBase64(pfp[0]).getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
    }

    public static Image fromBase64(String string) {
        BufferedImage image;
        byte[] imageBytes;

        imageBytes = Base64.getDecoder().decode(string);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        try {
            image = ImageIO.read(bis);
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    public static String toBase64(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
            byte[] res = outputStream.toByteArray();
            outputStream.close();
            return new String(Base64.getEncoder().encode(res));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Chess> getInstances() {
        return instances;
    }

    public LoginResult loginPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(new JLabel("(Note: Registering accounts requires an administrator)"));
        JPanel panel = new JPanel(new FlowLayout());
        PlaceholderTextField ptf = new PlaceholderTextField("", 20);
        ptf.setPlaceholder("Username");
        PlaceholderPassField passwordField = new PlaceholderPassField("", 20);
        passwordField.setPlaceholder("Password");
        panel.add(ptf);
        panel.add(passwordField);
        wrapper.add(panel);

        Object[] options = {"Login"};
        int result = JOptionPane.showOptionDialog(null, wrapper, "Login", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        } else {
            return new LoginResult(ptf.getText(), new String(passwordField.getPassword()));
        }
    }

    public boolean validLogin() {
        LoginResult result = loginPanel();
        if (result == null) return false;
        return CommunicationHandler.validateLogin(result.username, result.password);
    }

    public void initUI() {
        frame = new JFrame("Chess - Menu");
        JPanel panel = new JPanel();
        panel.add(new GameSelector.MenuButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateChessGame.TestingResult panel = CreateChessGame.openTestingPopup();
                if (panel != null) {
                    classroomInstances.add(new Classroom(panel.pSz(), panel.boardSize(), panel.online(), panel.playAsWhite(), panel.pieceDesign(), panel.boardTheme()));
                }
            }
        }, testing, 50, 50, 150, 150));
        panel.add(new GameSelector.MenuButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateChessGame.PanelResult panel = CreateChessGame.openChessPopup(false);
                if (panel != null) {
                    instances.add(new Chess(false, 0, null, null, panel.pSz(), panel.boardSize(), panel.online(), panel.playAsWhite(), panel.opponentType(), panel.gameType(), panel.layout(), panel.pieceDesign(), panel.boardTheme(), panel.moveMethod(), panel.moveStyle(), panel.captureStyle(), panel.coordinateDisplayType()));
                }
            }
        }, board, 50, 100, 150, 150));
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                close();
            }
        });

        frame.setResizable(true);
        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());

        frame.setJMenuBar(getMenu());

        frame.setIconImage(GameSelector.getImageIcon());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();
    }

    public JMenuBar getMenu() {
        JMenuBar settings = new JMenuBar();
        JMenuItem social = new JMenu("Social");
        JMenuItem profile = new JMenuItem("Profile (" + CommunicationHandler.nameFromUUID(ChessMenu.IDENTIFIER) + ")");
        profile.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProfileMenu(IDENTIFIER);
            }
        });
        social.add(profile);

        JMenuItem online = new JMenuItem("Online");
        online.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Chess.SocialMenuResult panel = createSocialMenu();
                if (panel == null) {
                    JOptionPane.showConfirmDialog(frame, "Nobody is online yet", "It's quiet in here...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
                } else {
                    Object[] options = {"Challenge", "Cancel"};
                    int r = JOptionPane.showOptionDialog(frame, panel.panel(), "Challenge a friend", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (r == JOptionPane.OK_OPTION) {
                        String s = panel.list().getSelectedValue();
                        if (s == null) return;
                        String uuid = CommunicationHandler.UUIDfromName(s);

                        ChessMenu.CreateChessGame.PanelResult panelResult = ChessMenu.CreateChessGame.openChessPopup(true);
                        if (panelResult != null) {
                            Challenge c = new Challenge(ChessMenu.IDENTIFIER, uuid, panelResult.boardSize(), panelResult.playAsWhite(), panelResult.gameType(), panelResult.layout(), BoardGUI.createFEN(panelResult.layout(), panelResult.boardSize()));

                            CommunicationHandler.thread.sendPacket(c);
                            CommunicationHandler.thread.getPendingChallenges().put(c.getID(), new DataThread.PendingChallenge(c.toJSON(), panelResult.pSz(), panelResult.pieceDesign(), panelResult.boardTheme(), panelResult.moveMethod(), panelResult.moveStyle(), panelResult.captureStyle(), panelResult.coordinateDisplayType()));
                        }
                    }
                }
            }
        });
        social.add(online);
        settings.add(social);
        return settings;
    }

    @Override
    public void close() {
        GameSelector.closeMenuInstance(GameSelector.Game.CHESS);
        for (Chess instance : instances) {
            instance.dispatchEvent(new WindowEvent(instance.frame, WindowEvent.WINDOW_CLOSING));
        }
        for (Classroom testingInstance : classroomInstances) {
            testingInstance.dispatchEvent(new WindowEvent(testingInstance.frame, WindowEvent.WINDOW_CLOSING));
        }
        frame.dispatchEvent(new WindowEvent(ChessMenu.frame, WindowEvent.WINDOW_CLOSING));
        GameSelector.frame.setVisible(true);
    }

    @Override
    public void focus() {
        frame.requestFocus();
    }

    public void loadFlags() {
        for (CountryCode countryCode : CountryCode.values()) {
            Image image = Move.getResource("/flags/" + countryCode.toString().toLowerCase() + ".png");
            FLAGS.put(countryCode, image);
        }
    }

    record LoginResult(String username, String password) {
    }

    public static class CreateChessGame {
        public static JPanel getTriplePanel(JComponent a, JComponent b, JComponent c) {
            JPanel row = new JPanel(new GridLayout(1, 3, 20, 5));
            row.add(a);
            row.add(b);
            row.add(c);
            return row;
        }

        public static JPanel getCouplePanel(JComponent left, JComponent right, boolean align) {
            JPanel inner = new JPanel(new BorderLayout());
            if (align) {
                left.setAlignmentX(Component.LEFT_ALIGNMENT);
                right.setAlignmentX(Component.RIGHT_ALIGNMENT);
            }
            inner.add(left, BorderLayout.WEST);
            inner.add(right, BorderLayout.EAST);

            return inner;
        }

        public static PanelResult openChessPopup(boolean challengeMenu) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JCheckBox radioButton = new JCheckBox();
            radioButton.setSelected(true);
            JLabel l = new JLabel("Play as white?: ");
            JPanel inner = getCouplePanel(l, radioButton, true);

            JCheckBox online = new JCheckBox();
            online.setSelected(true);
            JLabel onlineLabel = new JLabel("Online mode: ");
            JPanel onlinePanel = getCouplePanel(onlineLabel, online, true);

            SpinnerModel model = new SpinnerNumberModel(100, 32, 150, 1);
            JSpinner spinner = new JSpinner(model);
            JLabel label = new JLabel("Piece Size (px): ");
            JPanel inner1 = getCouplePanel(label, spinner, true);

            JComboBox<Chess.BoardLayout> boardLayoutJComboBox = new JComboBox<>(Chess.BoardLayout.values());
            boardLayoutJComboBox.setSelectedItem(Chess.BoardLayout.DEFAULT);
            SpinnerModel model1 = new SpinnerNumberModel(8, 4, 26, 1);
            JSpinner spinner1 = new JSpinner(model1);

            boardLayoutJComboBox.addItemListener(e -> spinner1.setEnabled(boardLayoutJComboBox.getSelectedItem() != Chess.BoardLayout.CHESS960));
            spinner1.addChangeListener(e -> boardLayoutJComboBox.setEnabled(spinner1.getValue().equals(8)));

            JLabel label1 = new JLabel("Board Size: ");
            JPanel inner2 = getCouplePanel(label1, spinner1, true);

            JComboBox<BoardGUI.OpponentType> opponentTypeJComboBox = new JComboBox<>(BoardGUI.OpponentType.values());
            opponentTypeJComboBox.setSelectedItem(challengeMenu ? BoardGUI.OpponentType.PLAYER : BoardGUI.OpponentType.AUTO_SWAP);
            if (challengeMenu) {
                opponentTypeJComboBox.setEnabled(false);
            } else {
                opponentTypeJComboBox.removeItem(BoardGUI.OpponentType.PLAYER);
            }
            JLabel opponentLabel = new JLabel("Opponent Type: ");
            JPanel opponentPanel = getCouplePanel(opponentLabel, opponentTypeJComboBox, true);

            JComboBox<BoardGUI.GameType> gameTypeJComboBox = new JComboBox<>(BoardGUI.GameType.values());
            gameTypeJComboBox.setSelectedItem(BoardGUI.GameType.DEFAULT);
            JLabel gameTypeLabel = new JLabel("Game Type: ");
            JPanel gameTypePanel = getCouplePanel(gameTypeLabel, gameTypeJComboBox, true);

            JLabel boardLayoutLabel = new JLabel("Board Layout: ");
            JPanel boardLayoutPanel = getCouplePanel(boardLayoutLabel, boardLayoutJComboBox, true);

            JComboBox<BoardGUI.PieceDesign> pieceDesignJComboBox = new JComboBox<>(BoardGUI.PieceDesign.values());
            pieceDesignJComboBox.setSelectedItem(BoardGUI.PieceDesign.NEO);
            JLabel pieceDesignLabel = new JLabel("Piece Theme: ");
            JPanel pieceDesignPanel = getCouplePanel(pieceDesignLabel, pieceDesignJComboBox, true);

            JComboBox<BoardGUI.Colours> boardThemeJComboBox = new JComboBox<>(BoardGUI.Colours.values());
            boardThemeJComboBox.setSelectedItem(BoardGUI.Colours.GREEN);
            JLabel boardThemeLabel = new JLabel("Board Theme: ");
            JPanel boardThemePanel = getCouplePanel(boardThemeLabel, boardThemeJComboBox, true);

            JComboBox<BoardGUI.MoveStyle> moveMethodJComboBox = new JComboBox<>(BoardGUI.MoveStyle.values());
            moveMethodJComboBox.setSelectedItem(BoardGUI.MoveStyle.BOTH);
            JLabel moveMethodLabel = new JLabel("Move Method: ");
            JPanel moveMethodPanel = getCouplePanel(moveMethodLabel, moveMethodJComboBox, true);

            JComboBox<BoardGUI.HintStyle.Move> moveJComboBox = new JComboBox<>(BoardGUI.HintStyle.Move.values());
            moveJComboBox.setSelectedItem(BoardGUI.HintStyle.Move.DOT);
            JLabel moveLabel = new JLabel("Move Style: ");
            JPanel movePanel = getCouplePanel(moveLabel, moveJComboBox, true);

            JComboBox<BoardGUI.HintStyle.Capture> captureJComboBox = new JComboBox<>(BoardGUI.HintStyle.Capture.values());
            captureJComboBox.setSelectedItem(BoardGUI.HintStyle.Capture.RING);
            JLabel captureLabel = new JLabel("Capture Style: ");
            JPanel capturePanel = getCouplePanel(captureLabel, captureJComboBox, true);

            JComboBox<BoardGUI.CoordinateDisplayType> coordinateDisplayTypeJComboBox = new JComboBox<>(BoardGUI.CoordinateDisplayType.values());
            coordinateDisplayTypeJComboBox.setSelectedItem(BoardGUI.CoordinateDisplayType.INSIDE);
            JLabel cdtLabel = new JLabel("Coordinate Display: ");
            JPanel cdtPanel = getCouplePanel(cdtLabel, coordinateDisplayTypeJComboBox, true);

            panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 20)));
            panel.add(inner1);
            panel.add(inner2);
            panel.add(new JSeparator());
            panel.add(inner);
            panel.add(onlinePanel);
            panel.add(new JSeparator());
            panel.add(opponentPanel);
            panel.add(gameTypePanel);
            panel.add(boardLayoutPanel);
            panel.add(new JSeparator());
            panel.add(pieceDesignPanel);
            panel.add(boardThemePanel);
            panel.add(new JSeparator());
            panel.add(moveMethodPanel);
            panel.add(new JSeparator());
            panel.add(movePanel);
            panel.add(capturePanel);
            panel.add(new JSeparator());
            panel.add(cdtPanel);
            panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 20)));

            int result = JOptionPane.showConfirmDialog(frame, panel, "Select Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            return new PanelResult((int) spinner.getValue(), (int) spinner1.getValue(), radioButton.isSelected(), online.isSelected(), (BoardGUI.OpponentType) opponentTypeJComboBox.getSelectedItem(), (BoardGUI.GameType) gameTypeJComboBox.getSelectedItem(), (Chess.BoardLayout) boardLayoutJComboBox.getSelectedItem(), (BoardGUI.PieceDesign) pieceDesignJComboBox.getSelectedItem(), (BoardGUI.Colours) boardThemeJComboBox.getSelectedItem(), (BoardGUI.MoveStyle) moveMethodJComboBox.getSelectedItem(), (BoardGUI.HintStyle.Move) moveJComboBox.getSelectedItem(), (BoardGUI.HintStyle.Capture) captureJComboBox.getSelectedItem(), (BoardGUI.CoordinateDisplayType) coordinateDisplayTypeJComboBox.getSelectedItem());
        }

        public static TestingResult openTestingPopup() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JCheckBox radioButton = new JCheckBox();
            radioButton.setSelected(true);
            JLabel l = new JLabel("Play as white?: ");
            JPanel inner = getCouplePanel(l, radioButton, true);

            JCheckBox online = new JCheckBox();
            online.setSelected(true);
            JLabel onlineLabel = new JLabel("Online mode: ");
            JPanel onlinePanel = getCouplePanel(onlineLabel, online, true);

            SpinnerModel model = new SpinnerNumberModel(100, 32, 150, 1);
            JSpinner spinner = new JSpinner(model);
            JLabel label = new JLabel("Piece Size (px): ");
            JPanel inner1 = getCouplePanel(label, spinner, true);

            SpinnerModel model1 = new SpinnerNumberModel(8, 4, 26, 1);
            JSpinner spinner1 = new JSpinner(model1);
            JLabel label1 = new JLabel("Board Size: ");
            JPanel inner2 = getCouplePanel(label1, spinner1, true);

            JComboBox<BoardGUI.PieceDesign> pieceDesignJComboBox = new JComboBox<>(BoardGUI.PieceDesign.values());
            pieceDesignJComboBox.setSelectedItem(BoardGUI.PieceDesign.NEO);
            JLabel pieceDesignLabel = new JLabel("Piece Theme: ");
            JPanel pieceDesignPanel = getCouplePanel(pieceDesignLabel, pieceDesignJComboBox, true);

            JComboBox<BoardGUI.Colours> boardThemeJComboBox = new JComboBox<>(BoardGUI.Colours.values());
            boardThemeJComboBox.setSelectedItem(BoardGUI.Colours.GREEN);
            JLabel boardThemeLabel = new JLabel("Board Theme: ");
            JPanel boardThemePanel = getCouplePanel(boardThemeLabel, boardThemeJComboBox, true);

            panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 20)));
            panel.add(inner);
            panel.add(onlinePanel);
            panel.add(new JSeparator());
            panel.add(inner1);
            panel.add(inner2);
            panel.add(new JSeparator());
            panel.add(pieceDesignPanel);
            panel.add(boardThemePanel);
            panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 20)));

            int result = JOptionPane.showConfirmDialog(frame, panel, "Select Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            return new TestingResult((int) spinner.getValue(), (int) spinner1.getValue(), radioButton.isSelected(), online.isSelected(), (BoardGUI.PieceDesign) pieceDesignJComboBox.getSelectedItem(), (BoardGUI.Colours) boardThemeJComboBox.getSelectedItem());
        }

        public record PanelResult(int pSz, int boardSize, boolean playAsWhite, boolean online,
                                  BoardGUI.OpponentType opponentType, BoardGUI.GameType gameType,
                                  Chess.BoardLayout layout, BoardGUI.PieceDesign pieceDesign,
                                  BoardGUI.Colours boardTheme, BoardGUI.MoveStyle moveMethod,
                                  BoardGUI.HintStyle.Move moveStyle, BoardGUI.HintStyle.Capture captureStyle,
                                  BoardGUI.CoordinateDisplayType coordinateDisplayType) {
        }

        public record TestingResult(int pSz, int boardSize, boolean playAsWhite, boolean online,
                                    BoardGUI.PieceDesign pieceDesign, BoardGUI.Colours boardTheme) {
        }
    }
}
