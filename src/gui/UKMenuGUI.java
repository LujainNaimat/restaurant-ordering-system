package gui;

import java.awt.Color;

public class UKMenuGUI extends MenuGUI {

	public UKMenuGUI() {
		super("UK", "British kitchen",
				new String[] { "Fish & Chips", "Sunday Roast", "Cornish Pasty", "Crab & Saffron", "Shepherd's Pie",
						"Apple Pie", "Apple Juice", "Orange Juice", "Water", "French Fries", "Salad", "Hummus" },
				new int[] { 28, 31, 24, 24, 16, 12, 14, 11, 4, 7, 8, 8 },
				new int[] { 7, 8, 6, 10 },
				new Color[] { new Color(0x012169), new Color(0xFFFFFF), new Color(0xC8102E) },
				new Color(0x1F3A7A));
	}
}
