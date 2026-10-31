package procedural;

import java.util.Scanner;

public class Restaurant {
	public static int[] order = new int[20];// the customer can order up to 20
	public static String menuItems[];
	public static int[] priceList;
	public static int[] dishRating;

	public static void main(String[] args) {
		System.out.println("welcome to our restaurant");
		System.out.println("Today only ,Free Drink with any order at All branches");
		int choice = 0;
		Scanner scan = new Scanner(System.in);
		try {
			System.out.println("choose a branch 1.spain 2.italy 3.uk");
			choice = Integer.parseInt(scan.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("please enter digit number only");
		}

		switch (choice) {
		case 1: {
			menu1();
			takeOrder(scan);
			int[] count = countOrderItems();
			printOrder(count);
			customerRating(scan);

			break;
		}
		case 2: {
			menu2();
			takeOrder(scan);
			int[] count = countOrderItems();
			printOrder(count);
			customerRating(scan);
			break;
		}
		case 3: {
			menu3();
			takeOrder(scan);
			int[] count = countOrderItems();
			printOrder(count);
			customerRating(scan);
			break;
		}

		default: {
			System.out.println("invalid branch selection");
			break;
		}
		}
		scan.close();
	}

//spain menu
	public static void menu1() {
		menuItems = new String[] { "Paella", "Gazpacho", "Tortilla Española", "Patatas bravas", "Churros",
				"Tarta de Santiago", "Apple juice", "Orange juice", "Water", "French fries", "Salad", "Hummus" };
		priceList = new int[] { 25, 22, 21, 15, 15, 10, 10, 5, 8, 7, 6, 7 };
		dishRating = new int[] { 8, 10, 9, 6 };
		sortAndPrintDishesWithRatings();// print main dishes sorted
		print();// print menu items+prices

	}

	// italy menu
	public static void menu2() {
		menuItems = new String[] { "Pizza", "Lasagna", "Spaghetti", "Fettuccine", "Tiramisu", "Cannoli", "Apple juice",
				"Orange juice", "Water", "French fries", "Salad", "Hummus" };
		priceList = new int[] { 30, 27, 25, 24, 20, 18, 10, 10, 5, 8, 6, 6 };
		dishRating = new int[] { 8, 6, 4, 2 };
		sortAndPrintDishesWithRatings();// print main dishes sorted
		print();// print menu items+prices

	}
	//uk menu
	public static void menu3(){
		menuItems =new String[] { "Fish & Chips", "Sunday Roast", "Cornish Pasty", "Crab & Saffron",
				"Shepherd's Pie", "Apple Pie", "Apple Juice", "Orange Juice", "Water", "French Fries", "Salad", "Hummus" };
	 
		priceList = new int[]{ 28, 31, 24, 24, 16, 12, 14, 11, 4, 7, 8, 8 };
	 
		dishRating =new int[] { 7, 8, 6, 10 };
		sortAndPrintDishesWithRatings();// print main dishes sorted
		print();// print menu items+prices

		
	}

//method to print the full menu items
	public static void print() {
		System.out.println("FULL menu");
		for (int i = 0; i < menuItems.length; i++) {
			System.out.println(i + 1 + ". " + menuItems[i] + " - " + priceList[i] + " JD");
		}
	}

	// method to sort and print main dishes using selection sort
	public static void sortAndPrintDishesWithRatings() {
		String[] tempDishNames = new String[menuItems.length];// created a copy of the array and string so that the
																// orginal doesnt get modified
		int[] tempDishRatings = new int[dishRating.length];
		for (int i = 0; i < tempDishRatings.length; i++) {
			tempDishNames[i] = menuItems[i];
			tempDishRatings[i] = dishRating[i];
		}
		// selection sort
		for (int i = 0; i < tempDishRatings.length - 1; i++) {
			int maxIndex = i;
			for (int j = i + 1; j < tempDishRatings.length; j++) {
				if (tempDishRatings[j] > tempDishRatings[maxIndex]) {
					maxIndex = j;
				}
			}
			// Swap ratings
			int tempRating = tempDishRatings[i];
			tempDishRatings[i] = tempDishRatings[maxIndex];
			tempDishRatings[maxIndex] = tempRating;

			// Swap names
			String tempName = tempDishNames[i];
			tempDishNames[i] = tempDishNames[maxIndex];
			tempDishNames[maxIndex] = tempName;
		}
		// print the main dishes an thier rating
		System.out.println("main Dishes sorted");
		for (int i = 0; i < tempDishRatings.length; i++) {
			System.out.println(tempDishNames[i] + " (Rating: " + tempDishRatings[i] + ")");
		}

	}

	// method to handle order
	public static void takeOrder(Scanner scan) {
		System.out.println("Enter the numbers of items you want to order (0 to finish):");
		int i = 0;
		int item = 1;
		while (i < order.length) {//the customer can only order up to the limit
			System.out.print("Item " + (i + 1) + ": ");
			String input = scan.nextLine();

			try {
				item = Integer.parseInt(input);
				if (item == 0) {
					break; // finish order input
				}
				order[i] = item; //the item is add to the order list
				i++;
			} catch (NumberFormatException e) {
				System.out.println("Only enter digits please.");
			}
		}
//if the order didnt end and it reached the full length of the arr it stops and prints
		if (item != 0 && i == order.length) {
			System.out.println("Order limit reached (max " + order.length + " items).");
		}
	}

//method to count how many time an item was ordered 
	public static int[] countOrderItems() {
		int[] count = new int[menuItems.length]; // menu items 1 to 12
//loops through each item in the order
		for (int item : order) {
			if (item >= 1 && item <= menuItems.length) {
				count[item - 1]++;
			} else if (item != 0) {

				System.out.println("Warning: invalid order item " + item + " ignored.");
			}
		}
		return count;
	}

	// method to calculate and print order summary
	public static void printOrder(int[] count) {
		int total = 0;
		System.out.println("Your order:");
		for (int i = 0; i < count.length; i++) {
			if (count[i] > 0) {
				int itemTotal = count[i] * priceList[i];
				System.out.println(menuItems[i] + " x" + count[i] + " = " + itemTotal + " JD");
				total += itemTotal;
			}
		}
		System.out.println("Total: " + total + " JD");

	}

	// to rate the customer experience
	public static void customerRating(Scanner scan) {
		while (true) {
			System.out.print("Please rate your experience from 1 to 5: ");

			try {
				String ratingInput = scan.nextLine();
				int rating = Integer.parseInt(ratingInput);
				if (rating >= 1 && rating <= 5) {
					System.out.println("Thank you for your rating!");
					break;
				} else {
					System.out.println("Please enter a rating between 1 and 5.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a number from 1 to 5.");// if the customer inputs other
																						// than number
			}
		}
	}

}
