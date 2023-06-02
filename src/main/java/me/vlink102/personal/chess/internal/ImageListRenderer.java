package me.vlink102.personal.chess.internal;

import com.neovisionaries.i18n.CountryCode;
import me.vlink102.personal.chess.BoardGUI;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ImageListRenderer extends DefaultListCellRenderer {

    public ImageListRenderer() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setIcon(new ImageIcon(((BoardGUI.PromotionPiece) value).getImage()));
        label.setText("");
        return label;
    }
}
