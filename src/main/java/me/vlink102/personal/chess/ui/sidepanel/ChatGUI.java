package me.vlink102.personal.chess.ui.sidepanel;

import com.neovisionaries.i18n.CountryCode;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.PlaceholderTextField;
import me.vlink102.personal.chess.internal.networking.CommunicationHandler;
import me.vlink102.personal.chess.internal.networking.DataThread;
import me.vlink102.personal.chess.internal.networking.packets.game.social.ChatMessage;
import me.vlink102.personal.chess.ui.CoordinateGUI;
import org.w3c.dom.css.Rect;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ChatGUI extends JPanel {
    private final BoardGUI boardGUI;
    private final Chess chess;

    public record ChatEntry(long time, String name, String fullMessage, String profanityFilter, boolean local) {}

    private final List<ChatEntry> chatHistory;
    private final ChatGUIProfile profile;

    private Font chatFont;

    public Font getChatFont() {
        return chatFont;
    }

    /**
     * @param opponentProfile
     * @param selfProfile
     * @param opponentName
     * @param selfName
     * @param opponentElo
     * @param selfElo
     */
    public record ChatGUIProfile(Image opponentProfile, Image selfProfile, String opponentName, String selfName, int opponentElo, int selfElo, CountryCode opponentCode, CountryCode selfCode) {}

    public ChatGUI(BoardGUI boardGUI, Chess chess, ChatGUIProfile profile) {
        this.boardGUI = boardGUI;
        this.chess = chess;
        this.chatHistory = new ArrayList<>();
        this.profile = profile;

        setOpaque(true);
        setBackground(Chess.menuScheme.darkerBackground());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(3, 5, 3, 5));
        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), (int) getBounds().getSize().getHeight()));
        updateFonts();
    }

    public void updateFonts() {
        this.chatFont = chess.getGoogle().deriveFont((float) 12);
    }


    public List<ChatEntry> getChatHistory() {
        return chatHistory;
    }

    private JTextPane createTextPane(String text) {
        JTextPane pane = new JTextPane();
        pane.setText(text);
        StyledDocument document = pane.getStyledDocument();
        SimpleAttributeSet left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
        document.setParagraphAttributes(0, document.getLength(), left, false);
        pane.setFont(chatFont);
        return pane;
    }

    public void clearChat() {
        removeAll();
        chatHistory.clear();
        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), 10));
        revalidate();
        EventQueue.invokeLater(() -> {
            JScrollBar b = chess.getChatPane().getVerticalScrollBar();
            b.setValue(b.getMaximum());
        });
    }

    public void updateChat() {
        removeAll();
        if (boardGUI.isChatFilterEnabled()) {
            for (ChatEntry entry : chatHistory) {
                JLabel label = new JLabel("<html><font size='-2' color='gray'>[" + getTime(entry.time) + "]</font>  <font color='" + (entry.local ? "yellow" : "gray") + "'><b>" + entry.name + "</b></font>  <font color='white'>" + entry.profanityFilter + "</font></html>");

                label.setFont(chatFont);
                add(label);
            }
        } else {
            for (ChatEntry entry : chatHistory) {
                JLabel label = new JLabel("<html><font size='-2' color='gray'>[" + getTime(entry.time) + "]</font>  <font color='" + (entry.local ? "yellow" : "gray") + "'><b>" + entry.name + "</b></font>  <font color='white'>" + entry.fullMessage + "</font></html>");

                label.setFont(chatFont);
                add(label);
            }
        }
        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), chatHistory.size() * 15 + 10));
        revalidate();
        EventQueue.invokeLater(() -> {
            JScrollBar b = chess.getChatPane().getVerticalScrollBar();
            b.setValue(b.getMaximum());
        });
    }

    public void addMessage(String message, boolean isLocal) {
        ChatEntry entry = new ChatEntry(System.currentTimeMillis(), (isLocal ? profile.selfName : profile.opponentName), message, boardGUI.getProfanityFilter().filterBadWords(message), isLocal);
        chatHistory.add(entry);
        JLabel label = new JLabel("<html><font size='-2' color='gray'>[" + getTime(entry.time) + "]</font>  <font color='" + (isLocal ? "yellow" : "gray") + "'><b>" + entry.name + "</b></font>  <font color='white'>" + (boardGUI.isChatFilterEnabled() ? entry.profanityFilter : entry.fullMessage) + "</font></html>");

        label.setFont(chatFont);
        add(label);

        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), chatHistory.size() * 15 + 10));

        revalidate();

        EventQueue.invokeLater(() -> {
            JScrollBar b = chess.getChatPane().getVerticalScrollBar();
            b.setValue(b.getMaximum());
            if (isLocal) {
                CommunicationHandler.thread.sendPacket(new ChatMessage(ChessMenu.IDENTIFIER, message, boardGUI.getGameID()));
            }
        });
    }

    public static class InputChat extends PlaceholderTextField {
        public InputChat(int pCols, ChatGUI chatGUI) {
            super(pCols);
            addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String text = getText();
                    if (text.isEmpty() || text.isBlank()) {
                        return;
                    }
                    chatGUI.addMessage(getText(), true);
                    setText(null);
                }
            });
            setVisible(true);
            setOpaque(true);
            requestFocusInWindow();
            validate();
        }
    }

    public static String getTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }

    public ChatGUIProfile getProfile() {
        return profile;
    }
}
