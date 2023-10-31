package lab;

public class 找零錢程式 {

	public static void main(String[] args) {
		// 零錢有 50, 10, 5, 1
		// 飲料一瓶 23 元, 我付 100 元, 請問要找幾元 ? 每個硬幣的數量
		int price = 23; // 商品金額
		int cash = 100; // 付款金額
		int change = cash - price; // 計算找零
		System.out.println("要找: " + change); // 印出來
		
		// 找零
		int num50 = change / 50; // 50 元的數量
		change = change % 50; // 剩餘金額
		
		int num10 = change / 10; // 10 元的數量
		change = change % 10; // 剩餘金額
		
		int num5 = change / 5; // 5 元的數量
		change = change % 5; // 剩餘金額
		
		int num1 = change; // 1 元的數量 
		
		// 輸出結果
		System.out.println("50 元: " + num50 + " 個");
		System.out.println("10 元: " + num10 + " 個");
		System.out.println("5 元: " + num5 + " 個");
		System.out.println("1 元: " + num1 + " 個");
	}

}
