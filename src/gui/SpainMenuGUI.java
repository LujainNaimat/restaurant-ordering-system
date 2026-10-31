package gui;

import java.awt.Color;

public class SpainMenuGUI extends MenuGUI {

	public SpainMenuGUI() {
		super("Spain", "Spanish kitchen",
				new String[] { "Paella", "Gazpacho", "Tortilla Española", "Patatas bravas", "Churros",
						"Tarta de Santiago", "Apple juice", "Orange juice", "Water", "French fries", "Salad", "Hummus" },
				new int[] { 25, 22, 21, 15, 15, 10, 10, 5, 8, 7, 6, 7 },
				new int[] { 8, 10, 9, 6 },
				new Color[] { new Color(0xAA151B), new Color(0xF1BF00), new Color(0xAA151B) },
				new Color(0xB4372B));
	}
}
