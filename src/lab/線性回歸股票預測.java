package lab;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class 線性回歸股票預測 {
	public static void main(String[] args) throws Exception {
		// 股票代號
		String symbol = "0050";
		
		// 股票價格與對應時間的二維陣列
		double[][] data = Utils.getTimeAndClosingPrice(symbol);
		
		// 利用簡單線性回歸
		SimpleRegression regression = new SimpleRegression();
		
		// 添加數據點
		for(double[] dataPoint : data) {
			regression.addData(dataPoint[0], dataPoint[1]);
		}
		
		// 跑模型
		regression.regress();
		
		// 得到明日的預測價格
		double predictedPrice = regression.predict(data.length+1);
		
		System.out.println("明日預測價格: " + predictedPrice);
		
		// 繪圖
		Utils.drawLineChart(symbol, predictedPrice);
	}
}
