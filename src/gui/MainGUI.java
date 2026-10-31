package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Start window: pick a branch. Uses all the required listeners:
 * ActionListener (buttons), MouseListener (card hover and click),
 * MouseMotionListener (drag to reveal today's offer).
 */
public class MainGUI {

	private JFrame frame;
	private PromoBox promo;
	final BranchCard[] cards = new BranchCard[3];

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainGUI().show();
			}
		});
	}

	public void show() {
		frame = new JFrame("Restaurant - Choose a branch");
		frame.setContentPane(buildContent());
		frame.setSize(820, 560);
		frame.setMinimumSize(new Dimension(760, 520));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	JPanel buildContent() {
		JPanel root = new JPanel(new BorderLayout(0, 28));
		root.setBackground(Theme.BG);
		root.setBorder(BorderFactory.createEmptyBorder(40, 44, 36, 44));

		// ---- header
		JPanel header = Theme.clear(null);
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.add(center(Theme.label("SPAIN  ·  ITALY  ·  UK", Theme.sans(Font.BOLD, 12f), Theme.ACCENT)));
		header.add(Box.createVerticalStrut(8));
		header.add(center(Theme.label("Welcome to our restaurant", Theme.serif(Font.BOLD, 34f), Theme.TEXT)));
		header.add(Box.createVerticalStrut(8));
		header.add(center(Theme.label("Choose a branch to see its menu, top-rated dishes and prices.",
				Theme.sans(Font.PLAIN, 15f), Theme.MUTED)));
		root.add(header, BorderLayout.NORTH);

		// ---- branch cards
		JPanel row = Theme.clear(new GridLayout(1, 3, 20, 0));
		MenuGUI[] branches = { new SpainMenuGUI(), new ItalyMenuGUI(), new UKMenuGUI() };
		for (int i = 0; i < branches.length; i++) {
			cards[i] = new BranchCard(branches[i]);
			row.add(cards[i]);
		}
		root.add(row, BorderLayout.CENTER);

		// ---- drag-to-reveal promo
		promo = new PromoBox();
		root.add(promo, BorderLayout.SOUTH);

		// MouseMotionListener on every component, so a drag anywhere reveals the offer
		addMotionListener(root, new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				promo.reveal();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				promo.nudge(); // the hint pulses gently while the mouse moves
			}
		});
		return root;
	}

	private static void addMotionListener(Component c, MouseMotionListener l) {
		c.addMouseMotionListener(l);
		if (c instanceof Container) {
			for (Component child : ((Container) c).getComponents()) {
				addMotionListener(child, l);
			}
		}
	}

	private static JComponent center(JComponent c) {
		c.setAlignmentX(Component.CENTER_ALIGNMENT);
		return c;
	}

	// ======================================================== branch card
	/** One card per branch. MouseListener: hover highlight, click to open. */
	class BranchCard extends Theme.RoundedPanel {
		private static final long serialVersionUID = 1L;
		private final MenuGUI menu;

		BranchCard(MenuGUI menu) {
			super(new BorderLayout(), Theme.SURFACE, Theme.BORDER, 20);
			this.menu = menu;
			setBorder(BorderFactory.createEmptyBorder(30, 22, 22, 22));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			int top = menu.sortedDishes()[0];
			int cheapest = Integer.MAX_VALUE;
			for (int i = 0; i < 4; i++) {
				cheapest = Math.min(cheapest, menu.getPrices()[i]);
			}

			JPanel text = Theme.clear(null);
			text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
			text.add(left(Theme.label(menu.getTagline().toUpperCase(), Theme.sans(Font.BOLD, 11f), menu.getAccent())));
			text.add(Box.createVerticalStrut(6));
			text.add(left(Theme.label(menu.getBranch(), Theme.serif(Font.BOLD, 28f), Theme.TEXT)));
			text.add(Box.createVerticalStrut(14));
			text.add(left(Theme.label("Top rated", Theme.sans(Font.PLAIN, 12.5f), Theme.MUTED)));
			text.add(Box.createVerticalStrut(2));
			text.add(left(Theme.label(menu.getMenuItems()[top] + "  ·  " + menu.getRatings()[top] + "/10",
					Theme.sans(Font.BOLD, 14f), Theme.TEXT)));
			text.add(Box.createVerticalStrut(10));
			text.add(left(Theme.label("Mains from", Theme.sans(Font.PLAIN, 12.5f), Theme.MUTED)));
			text.add(Box.createVerticalStrut(2));
			text.add(left(Theme.label(cheapest + " " + MenuGUI.CURRENCY, Theme.sans(Font.BOLD, 14f), Theme.TEXT)));
			add(text, BorderLayout.NORTH);

			Theme.PillButton open = new Theme.PillButton("View menu", true, menu.getAccent());
			open.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					menu.showMenu();
				}
			});
			add(open, BorderLayout.SOUTH);

			addMouseListener(new MouseListener() {
				@Override
				public void mouseEntered(MouseEvent e) {
					setHover(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// moving onto the button inside the card is still "inside"
					if (!contains(e.getPoint())) {
						setHover(false);
					}
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					menu.showMenu();
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}
			});
		}

		void setHover(boolean on) {
			setColors(on ? Theme.mix(menu.getAccent(), Theme.SURFACE, 0.94) : Theme.SURFACE,
					on ? menu.getAccent() : Theme.BORDER);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// flag stripe, clipped to the card's rounded top
			Graphics2D g2 = (Graphics2D) g.create();
			Theme.smooth(g2);
			g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
			java.awt.Color[] flag = menu.getFlag();
			int w = getWidth() / flag.length;
			for (int i = 0; i < flag.length; i++) {
				g2.setColor(flag[i]);
				g2.fillRect(i * w, 0, i == flag.length - 1 ? getWidth() - i * w : w, 8);
			}
			g2.setClip(null);
			g2.setColor(stroke);
			g2.setStroke(new BasicStroke(1.2f));
			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
			g2.dispose();
		}
	}

	private static JComponent left(JComponent c) {
		c.setAlignmentX(Component.LEFT_ALIGNMENT);
		return c;
	}

	// ========================================================== promo box
	/** Dashed hint that turns into the offer banner once the mouse is dragged. */
	static class PromoBox extends JPanel {
		private static final long serialVersionUID = 1L;
		private boolean revealed;
		private int pulse;
		private final JLabel label;

		PromoBox() {
			super(new BorderLayout());
			setOpaque(false);
			setPreferredSize(new Dimension(10, 62));
			label = Theme.label("Click and drag anywhere to reveal today's offer", Theme.sans(Font.BOLD, 14f),
					Theme.MUTED);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			add(label, BorderLayout.CENTER);
		}

		void reveal() {
			if (!revealed) {
				revealed = true;
				label.setText("Today only: free drink with any order, at all branches");
				label.setForeground(java.awt.Color.WHITE);
				label.setFont(Theme.sans(Font.BOLD, 16f));
				repaint();
			}
		}

		void nudge() {
			if (!revealed) {
				pulse = (pulse + 1) % 40;
				label.setForeground(Theme.mix(Theme.MUTED, Theme.ACCENT, Math.abs(20 - pulse) / 20.0));
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			Theme.smooth(g2);
			int w = getWidth() - 1, h = getHeight() - 1;
			if (revealed) {
				g2.setColor(Theme.ACCENT);
				g2.fillRoundRect(0, 0, w, h, 18, 18);
			} else {
				g2.setColor(Theme.SURFACE);
				g2.fillRoundRect(0, 0, w, h, 18, 18);
				g2.setColor(Theme.BORDER.darker());
				g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
						new float[] { 6f, 6f }, 0f));
				g2.drawRoundRect(0, 0, w, h, 18, 18);
			}
			g2.dispose();
		}
	}
}
