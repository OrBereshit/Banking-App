import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * UI styling and component factory helper to maintain a clean, consistent Swing GUI.
 */
public class UIHelper {

    public static final Color COLOR_PRIMARY = new Color(30, 58, 138);   // Navy
    public static final Color COLOR_ACCENT = new Color(37, 99, 235);    // Royal Blue
    public static final Color COLOR_ACCENT_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);   // Green
    public static final Color COLOR_DANGER = new Color(220, 38, 38);    // Red
    public static final Color COLOR_GRAY_BTN = new Color(100, 116, 139); // Gray
    public static final Color COLOR_BG = new Color(248, 250, 252);      // Soft slate background
    public static final Color COLOR_CARD = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BALANCE = new Font("SansSerif", Font.BOLD, 26);

    public static JButton createPrimaryButton(String text) {
        return createButton(text, COLOR_ACCENT, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, COLOR_GRAY_BTN, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, COLOR_DANGER, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, COLOR_SUCCESS, Color.WHITE);
    }

    private static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 38));
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(COLOR_TEXT_MAIN);
        Border line = new LineBorder(COLOR_BORDER, 1);
        Border padding = new EmptyBorder(6, 10, 6, 10);
        tf.setBorder(new CompoundBorder(line, padding));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_REGULAR);
        pf.setForeground(COLOR_TEXT_MAIN);
        Border line = new LineBorder(COLOR_BORDER, 1);
        Border padding = new EmptyBorder(6, 10, 6, 10);
        pf.setBorder(new CompoundBorder(line, padding));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 36));
        return pf;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD);
        Border line = new LineBorder(COLOR_BORDER, 1);
        Border padding = new EmptyBorder(20, 24, 20, 24);
        card.setBorder(new CompoundBorder(line, padding));
        return card;
    }
}
