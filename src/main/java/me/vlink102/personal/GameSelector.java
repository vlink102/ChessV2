package me.vlink102.personal;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.internal.networking.MySQLConnection;
import me.vlink102.personal.minesweeper.MSMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class GameSelector {
    private static Image imageIcon;
    private static Image chessIcon;
    private static Image msIcon;
    public static JFrame frame;
    public static MySQLConnection connection;
    private static final HashMap<Game, Menu> menuInstances = new HashMap<>();

    public enum Game {
        CHESS,
        MINESWEEPER
    }

    public static void initUI() {
        imageIcon = Move.getResource("/icon.png");
        chessIcon = Move.getResource("/play.png").getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        msIcon = Move.getResource("/minesweeper.png").getScaledInstance(100, 100, Image.SCALE_SMOOTH);

        LafManager.install(new DarculaTheme());
        LafManager.setDecorationsEnabled(true);

        frame = new JFrame("Game Selector");

        JPanel panel = new JPanel();
        panel.add(new MenuButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!menuInstances.containsKey(Game.CHESS)) {
                    ChessMenu chessMenu = new ChessMenu();
                    menuInstances.put(Game.CHESS, chessMenu);
                } else {
                    menuInstances.get(Game.CHESS).focus();
                }
            }
        }, chessIcon, 50, 50, 150, 150));
        panel.add(new MenuButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!menuInstances.containsKey(Game.MINESWEEPER)) {
                    MSMenu msMenu = new MSMenu();
                    menuInstances.put(Game.MINESWEEPER, msMenu);
                } else {
                    menuInstances.get(Game.MINESWEEPER).focus();
                }
            }
        }, msIcon, 200, 50, 150, 150));
        frame.add(panel);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(true);

        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());

        frame.setIconImage(imageIcon);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocus();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(GameSelector::initUI);
        connection = new MySQLConnection("ulucl02v8dm4l3qm", "bf5v9fiyfc6bqge4qrz1-mysql.services.clever-cloud.com", "bf5v9fiyfc6bqge4qrz1", 3306);
    }

    public static void closeMenuInstance(Game game) {
        menuInstances.remove(game);
    }


    public static Image getImageIcon() {
        return imageIcon;
    }


    public static class MenuButton extends JButton {
        public MenuButton(AbstractAction action, Image image, int x, int y, int w, int h) {
            setIcon(new ImageIcon(image));
            setLocation(x, y);
            setPreferredSize(new Dimension(w, h));
            setSize(getPreferredSize());

            addActionListener(action);
            setBorderPainted(false);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }
    }
}
