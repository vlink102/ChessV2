package me.vlink102.personal.chess.internal;

import com.neovisionaries.i18n.CountryCode;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class IconListRenderer extends DefaultListCellRenderer {
    private final Map<CountryCode, Image> icons;

    public IconListRenderer(Map<CountryCode, Image> icons){
        this.icons = icons;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Image icon = icons.get((CountryCode) value);
        if (value.equals(CountryCode.UNDEFINED)) {
            label.setIcon(null);
            label.setText("Nowhere");
        } else {
            if (icon != null) {
                label.setIcon(new ImageIcon(icon.getScaledInstance(15, -1, Image.SCALE_SMOOTH)));
                label.setText("  " + ((CountryCode) value).getName() + " (" + value + ")");
            } else {
                label.setText("         " + ((CountryCode) value).getName() + " (" + value + ")");
            }
        }
        label.setHorizontalAlignment(JLabel.LEFT);
        return label;
    }
}