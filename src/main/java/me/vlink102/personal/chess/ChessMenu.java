package me.vlink102.personal.chess;

import com.github.weisj.darklaf.ui.tooltip.DarkDefaultToolTipBorder;
import com.mysql.cj.util.Base64Decoder;
import com.neovisionaries.i18n.CountryCode;
import me.vlink102.personal.GameSelector;
import me.vlink102.personal.Menu;
import me.vlink102.personal.chess.classroom.Classroom;
import me.vlink102.personal.chess.internal.*;
import me.vlink102.personal.chess.internal.networking.CommunicationHandler;
import me.vlink102.personal.chess.internal.networking.DataThread;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Challenge;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static java.awt.Component.LEFT_ALIGNMENT;
import static java.awt.Component.TOP_ALIGNMENT;
import static me.vlink102.personal.chess.Chess.createSocialMenu;

public class ChessMenu extends Menu {
    private static final Map<CountryCode, Image> FLAGS = new HashMap<>();
    public static String IDENTIFIER = null;

    public static JFrame frame;
    public final Image testing;
    public final Image board;
    private static List<Chess> instances;
    private static List<Classroom> classroomInstances;

    private final Font emojis;

    record LoginResult(String username, String password) {
    }

    public LoginResult loginPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        PlaceholderTextField ptf = new PlaceholderTextField("", 20);
        ptf.setPlaceholder("Username");
        PlaceholderPassField passwordField = new PlaceholderPassField("", 20);
        passwordField.setPlaceholder("Password");
        panel.add(ptf);
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login/Register", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
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
        frame = new JFrame("Chess ~ Menu");
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

    public void openProfileMenu() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel usernameLabel = new JLabel("Username");
        PlaceholderTextField usernameTextField = new PlaceholderTextField(CommunicationHandler.nameFromUUID(ChessMenu.IDENTIFIER), 20);
        usernameTextField.setEnabled(false);
        JPanel usernamePanel = ChessMenu.CreateChessGame.getCouplePanel(usernameLabel, usernameTextField, true);

        JLabel passwordLabel = new JLabel("Password");
        String password = CommunicationHandler.getPass(ChessMenu.IDENTIFIER);
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
        SpinnerModel spinnerModel = new SpinnerNumberModel(CommunicationHandler.getAge(ChessMenu.IDENTIFIER), 0, 100, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        spinner.setEnabled(false);
        JPanel agePanel = CreateChessGame.getCouplePanel(age, spinner, true);

        JLabel location = new JLabel("Country");
        JComboBox<CountryCode> locationComboBox = new JComboBox<>();
        locationComboBox.setRenderer(new IconListRenderer(FLAGS));
        for (CountryCode value : CountryCode.values()) {
            locationComboBox.addItem(value);
        }
        String iso = CommunicationHandler.getLocation(ChessMenu.IDENTIFIER);
        if (!Objects.equals(iso, "")) {
            locationComboBox.setSelectedItem(CountryCode.valueOf(iso));
        }
        locationComboBox.setEnabled(false);
        JPanel locationPanel = CreateChessGame.getCouplePanel(location, locationComboBox, true);

        JLabel aboutMe = new JLabel("About me");
        PlaceholderAreaField aboutMeArea = new PlaceholderAreaField(CommunicationHandler.getAboutMe(ChessMenu.IDENTIFIER), 7, 20);
        aboutMeArea.setPlaceholder("Something interesting...");
        aboutMeArea.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(aboutMeArea);
        scrollPane.setBorder(new DarkDefaultToolTipBorder());

        JPanel aboutMePanel = CreateChessGame.getCouplePanel(aboutMe, scrollPane, true);

        final String[] pfp = {CommunicationHandler.getProfilePicture(ChessMenu.IDENTIFIER)};
        JButton profilePic = new JButton("Change profile picture");
        profilePic.setEnabled(false);

        profilePic.addActionListener(new AbstractAction() {
            final JFileChooser chooser = new JFileChooser();
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.showOpenDialog(panel);
                File file = chooser.getSelectedFile();
                if (file == null) {
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

        JButton editButton = new JButton("Edit Profile");
        JButton saveChanges = new JButton("Save Changes");
        saveChanges.setEnabled(false);
        editButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usernameTextField.setEnabled(!usernameTextField.isEnabled());
                passwordField.setEnabled(!passwordField.isEnabled());
                spinner.setEnabled(!spinner.isEnabled());
                locationComboBox.setEnabled(!locationComboBox.isEnabled());
                aboutMeArea.setEnabled(!aboutMeArea.isEnabled());
                scrollPane.setEnabled(!scrollPane.isEnabled());
                saveChanges.setEnabled(!saveChanges.isEnabled());
                profilePic.setEnabled(!profilePic.isEnabled());
                editButton.setText(editButton.isEnabled() ? "Revert Changes" : "Edit Profile"); // revert in real time TODO
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

                CommunicationHandler.setString("name", usernameTextField.getText());
                CommunicationHandler.setString("password", passwordField.getText());
                CommunicationHandler.setNumber("age", (int) spinner.getValue());
                CommunicationHandler.setString("location", locationComboBox.getSelectedItem() == null ? "UNDEFINED" : locationComboBox.getSelectedItem().toString());
                CommunicationHandler.setString("about", aboutMeArea.getText());
                CommunicationHandler.setString("pfp", pfp[0]);

                JOptionPane.showMessageDialog(panel, "Changes Saved!", "Saved changes successfully", JOptionPane.INFORMATION_MESSAGE);
            }
        });



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
        panel.add(profilePic);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(new JSeparator());
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 30)));
        panel.add(options);

        BufferedImage image;
        byte[] imageBytes;

        imageBytes = Base64.getDecoder().decode(pfp[0]);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        try {
            image = ImageIO.read(bis);
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JOptionPane.showMessageDialog(frame, panel, "Your Profile", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(image.getScaledInstance(100, 100, Image.SCALE_SMOOTH)));


    }

    public JMenuBar getMenu() {
        JMenuBar settings = new JMenuBar();
        JMenuItem social = new JMenu("Social");
        JMenuItem profile = new JMenuItem("Profile (" + CommunicationHandler.nameFromUUID(ChessMenu.IDENTIFIER) + ")");
        profile.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProfileMenu();
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
            initUI();
        } else {
            close();
        }
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
    }

    @Override
    public void focus() {
        frame.requestFocus();
    }

    public static class CreateChessGame {
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
    }

    public static List<Chess> getInstances() {
        return instances;
    }


    public void loadFlags() {
        for (CountryCode countryCode : CountryCode.values()) {
            Image image = Move.getResource("/flags/" + countryCode.toString().toLowerCase() + ".png");
            FLAGS.put(countryCode, image);
        }
    }
}
