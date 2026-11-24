package rentalkendaraan.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Kelas helper utilitas terpusat untuk tema desain Modern Green and White.
 * Menyediakan palet warna, tipografi, dan metode styling dasar aplikasi Jaya Rent.
 */
public final class UITheme {
    // Private constructor untuk mencegah instansiasi
    private UITheme() {}

    // --- PALET WARNA RESMI (GREEN & WHITE THEME) ---
    public static final Color DARK_GREEN = new Color(0x14, 0x53, 0x2D);       // #14532D (Primary Brand / Header)
    public static final Color PRIMARY_GREEN = new Color(0x16, 0xA3, 0x4A);    // #16A34A (Action Buttons / Accent)
    public static final Color SECONDARY_GREEN = new Color(0x15, 0x80, 0x3D);  // #15803D (Hover / Sub-accent)
    public static final Color SOFT_GREEN = new Color(0xF0, 0xFD, 0xF4);       // #F0FDF4 (Card Backgrounds / Striping)
    public static final Color LIGHT_GREEN = new Color(0xDC, 0xFC, 0xE7);      // #DCFCE7 (Badges / Highlights)
    public static final Color WHITE = new Color(0xFF, 0xFF, 0xFF);            // #FFFFFF (Main Surface)
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);           // #E2E8F0 (Subtle Border)
    public static final Color GREEN_BORDER = new Color(0xBB, 0xF7, 0xD0);     // #BBF7D0 (Green Card Border)
    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);           // #DC2626 (Destructive / Warning)
    public static final Color DANGER_HOVER = new Color(0xB9, 0x1C, 0x1C);     // #B91C1C (Danger Button Hover)
    public static final Color WARNING = new Color(180, 83, 9);                 // #B45309 (Warning Text / Amber-700)
    public static final Color WARNING_BG = new Color(254, 243, 199);           // #FEF3C7 (Warning Background / Amber-100)
    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);     // #0F172A (Heading & Body Text)
    public static final Color TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);   // #475569 (Labels & Subtitles)
    public static final Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);       // #94A3B8 (Placeholders & Hints)

    // --- TIPOGRAFI STANDAR (SEGOE UI) ---
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);

    // --- HELPER BORDER ---
    public static Border createFieldBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    public static Border createGreenFieldBorder() {
        return new CompoundBorder(
                new LineBorder(GREEN_BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(14, 14, 14, 14)
        );
    }

    // --- HELPER STYLING TOMBOL MODERN (SOLID & OUTLINE DENGAN RADIUS) ---

    public enum ButtonVariant {
        PRIMARY,
        OUTLINE_GREEN,
        DANGER,
        NEUTRAL
    }

    public static class ModernButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final ButtonVariant variant;
        private final int arc;

        public ModernButtonUI(ButtonVariant variant) {
            this(variant, 8);
        }

        public ModernButtonUI(ButtonVariant variant, int arc) {
            this.variant = variant;
            this.arc = arc;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setRolloverEnabled(true);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = b.getWidth();
            int height = b.getHeight();

            Color bgColor;
            Color borderColor = null;
            Color textColor;

            boolean enabled = b.isEnabled();
            boolean rollover = model.isRollover();
            boolean pressed = model.isArmed() && model.isPressed();

            switch (variant) {
                case PRIMARY:
                    if (!enabled) {
                        bgColor = new Color(0xDC, 0xFC, 0xE7); // Soft light green (#DCFCE7)
                        borderColor = new Color(0xBB, 0xF7, 0xD0);
                        textColor = new Color(0x15, 0x80, 0x3D, 180); // Jelas terbaca tetapi muted
                    } else if (pressed) {
                        bgColor = DARK_GREEN;
                        borderColor = DARK_GREEN;
                        textColor = WHITE;
                    } else if (rollover) {
                        bgColor = SECONDARY_GREEN;
                        borderColor = SECONDARY_GREEN;
                        textColor = WHITE;
                    } else {
                        bgColor = PRIMARY_GREEN;
                        borderColor = PRIMARY_GREEN;
                        textColor = WHITE;
                    }
                    break;

                case OUTLINE_GREEN:
                    if (!enabled) {
                        bgColor = new Color(0xF8, 0xFA, 0xFC);
                        borderColor = new Color(0xE2, 0xE8, 0xF0);
                        textColor = new Color(0x94, 0xA3, 0xB8);
                    } else if (pressed) {
                        bgColor = new Color(0xDC, 0xFC, 0xE7);
                        borderColor = DARK_GREEN;
                        textColor = DARK_GREEN;
                    } else if (rollover) {
                        bgColor = SOFT_GREEN;
                        borderColor = SECONDARY_GREEN;
                        textColor = DARK_GREEN;
                    } else {
                        bgColor = WHITE;
                        borderColor = PRIMARY_GREEN;
                        textColor = DARK_GREEN;
                    }
                    break;

                case DANGER:
                    if (!enabled) {
                        bgColor = new Color(0xF1, 0xF5, 0xF9);
                        borderColor = new Color(0xE2, 0xE8, 0xF0);
                        textColor = new Color(0x94, 0xA3, 0xB8);
                    } else if (pressed) {
                        bgColor = new Color(0x99, 0x1B, 0x1B);
                        borderColor = new Color(0x99, 0x1B, 0x1B);
                        textColor = WHITE;
                    } else if (rollover) {
                        bgColor = DANGER_HOVER;
                        borderColor = DANGER_HOVER;
                        textColor = WHITE;
                    } else {
                        bgColor = DANGER;
                        borderColor = DANGER;
                        textColor = WHITE;
                    }
                    break;

                case NEUTRAL:
                default:
                    if (!enabled) {
                        bgColor = new Color(0xF8, 0xFA, 0xFC);
                        borderColor = new Color(0xE2, 0xE8, 0xF0);
                        textColor = new Color(0x94, 0xA3, 0xB8);
                    } else if (pressed) {
                        bgColor = LIGHT_GREEN;
                        borderColor = GREEN_BORDER;
                        textColor = DARK_GREEN;
                    } else if (rollover) {
                        bgColor = SOFT_GREEN;
                        borderColor = GREEN_BORDER;
                        textColor = DARK_GREEN;
                    } else {
                        bgColor = WHITE;
                        borderColor = BORDER;
                        textColor = TEXT_PRIMARY;
                    }
                    break;
            }

            // Gambar latar belakang tombol
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            // Gambar border
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
            }

            // Gambar teks di tengah
            Font font = b.getFont();
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics(font);
            String text = b.getText();
            if (text != null && !text.isEmpty()) {
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent() - fm.getDescent();
                int x = (width - textWidth) / 2;
                int y = (height + textHeight) / 2;

                g2.setColor(textColor);
                g2.drawString(text, x, y);
            }

            g2.dispose();
        }
    }

    public static void stylePrimaryButton(JButton button) {
        button.setFont(FONT_SMALL_BOLD);
        button.setBackground(PRIMARY_GREEN);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new ModernButtonUI(ButtonVariant.PRIMARY, 8));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(SECONDARY_GREEN);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PRIMARY_GREEN);
                }
            }
        });
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFont(FONT_SMALL_BOLD);
        button.setBackground(WHITE);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(8, 16, 8, 16)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new ModernButtonUI(ButtonVariant.NEUTRAL, 8));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(SOFT_GREEN);
                    button.setForeground(DARK_GREEN);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(WHITE);
                    button.setForeground(TEXT_PRIMARY);
                }
            }
        });
    }

    public static void styleOutlineSuccessButton(JButton button) {
        button.setFont(FONT_SMALL_BOLD);
        button.setBackground(SECONDARY_GREEN);
        button.setForeground(DARK_GREEN);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(new LineBorder(PRIMARY_GREEN, 1), new EmptyBorder(8, 14, 8, 14)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new ModernButtonUI(ButtonVariant.OUTLINE_GREEN, 8));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(DARK_GREEN);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(SECONDARY_GREEN);
                }
            }
        });
    }

    public static void styleDangerButton(JButton button) {
        button.setFont(FONT_SMALL_BOLD);
        button.setBackground(DANGER);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new ModernButtonUI(ButtonVariant.DANGER, 8));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(DANGER_HOVER);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(DANGER);
                }
            }
        });
    }

    public static void styleNeutralButton(JButton button) {
        button.setFont(FONT_SMALL_BOLD);
        button.setBackground(WHITE);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(8, 14, 8, 14)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new ModernButtonUI(ButtonVariant.NEUTRAL, 8));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(SOFT_GREEN);
                    button.setForeground(DARK_GREEN);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(WHITE);
                    button.setForeground(TEXT_PRIMARY);
                }
            }
        });
    }
}
