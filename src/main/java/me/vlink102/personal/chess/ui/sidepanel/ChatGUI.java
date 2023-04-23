package me.vlink102.personal.chess.ui.sidepanel;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.PlaceholderTextField;
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

    public record ChatEntry(long time, String name, String fullMessage) {}

    private final List<ChatEntry> chatHistory;
    private final ChatGUIProfile profile;

    private Font chatFont;

    public Font getChatFont() {
        return chatFont;
    }

    public record ChatGUIProfile(Image opponentProfile, Image selfProfile, String opponentName, String selfName, int opponentElo, int selfElo) {}

    public ChatGUI(BoardGUI boardGUI, Chess chess, ChatGUIProfile profile) {
        this.boardGUI = boardGUI;
        this.chess = chess;
        this.chatHistory = new ArrayList<>();
        this.profile = profile;

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

    public void addMessage(String message) {
        ChatEntry entry = new ChatEntry(System.currentTimeMillis(), profile.selfName, message);
        chatHistory.add(entry);
        JLabel label = new JLabel("<html><font size='-2' color='gray'>[" + getTime(entry.time) + "]</font>  <font color='yellow'><b>" + entry.name + "</b></font>  <font color='white'>" + entry.fullMessage + "</font></html>");

        label.setFont(chatFont);
        add(label);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), chatHistory.size() * 15));
        revalidate();

    }

    public static class InputChat extends PlaceholderTextField {
        public InputChat(int pCols, ChatGUI chatGUI, Chess chess) {
            super(pCols);
            addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String text = getText();
                    if (text.isEmpty() || text.isBlank()) {
                        return;
                    }
                    chatGUI.addMessage(getText());
                    JScrollBar bar = chess.getChatPane().getVerticalScrollBar();
                    bar.setValue(bar.getMaximum());
                    setText(null);
                }
            });
            setVisible(true);
            setOpaque(true);
            validate();
        }
    }

    public static String getTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }
}
