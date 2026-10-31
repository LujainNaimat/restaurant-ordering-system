package gui;

import java.awt.Color;

public class ItalyMenuGUI extends MenuGUI {

	public ItalyMenuGUI() {
		super("Italy", "Italian kitchen",
				new String[] { "Pizza", "Lasagna", "Spaghetti", "Fettuccine", "Tiramisu", "Cannoli", "Apple juice",
						"Orange juice", "Water", "French fries", "Salad", "Hummus" },
				new int[] { 30, 27, 25, 24, 20, 18, 10, 10, 5, 8, 6, 6 },
				new int[] { 8, 6, 4, 2 },
				new Color[] { new Color(0x008C45), new Color(0xF4F5F0), new Color(0xCD212A) },
				new Color(0x1E7A4C));
	}
}
