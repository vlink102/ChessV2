package me.vlink102.personal.chess.ui.sidepanel;

import com.neovisionaries.i18n.CountryCode;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.ChessMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.NoRouteToHostException;

public class ProfileGUI extends JPanel {
    private final BoardGUI boardGUI;
    private final Chess chess;
    public ProfileGUI(Chess chess, BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        this.chess = chess;
        updateFonts();
        setOpaque(true);
        setBackground(Chess.menuScheme.background());
        setLayout(new BorderLayout());
    }

    private Font name;
    private Font elo;

    public void updateFonts() {
        name = (chess.getBold().deriveFont((float) boardGUI.getPieceSize() / 6));
        elo = (chess.getDef().deriveFont((float) boardGUI.getPieceSize() / 6));
    }

    public void updateAll() {
        setPreferredSize(new Dimension((int) getBounds().getWidth(), (int) getBounds().getHeight()));
        updateProfiles();
        revalidate();
    }

    private void updateProfiles() {
        removeAll();
        int bruh = boardGUI.getPieceSize() / 2;
        ChatGUI.ChatGUIProfile profile = boardGUI.getChatGUI().getProfile();
        JPanel self = getProfilePanel(profile, false, bruh);
        BoardGUI.OpponentType type = boardGUI.getOpponentType();
        boolean e = (type.equals(BoardGUI.OpponentType.PLAYER) || type.equals(BoardGUI.OpponentType.COMPUTER) || type.equals(BoardGUI.OpponentType.RANDOM));
        JPanel opp = getProfilePanel(profile, e, bruh);
        self.setAlignmentX(LEFT_ALIGNMENT);
        opp.setAlignmentX(LEFT_ALIGNMENT);
        add(self, (boardGUI.selfAtBottom() ? BorderLayout.SOUTH : BorderLayout.NORTH));
        add(opp, (boardGUI.selfAtBottom() ? BorderLayout.NORTH : BorderLayout.SOUTH));
    }

    private JPanel getProfilePanel(ChatGUI.ChatGUIProfile profile, boolean isOpp, int bruh) {
        if (isOpp) {
            Image opponentProfile = profile.opponentProfile().getScaledInstance(bruh, bruh, Image.SCALE_SMOOTH);

            JPanel opponent = new JPanel();
            opponent.setOpaque(true);
            opponent.setBackground(Chess.menuScheme.background());
            opponent.setLayout(new BoxLayout(opponent, BoxLayout.X_AXIS));
            opponent.add(new JLabel(new ImageIcon(opponentProfile)));
            JLabel opponentName = new JLabel("   " + profile.opponentName() + "  ");
            opponentName.setOpaque(false);
            opponentName.setFont(name);
            opponent.add(opponentName);
            JLabel opponentLocation = new JLabel(new ImageIcon(ChessMenu.FLAGS.get(profile.opponentCode().equals(CountryCode.UNDEFINED) ? CountryCode.AQ : profile.opponentCode()).getScaledInstance(-1, boardGUI.getPieceSize() / 6, Image.SCALE_SMOOTH)));
            opponent.add(opponentLocation);
            JLabel opponentElo = new JLabel("  (" + profile.opponentElo() + ")");
            opponentElo.setOpaque(false);
            opponentElo.setFont(elo);
            opponent.add(opponentElo);
            if (boardGUI.getOpponentUUID() != null) {
                opponent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        ChessMenu.openOther(boardGUI.getOpponentUUID());
                    }
                });
            }
            return opponent;
        } else {
            Image selfProfile = profile.selfProfile().getScaledInstance(bruh, bruh, Image.SCALE_SMOOTH);
            JPanel self = new JPanel();
            self.setOpaque(true);
            self.setBackground(Chess.menuScheme.background());
            self.setLayout(new BoxLayout(self, BoxLayout.X_AXIS));
            self.add(new JLabel(new ImageIcon(selfProfile)));
            JLabel selfName = new JLabel("   " + profile.selfName() + "  ");
            selfName.setOpaque(false);
            selfName.setFont(name);
            self.add(selfName);
            JLabel selfLocation = new JLabel(new ImageIcon(ChessMenu.FLAGS.get(profile.selfCode().equals(CountryCode.UNDEFINED) ? CountryCode.AQ : profile.selfCode()).getScaledInstance(-1, boardGUI.getPieceSize() / 6, Image.SCALE_SMOOTH)));
            self.add(selfLocation);
            JLabel selfElo = new JLabel("  (" + profile.selfElo() + ")");
            selfElo.setOpaque(false);
            selfElo.setFont(elo);
            self.add(selfElo);
            self.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    ChessMenu.openOther(ChessMenu.IDENTIFIER);
                }
            });
            return self;
        }
    }
}
