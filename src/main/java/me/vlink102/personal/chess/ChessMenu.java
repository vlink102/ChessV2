package me.vlink102.personal.chess;

import me.vlink102.personal.GameSelector;
import me.vlink102.personal.Menu;
import me.vlink102.personal.chess.classroom.Classroom;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.internal.networking.CommunicationHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class ChessMenu extends Menu {
    public static String IDENTIFIER = null;

    public static JFrame frame;
    public final Image testing;
    public final Image board;
    private static List<Chess> instances;
    private static List<Classroom> classroomInstances;

    record LoginResult(String username, String password) {}

    public LoginResult loginPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JTextField textField = new JTextField("username", 20);
        JPasswordField passwordField = new JPasswordField("password", 20);
        panel.add(textField);
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login/Register", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        } else {
            return new LoginResult(textField.getText(), new String(passwordField.getPassword()));
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
                    classroomInstances.add(Classroom.initUI(panel.pSz(), panel.boardSize(), panel.online(), panel.playAsWhite(), panel.pieceDesign(), panel.boardTheme()));
                }
            }
        }, testing, 50, 50, 150, 150));
        panel.add(new GameSelector.MenuButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateChessGame.PanelResult panel = CreateChessGame.openChessPopup();
                if (panel != null) {
                    instances.add(Chess.initUI(false, panel.pSz(), panel.boardSize(), panel.online(), panel.playAsWhite(), panel.opponentType(), panel.gameType(), panel.layout(), panel.pieceDesign(), panel.boardTheme(), panel.moveMethod(), panel.moveStyle(), panel.captureStyle(), panel.coordinateDisplayType()));
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

        frame.setIconImage(GameSelector.getImageIcon());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();
    }

    public ChessMenu() {
        this.testing = Move.getResource("/testing.png");
        this.board = Move.getResource("/board.png");
        classroomInstances = new ArrayList<>();
        instances = new ArrayList<>();
        new CommunicationHandler("ulucl02v8dm4l3qm", "bf5v9fiyfc6bqge4qrz1-mysql.services.clever-cloud.com", "bf5v9fiyfc6bqge4qrz1", 3306);

        if (validLogin()) {
            CommunicationHandler.establishConnection(IDENTIFIER);
            initUI();
        } else {
            close();
        }

    }

    @Override
    public void close() {
        GameSelector.closeMenuInstance(GameSelector.Game.CHESS);
        for (Chess instance : instances) {
            instance.dispatchEvent(new WindowEvent(Chess.frame, WindowEvent.WINDOW_CLOSING));
        }
        for (Classroom testingInstance : classroomInstances) {
            testingInstance.dispatchEvent(new WindowEvent(Classroom.frame, WindowEvent.WINDOW_CLOSING));
        }
        frame.dispatchEvent(new WindowEvent(ChessMenu.frame, WindowEvent.WINDOW_CLOSING));

    }

    @Override
    public void focus() {
        frame.requestFocus();
    }

    public static class CreateChessGame {
        public record PanelResult(int pSz, int boardSize, boolean playAsWhite, boolean online, BoardGUI.OpponentType opponentType, BoardGUI.GameType gameType, Chess.BoardLayout layout, BoardGUI.PieceDesign pieceDesign, BoardGUI.Colours boardTheme, BoardGUI.MoveStyle moveMethod, BoardGUI.HintStyle.Move moveStyle, BoardGUI.HintStyle.Capture captureStyle, BoardGUI.CoordinateDisplayType coordinateDisplayType) {
        }
        public record TestingResult(int pSz, int boardSize, boolean playAsWhite, boolean online, BoardGUI.PieceDesign pieceDesign, BoardGUI.Colours boardTheme) {
        }

        private static JPanel getCouplePanel(JComponent a, JComponent b) {
            JPanel inner = new JPanel(new BorderLayout());
            a.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setAlignmentX(Component.RIGHT_ALIGNMENT);
            inner.add(a, BorderLayout.WEST);
            inner.add(b, BorderLayout.EAST);

            return inner;
        }

        public static PanelResult openChessPopup() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JCheckBox radioButton = new JCheckBox();
            radioButton.setSelected(true);
            JLabel l = new JLabel("Play as white?: ");
            JPanel inner = getCouplePanel(l, radioButton);

            JCheckBox online = new JCheckBox();
            online.setSelected(true);
            JLabel onlineLabel = new JLabel("Online mode: ");
            JPanel onlinePanel = getCouplePanel(onlineLabel, online);

            SpinnerModel model = new SpinnerNumberModel(100, 32, 150, 1);
            JSpinner spinner = new JSpinner(model);
            JLabel label = new JLabel("Piece Size (px): ");
            JPanel inner1 = getCouplePanel(label, spinner);

            SpinnerModel model1 = new SpinnerNumberModel(8, 4, 26, 1);
            JSpinner spinner1 = new JSpinner(model1);
            JLabel label1 = new JLabel("Board Size: ");
            JPanel inner2 = getCouplePanel(label1, spinner1);

            JComboBox<BoardGUI.OpponentType> opponentTypeJComboBox = new JComboBox<>(BoardGUI.OpponentType.values());
            opponentTypeJComboBox.setSelectedItem(BoardGUI.OpponentType.AUTO_SWAP);
            JLabel opponentLabel = new JLabel("Opponent Type: ");
            JPanel opponentPanel = getCouplePanel(opponentLabel, opponentTypeJComboBox);

            JComboBox<BoardGUI.GameType> gameTypeJComboBox = new JComboBox<>(BoardGUI.GameType.values());
            gameTypeJComboBox.setSelectedItem(BoardGUI.GameType.DEFAULT);
            JLabel gameTypeLabel = new JLabel("Game Type: ");
            JPanel gameTypePanel = getCouplePanel(gameTypeLabel, gameTypeJComboBox);

            JComboBox<Chess.BoardLayout> boardLayoutJComboBox = new JComboBox<>(Chess.BoardLayout.values());
            boardLayoutJComboBox.setSelectedItem(Chess.BoardLayout.DEFAULT);
            JLabel boardLayoutLabel = new JLabel("Board Layout: ");
            JPanel boardLayoutPanel = getCouplePanel(boardLayoutLabel, boardLayoutJComboBox);

            JComboBox<BoardGUI.PieceDesign> pieceDesignJComboBox = new JComboBox<>(BoardGUI.PieceDesign.values());
            pieceDesignJComboBox.setSelectedItem(BoardGUI.PieceDesign.NEO);
            JLabel pieceDesignLabel = new JLabel("Piece Theme: ");
            JPanel pieceDesignPanel = getCouplePanel(pieceDesignLabel, pieceDesignJComboBox);

            JComboBox<BoardGUI.Colours> boardThemeJComboBox = new JComboBox<>(BoardGUI.Colours.values());
            boardThemeJComboBox.setSelectedItem(BoardGUI.Colours.GREEN);
            JLabel boardThemeLabel = new JLabel("Board Theme: ");
            JPanel boardThemePanel = getCouplePanel(boardThemeLabel, boardThemeJComboBox);

            JComboBox<BoardGUI.MoveStyle> moveMethodJComboBox = new JComboBox<>(BoardGUI.MoveStyle.values());
            moveMethodJComboBox.setSelectedItem(BoardGUI.MoveStyle.BOTH);
            JLabel moveMethodLabel = new JLabel("Move Method: ");
            JPanel moveMethodPanel = getCouplePanel(moveMethodLabel, moveMethodJComboBox);

            JComboBox<BoardGUI.HintStyle.Move> moveJComboBox = new JComboBox<>(BoardGUI.HintStyle.Move.values());
            moveJComboBox.setSelectedItem(BoardGUI.HintStyle.Move.DOT);
            JLabel moveLabel = new JLabel("Move Style: ");
            JPanel movePanel = getCouplePanel(moveLabel, moveJComboBox);

            JComboBox<BoardGUI.HintStyle.Capture> captureJComboBox = new JComboBox<>(BoardGUI.HintStyle.Capture.values());
            captureJComboBox.setSelectedItem(BoardGUI.HintStyle.Capture.RING);
            JLabel captureLabel = new JLabel("Capture Style: ");
            JPanel capturePanel = getCouplePanel(captureLabel, captureJComboBox);

            JComboBox<BoardGUI.CoordinateDisplayType> coordinateDisplayTypeJComboBox = new JComboBox<>(BoardGUI.CoordinateDisplayType.values());
            coordinateDisplayTypeJComboBox.setSelectedItem(BoardGUI.CoordinateDisplayType.INSIDE);
            JLabel cdtLabel = new JLabel("Coordinate Display: ");
            JPanel cdtPanel = getCouplePanel(cdtLabel, coordinateDisplayTypeJComboBox);

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
            JPanel inner = getCouplePanel(l, radioButton);

            JCheckBox online = new JCheckBox();
            online.setSelected(true);
            JLabel onlineLabel = new JLabel("Online mode: ");
            JPanel onlinePanel = getCouplePanel(onlineLabel, online);

            SpinnerModel model = new SpinnerNumberModel(100, 32, 150, 1);
            JSpinner spinner = new JSpinner(model);
            JLabel label = new JLabel("Piece Size (px): ");
            JPanel inner1 = getCouplePanel(label, spinner);

            SpinnerModel model1 = new SpinnerNumberModel(8, 4, 26, 1);
            JSpinner spinner1 = new JSpinner(model1);
            JLabel label1 = new JLabel("Board Size: ");
            JPanel inner2 = getCouplePanel(label1, spinner1);

            JComboBox<BoardGUI.PieceDesign> pieceDesignJComboBox = new JComboBox<>(BoardGUI.PieceDesign.values());
            pieceDesignJComboBox.setSelectedItem(BoardGUI.PieceDesign.NEO);
            JLabel pieceDesignLabel = new JLabel("Piece Theme: ");
            JPanel pieceDesignPanel = getCouplePanel(pieceDesignLabel, pieceDesignJComboBox);

            JComboBox<BoardGUI.Colours> boardThemeJComboBox = new JComboBox<>(BoardGUI.Colours.values());
            boardThemeJComboBox.setSelectedItem(BoardGUI.Colours.GREEN);
            JLabel boardThemeLabel = new JLabel("Board Theme: ");
            JPanel boardThemePanel = getCouplePanel(boardThemeLabel, boardThemeJComboBox);

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
}
