package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Colors, fonts and small custom-painted components shared by every window.
 */
public final class Theme {

	private Theme() {
	}

	// ---------------------------------------------------------------- colors
	public static final Color BG = new Color(0xF7F3EC); // warm off-white
	public static final Color SURFACE = Color.WHITE;
	public static final Color BORDER = new Color(0xE6DFD3);
	public static final Color TEXT = new Color(0x2B2622);
	public static final Color MUTED = new Color(0x857B70);
	public static final Color ACCENT = new Color(0xC2502E); // terracotta
	public static final Color ACCENT_SOFT = new Color(0xF6E3DB);
	public static final Color SUCCESS = new Color(0x3F7D4E);
	public static final Color STAR = new Color(0xE0A526);

	// ----------------------------------------------------------------- fonts
	private static final List<String> FAMILIES = Arrays
			.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

	private static String pick(String fallback, String... names) {
		for (String n : names) {
			if (FAMILIES.contains(n)) {
				return n;
			}
		}
		return fallback;
	}

	public static final String SANS = pick(Font.SANS_SERIF, "Segoe UI", "Inter", "Helvetica Neue", "Arial");
	public static final String SERIF = pick(Font.SERIF, "Georgia", "DejaVu Serif", "Times New Roman");

	public static Font sans(int style, float size) {
		return new Font(SANS, style, 1).deriveFont(size);
	}

	public static Font serif(int style, float size) {
		return new Font(SERIF, style, 1).deriveFont(size);
	}

	// ------------------------------------------------------------- utilities
	public static JLabel label(String text, Font font, Color color) {
		JLabel l = new JLabel(text);
		l.setFont(font);
		l.setForeground(color);
		l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3)); // room for the last glyph
		return l;
	}

	/** Stops BoxLayout from stretching a component taller than it needs. */
	public static <T extends JComponent> T fixHeight(T c) {
		c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
		return c;
	}

	/** A slim, rounded scrollbar instead of the default one. */
	public static void slimScrollBar(javax.swing.JScrollBar bar) {
		bar.setPreferredSize(new Dimension(8, 8));
		bar.setOpaque(false);
		bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
			@Override
			protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
			}

			@Override
			protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle r) {
				Graphics2D g2 = (Graphics2D) g.create();
				smooth(g2);
				g2.setColor(isThumbRollover() ? MUTED : BORDER.darker());
				g2.fillRoundRect(r.x + 1, r.y, r.width - 2, r.height, r.width - 2, r.width - 2);
				g2.dispose();
			}

			@Override
			protected JButton createDecreaseButton(int o) {
				return zero();
			}

			@Override
			protected JButton createIncreaseButton(int o) {
				return zero();
			}

			private JButton zero() {
				JButton b = new JButton();
				b.setPreferredSize(new Dimension(0, 0));
				return b;
			}
		});
	}

	public static JPanel clear(LayoutManager layout) {
		JPanel p = new JPanel(layout);
		p.setOpaque(false);
		return p;
	}

	static void smooth(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}

	// ------------------------------------------------------- rounded panel
	/** A panel with rounded corners, a fill and an optional border. */
	public static class RoundedPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		protected Color fill;
		protected Color stroke;
		protected int arc;

		public RoundedPanel(LayoutManager layout, Color fill, Color stroke, int arc) {
			super(layout);
			this.fill = fill;
			this.stroke = stroke;
			this.arc = arc;
			setOpaque(false);
		}

		public void setColors(Color fill, Color stroke) {
			this.fill = fill;
			this.stroke = stroke;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			smooth(g2);
			g2.setColor(fill);
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
			if (stroke != null) {
				g2.setColor(stroke);
				g2.setStroke(new BasicStroke(1.2f));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
			}
			g2.dispose();
			super.paintComponent(g);
		}
	}

	// --------------------------------------------------------- pill button
	/** A flat, rounded button: filled (primary) or outlined (secondary). */
	public static class PillButton extends JButton {
		private static final long serialVersionUID = 1L;
		private final boolean primary;
		private Color accent;

		public PillButton(String text, boolean primary, Color accent) {
			super(text);
			this.primary = primary;
			this.accent = accent;
			setFont(sans(Font.BOLD, 13f));
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			setRolloverEnabled(true);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			smooth(g2);
			int w = getWidth(), h = getHeight();
			boolean over = getModel().isRollover(), down = getModel().isPressed();
			if (!isEnabled()) {
				g2.setColor(BORDER);
				g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
				g2.setColor(MUTED);
			} else if (primary) {
				Color c = down ? accent.darker().darker() : over ? accent.darker() : accent;
				g2.setColor(c);
				g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
				g2.setColor(Color.WHITE);
			} else {
				g2.setColor(over || down ? mix(accent, Color.WHITE, 0.88) : SURFACE);
				g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
				g2.setColor(over ? accent : BORDER);
				g2.setStroke(new BasicStroke(1.2f));
				g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
				g2.setColor(accent);
			}
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			String t = getText();
			g2.drawString(t, (w - fm.stringWidth(t)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
			g2.dispose();
		}
	}

	// ----------------------------------------------------------- rating bar
	/** A thin horizontal bar showing a score out of 10. */
	public static class RatingBar extends JComponent {
		private static final long serialVersionUID = 1L;
		private final int score;
		private final Color color;

		public RatingBar(int score, Color color) {
			this.score = score;
			this.color = color;
			setPreferredSize(new Dimension(120, 6));
			setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			smooth(g2);
			int w = getWidth(), h = getHeight();
			g2.setColor(BORDER);
			g2.fillRoundRect(0, 0, w, h, h, h);
			g2.setColor(color);
			g2.fillRoundRect(0, 0, Math.max(h, w * score / 10), h, h, h);
			g2.dispose();
		}
	}

	// ---------------------------------------------------------------- stars
	/** Five stars, the first {@code value} of them filled. */
	public static class Stars extends JComponent {
		private static final long serialVersionUID = 1L;
		private int value;

		public Stars() {
			setPreferredSize(new Dimension(5 * 22, 20));
			setMaximumSize(getPreferredSize());
		}

		public void setValue(int value) {
			this.value = Math.max(0, Math.min(5, value));
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			smooth(g2);
			for (int i = 0; i < 5; i++) {
				Path2D star = star(i * 22 + 9, 10, 9, 4);
				if (i < value) {
					g2.setColor(STAR);
					g2.fill(star);
				} else {
					g2.setColor(BORDER);
					g2.setStroke(new BasicStroke(1.3f));
					g2.draw(star);
				}
			}
			g2.dispose();
		}

		private static Path2D star(double cx, double cy, double outer, double inner) {
			Path2D p = new Path2D.Double();
			for (int i = 0; i < 10; i++) {
				double r = i % 2 == 0 ? outer : inner;
				double a = -Math.PI / 2 + i * Math.PI / 5;
				double x = cx + r * Math.cos(a), y = cy + r * Math.sin(a);
				if (i == 0) {
					p.moveTo(x, y);
				} else {
					p.lineTo(x, y);
				}
			}
			p.closePath();
			return p;
		}
	}

	// ------------------------------------------------------------- helpers
	public static Color mix(Color a, Color b, double t) {
		return new Color((int) (a.getRed() * (1 - t) + b.getRed() * t),
				(int) (a.getGreen() * (1 - t) + b.getGreen() * t),
				(int) (a.getBlue() * (1 - t) + b.getBlue() * t));
	}
}
