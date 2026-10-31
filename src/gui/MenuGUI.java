package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Parent class for every branch menu. Subclasses only supply their data
 * (dishes, prices, ratings and colors); the window, sorting and ordering are
 * inherited from here.
 */
public class MenuGUI {

	static final int MAX_ITEMS = 20; // the customer can order up to 20 items
	static final String CURRENCY = "JD";

	// section boundaries in every branch's menu arrays
	private static final int[][] SECTIONS = { { 0, 4 }, { 4, 6 }, { 6, 9 }, { 9, 12 } };
	private static final String[] SECTION_NAMES = { "Main dishes", "More from the kitchen", "Drinks", "Sides" };
	private static final int DRINKS_START = 6, DRINKS_END = 9;

	private final String branch;
	private final String tagline;
	private final String[] menuItems;
	private final int[] prices;
	private final int[] ratings; // ratings of the main dishes (first 4 items)
	private final Color[] flag;
	private final Color accent;

	private final int[] count; // how many of each item is in the order
	private int customerRating;

	// order summary widgets, rebuilt whenever the order changes
	private JPanel orderLines;
	private JLabel totalLabel;
	private JLabel itemsLabel;
	private Theme.PillButton placeOrderBtn;
	private JFrame frame;

	public MenuGUI(String branch, String tagline, String[] menuItems, int[] prices, int[] ratings, Color[] flag,
			Color accent) {
		this.branch = branch;
		this.tagline = tagline;
		this.menuItems = menuItems;
		this.prices = prices;
		this.ratings = ratings;
		this.flag = flag;
		this.accent = accent;
		this.count = new int[menuItems.length];
	}

	public String getBranch() {
		return branch;
	}

	public String getTagline() {
		return tagline;
	}

	public String[] getMenuItems() {
		return menuItems;
	}

	public int[] getPrices() {
		return prices;
	}

	public int[] getRatings() {
		return ratings;
	}

	public Color[] getFlag() {
		return flag;
	}

	public Color getAccent() {
		return accent;
	}

	// ------------------------------------------------------ selection sort
	/**
	 * Sorts the main dishes by rating, highest first, using selection sort.
	 * Works on copies so the original menu order is never changed.
	 *
	 * @return the sorted indexes of the main dishes in {@code menuItems}
	 */
	public int[] sortedDishes() {
		int[] index = new int[ratings.length];
		int[] tempRatings = ratings.clone();
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		for (int i = 0; i < tempRatings.length - 1; i++) {
			int max = i;
			for (int j = i + 1; j < tempRatings.length; j++) {
				if (tempRatings[j] > tempRatings[max]) {
					max = j;
				}
			}
			int tempRate = tempRatings[i];
			tempRatings[i] = tempRatings[max];
			tempRatings[max] = tempRate;
			int tempIndex = index[i];
			index[i] = index[max];
			index[max] = tempIndex;
		}
		return index;
	}

	// ------------------------------------------------------------ the window
	public void showMenu() {
		frame = new JFrame(branch + " - Menu");
		frame.setContentPane(buildContent());
		frame.setSize(1080, 700);
		frame.setMinimumSize(new Dimension(900, 560));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	/** Builds the whole menu window. Separate from showMenu() so it can be previewed. */
	JPanel buildContent() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(Theme.BG);
		root.add(buildHeader(), BorderLayout.NORTH);

		JPanel body = Theme.clear(new BorderLayout(24, 0));
		body.setBorder(BorderFactory.createEmptyBorder(24, 32, 28, 32));
		body.add(buildTopRated(), BorderLayout.WEST);
		body.add(buildMenuList(), BorderLayout.CENTER);
		body.add(buildOrderPanel(), BorderLayout.EAST);
		root.add(body, BorderLayout.CENTER);
		return root;
	}

	private JComponent buildHeader() {
		JPanel header = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// flag stripe across the top
				int w = getWidth() / flag.length;
				for (int i = 0; i < flag.length; i++) {
					g.setColor(flag[i]);
					g.fillRect(i * w, 0, i == flag.length - 1 ? getWidth() - i * w : w, 6);
				}
				g.setColor(Theme.BORDER);
				g.fillRect(0, getHeight() - 1, getWidth(), 1);
			}
		};
		header.setBackground(Theme.SURFACE);
		header.setBorder(BorderFactory.createEmptyBorder(26, 32, 20, 32));

		JPanel titles = Theme.clear(null);
		titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
		titles.add(Theme.label(tagline.toUpperCase(), Theme.sans(Font.BOLD, 11.5f), accent));
		titles.add(Box.createVerticalStrut(4));
		titles.add(Theme.label(branch + " branch", Theme.serif(Font.BOLD, 30f), Theme.TEXT));
		header.add(titles, BorderLayout.WEST);

		Theme.RoundedPanel promo = new Theme.RoundedPanel(new FlowLayout(FlowLayout.CENTER, 0, 0), Theme.ACCENT_SOFT,
				null, 28);
		promo.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		promo.add(Theme.label("Today only: free drink with any order", Theme.sans(Font.BOLD, 13f), Theme.ACCENT));
		JPanel promoWrap = Theme.clear(new BorderLayout());
		promoWrap.add(promo, BorderLayout.SOUTH);
		header.add(promoWrap, BorderLayout.EAST);
		return header;
	}

	/** Left column: main dishes sorted by rating. */
	private JComponent buildTopRated() {
		Theme.RoundedPanel card = new Theme.RoundedPanel(null, Theme.SURFACE, Theme.BORDER, 18);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		card.setPreferredSize(new Dimension(230, 10));

		card.add(left(Theme.label("Top rated", Theme.serif(Font.BOLD, 19f), Theme.TEXT)));
		card.add(Box.createVerticalStrut(2));
		card.add(left(Theme.label("Main dishes, sorted by rating", Theme.sans(Font.PLAIN, 12.5f), Theme.MUTED)));
		card.add(Box.createVerticalStrut(16));

		int[] order = sortedDishes();
		for (int rank = 0; rank < order.length; rank++) {
			int i = order[rank];
			JPanel row = Theme.clear(new BorderLayout(10, 0));
			row.add(Theme.label(String.valueOf(rank + 1), Theme.serif(Font.BOLD, 20f),
					rank == 0 ? accent : Theme.BORDER.darker()), BorderLayout.WEST);

			JPanel info = Theme.clear(null);
			info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
			JPanel nameRow = Theme.clear(new BorderLayout());
			nameRow.add(Theme.label(menuItems[i], Theme.sans(Font.BOLD, 13.5f), Theme.TEXT), BorderLayout.WEST);
			nameRow.add(Theme.label(ratings[i] + "/10", Theme.sans(Font.PLAIN, 12.5f), Theme.MUTED), BorderLayout.EAST);
			info.add(left(nameRow));
			info.add(Box.createVerticalStrut(6));
			info.add(left(new Theme.RatingBar(ratings[i], rank == 0 ? accent : Theme.MUTED)));
			row.add(info, BorderLayout.CENTER);

			card.add(left(Theme.fixHeight(row)));
			card.add(Box.createVerticalStrut(18));
		}
		card.add(Box.createVerticalGlue());
		return card;
	}

	/** Center column: the full menu, grouped into sections. */
	private JComponent buildMenuList() {
		JPanel list = Theme.clear(null);
		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

		for (int s = 0; s < SECTIONS.length; s++) {
			list.add(left(Theme.label(SECTION_NAMES[s].toUpperCase(), Theme.sans(Font.BOLD, 11.5f), Theme.MUTED)));
			list.add(Box.createVerticalStrut(8));

			Theme.RoundedPanel section = new Theme.RoundedPanel(null, Theme.SURFACE, Theme.BORDER, 16);
			section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
			for (int i = SECTIONS[s][0]; i < SECTIONS[s][1]; i++) {
				if (i > SECTIONS[s][0]) {
					section.add(divider());
				}
				section.add(menuRow(i));
			}
			list.add(left(section));
			list.add(Box.createVerticalStrut(20));
		}

		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(null);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		Theme.slimScrollBar(scroll.getVerticalScrollBar());
		list.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12)); // gap before the scrollbar
		return scroll;
	}

	private JComponent menuRow(int i) {
		JPanel row = Theme.clear(new BorderLayout(12, 0));
		row.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 12));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

		JLabel name = Theme.label(menuItems[i], Theme.sans(Font.PLAIN, 14.5f), Theme.TEXT);
		row.add(name, BorderLayout.CENTER);

		JPanel right = Theme.clear(new FlowLayout(FlowLayout.RIGHT, 14, 0));
		right.add(Theme.label(prices[i] + " " + CURRENCY, Theme.sans(Font.BOLD, 14f), Theme.TEXT));
		Theme.PillButton add = new Theme.PillButton("Add", false, accent);
		add.setFont(Theme.sans(Font.BOLD, 12.5f));
		add.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addItem(i);
			}
		});
		right.add(add);
		row.add(right, BorderLayout.EAST);
		return left(row);
	}

	/** Right column: the order summary and rating. */
	private JComponent buildOrderPanel() {
		Theme.RoundedPanel card = new Theme.RoundedPanel(new BorderLayout(), Theme.SURFACE, Theme.BORDER, 18);
		card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		card.setPreferredSize(new Dimension(300, 10));

		JPanel top = Theme.clear(null);
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.add(left(Theme.label("Your order", Theme.serif(Font.BOLD, 19f), Theme.TEXT)));
		top.add(Box.createVerticalStrut(2));
		itemsLabel = Theme.label("", Theme.sans(Font.PLAIN, 12.5f), Theme.MUTED);
		top.add(left(itemsLabel));
		top.add(Box.createVerticalStrut(14));
		orderLines = Theme.clear(null);
		orderLines.setLayout(new BoxLayout(orderLines, BoxLayout.Y_AXIS));
		top.add(left(orderLines));
		card.add(top, BorderLayout.NORTH);

		// ---- bottom: total, rating, buttons
		JPanel bottom = Theme.clear(null);
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
		bottom.add(divider());
		bottom.add(Box.createVerticalStrut(12));
		JPanel totalRow = Theme.clear(new BorderLayout());
		totalRow.add(Theme.label("Total", Theme.sans(Font.BOLD, 14f), Theme.TEXT), BorderLayout.WEST);
		totalLabel = Theme.label("", Theme.serif(Font.BOLD, 24f), Theme.TEXT);
		totalRow.add(totalLabel, BorderLayout.EAST);
		bottom.add(left(totalRow));
		bottom.add(Box.createVerticalStrut(18));

		bottom.add(left(Theme.label("Rate your experience", Theme.sans(Font.BOLD, 13f), Theme.TEXT)));
		bottom.add(Box.createVerticalStrut(8));
		bottom.add(left(buildRatingInput()));
		bottom.add(Box.createVerticalStrut(18));

		JPanel buttons = Theme.clear(new GridLayout(1, 2, 10, 0));
		Theme.PillButton clear = new Theme.PillButton("Clear", false, accent);
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < count.length; i++) {
					count[i] = 0;
				}
				refreshOrder();
			}
		});
		placeOrderBtn = new Theme.PillButton("Place order", true, accent);
		placeOrderBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				placeOrder();
			}
		});
		buttons.add(clear);
		buttons.add(placeOrderBtn);
		bottom.add(left(buttons));
		card.add(bottom, BorderLayout.SOUTH);

		refreshOrder();
		return card;
	}

	/** Rating box: type a number from 1 to 5 and press Enter (KeyListener). */
	private JComponent buildRatingInput() {
		JPanel box = Theme.clear(new BorderLayout(12, 0));
		Theme.Stars stars = new Theme.Stars();
		JLabel hint = Theme.label("Type 1-5, press Enter", Theme.sans(Font.PLAIN, 12f), Theme.MUTED);

		JTextField field = new JTextField(2);
		field.setFont(Theme.sans(Font.BOLD, 15f));
		field.setHorizontalAlignment(SwingConstants.CENTER);
		field.setForeground(Theme.TEXT);
		field.setCaretColor(accent);
		field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true),
				BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		field.setPreferredSize(new Dimension(44, 34));

		field.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// only digits can be typed
				if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// live preview of the stars while typing
				try {
					stars.setValue(Integer.parseInt(field.getText().trim()));
				} catch (NumberFormatException ex) {
					stars.setValue(0);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						int rating = Integer.parseInt(field.getText().trim());
						if (rating >= 1 && rating <= 5) {
							customerRating = rating;
							hint.setText("Thanks for your rating!");
							hint.setForeground(Theme.SUCCESS);
						} else {
							hint.setText("Please enter 1 to 5 only");
							hint.setForeground(accent);
						}
					} catch (NumberFormatException ex) {
						hint.setText("Digits only, please");
						hint.setForeground(accent);
					}
				}
			}
		});

		box.add(field, BorderLayout.WEST);
		JPanel right = Theme.clear(null);
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		right.add(left(stars));
		right.add(Box.createVerticalStrut(4));
		right.add(left(hint));
		box.add(right, BorderLayout.CENTER);
		return box;
	}

	// ----------------------------------------------------------- ordering
	void addItem(int i) {
		if (totalItems() >= MAX_ITEMS) {
			JOptionPane.showMessageDialog(frame, "Order limit reached (max " + MAX_ITEMS + " items).", "Order limit",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		count[i]++;
		refreshOrder();
	}

	private void removeItem(int i) {
		if (count[i] > 0) {
			count[i]--;
			refreshOrder();
		}
	}

	private int totalItems() {
		int n = 0;
		for (int c : count) {
			n += c;
		}
		return n;
	}

	/** The most expensive drink in the order is free (today's promotion). */
	private int freeDrinkDiscount() {
		int best = 0;
		for (int i = DRINKS_START; i < DRINKS_END; i++) {
			if (count[i] > 0) {
				best = Math.max(best, prices[i]);
			}
		}
		return best;
	}

	private int total() {
		int total = 0;
		for (int i = 0; i < count.length; i++) {
			total += count[i] * prices[i];
		}
		return total - freeDrinkDiscount();
	}

	private void refreshOrder() {
		orderLines.removeAll();
		int n = totalItems();
		if (n == 0) {
			orderLines.add(left(Theme.label("<html>Nothing here yet.<br>Add dishes from the menu.</html>",
					Theme.sans(Font.PLAIN, 13.5f), Theme.MUTED)));
		}
		for (int i = 0; i < count.length; i++) {
			if (count[i] > 0) {
				orderLines.add(left(orderLine(i)));
				orderLines.add(Box.createVerticalStrut(6));
			}
		}
		int discount = freeDrinkDiscount();
		if (discount > 0) {
			JPanel promo = Theme.clear(new BorderLayout());
			promo.add(Theme.label("Free drink (today only)", Theme.sans(Font.PLAIN, 13f), Theme.SUCCESS),
					BorderLayout.WEST);
			promo.add(Theme.label("-" + discount + " " + CURRENCY, Theme.sans(Font.BOLD, 13f), Theme.SUCCESS),
					BorderLayout.EAST);
			orderLines.add(left(promo));
		}
		itemsLabel.setText(n + (n == 1 ? " item" : " items") + "  ·  max " + MAX_ITEMS);
		totalLabel.setText(total() + " " + CURRENCY);
		placeOrderBtn.setEnabled(n > 0);
		orderLines.revalidate();
		orderLines.repaint();
	}

	private JComponent orderLine(int i) {
		JPanel line = Theme.clear(new BorderLayout(8, 0));
		JLabel minus = Theme.label("−", Theme.sans(Font.BOLD, 15f), Theme.MUTED);
		minus.setToolTipText("Remove one");
		minus.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		minus.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				removeItem(i);
			}
		});
		line.add(minus, BorderLayout.WEST);
		line.add(Theme.label(count[i] + " × " + menuItems[i], Theme.sans(Font.PLAIN, 13.5f), Theme.TEXT),
				BorderLayout.CENTER);
		line.add(Theme.label(count[i] * prices[i] + " " + CURRENCY, Theme.sans(Font.BOLD, 13.5f), Theme.TEXT),
				BorderLayout.EAST);
		return line;
	}

	private void placeOrder() {
		StringBuilder sb = new StringBuilder("Your order from the " + branch + " branch:\n\n");
		for (int i = 0; i < count.length; i++) {
			if (count[i] > 0) {
				sb.append(menuItems[i]).append(" x").append(count[i]).append(" = ").append(count[i] * prices[i])
						.append(" ").append(CURRENCY).append("\n");
			}
		}
		if (freeDrinkDiscount() > 0) {
			sb.append("Free drink: -").append(freeDrinkDiscount()).append(" ").append(CURRENCY).append("\n");
		}
		sb.append("\nTotal: ").append(total()).append(" ").append(CURRENCY);
		if (customerRating > 0) {
			sb.append("\nYour rating: ").append(customerRating).append("/5");
		}
		JOptionPane.showMessageDialog(frame, sb.toString(), "Order placed", JOptionPane.INFORMATION_MESSAGE);
	}

	// -------------------------------------------------------------- helpers
	private static JComponent divider() {
		JPanel d = new JPanel();
		d.setBackground(Theme.BORDER);
		d.setPreferredSize(new Dimension(1, 1));
		d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		d.setAlignmentX(Component.LEFT_ALIGNMENT);
		return d;
	}

	private static <T extends JComponent> T left(T c) {
		c.setAlignmentX(Component.LEFT_ALIGNMENT);
		return c;
	}
}
