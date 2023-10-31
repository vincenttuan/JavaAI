package lab;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.google.gson.Gson;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

//實用工具類，包含獲取股票資料和繪製股價折線圖的方法
public class Utils {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	
	public static void disableSSLVerification() {
	    try {
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
	        } };

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	// 從台灣證交所API獲取股票資訊的JSON字符串
	private static String getJsonString(String stockNo) throws IOException {
		disableSSLVerification();
		String today = dateFormat.format(new Date());
		// 股票資訊API的URL
		String path = "https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY?date=" + today + "&stockNo=" + stockNo + "&response=json";
		try (Scanner scanner = new Scanner(new URL(path).openStream()).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	// 獲取指定股票的時間序列和收盤價格
	public static double[][] getTimeAndClosingPrice(String stockNo) throws IOException {
		// 調用getJsonString獲取JSON數據
		String jsonString = getJsonString(stockNo);

		// 使用Gson解析JSON數據
		Gson gson = new Gson();
		TradingData tradingData = gson.fromJson(jsonString, TradingData.class);

		// 創建一個二維數組來儲存時間和價格
		double[][] prices = new double[tradingData.data.size()][2];

		for (int i = 0; i < tradingData.data.size(); i++) {
			List<String> dailyData = tradingData.data.get(i);
			prices[i][0] = i + 1; // 時間流水號
			prices[i][1] = Double.parseDouble(dailyData.get(6).replace(",", "")); // 收盤價在索引6的位置
		}

		return prices;
	}

	// 單獨獲取指定股票的收盤價格
	public static double[] getClosingPrice(String stockNo) throws IOException {
		// 調用getJsonString獲取JSON數據
		String jsonString = getJsonString(stockNo);
		Gson gson = new Gson();
		TradingData tradingData = gson.fromJson(jsonString, TradingData.class);

		// 從tradingData中提取收盤價格，並轉換為double數組
		return tradingData.data.stream().mapToDouble(dailyData -> Double.parseDouble(dailyData.get(6).replace(",", ""))) // 收盤價是在索引6的位置
				.toArray();
	}

	// 獲取指定股票的成交量
	public static double[] getVolume(String stockNo) throws IOException {
		// 調用getJsonString獲取JSON數據
		String jsonString = getJsonString(stockNo);
		Gson gson = new Gson();
		TradingData tradingData = gson.fromJson(jsonString, TradingData.class);

		// 從tradingData中提取成交量，並轉換為double數組
		return tradingData.data.stream().mapToDouble(dailyData -> Double.parseDouble(dailyData.get(8).replace(",", ""))) // 成交量是在索引8的位置
				.toArray();
	}

	// 顯示股票價格折線圖
	private static void displayChart(String symbol, double[] prices, double predictorPrice) {
		// 將預測值格式化為兩位小數
		predictorPrice = ((int) (predictorPrice * 100)) / 100.0;

		// 將原始價格數組和預測值組合成一個新的數組
		double[] extendedPrices = new double[prices.length + 1];
		System.arraycopy(prices, 0, extendedPrices, 0, prices.length);
		extendedPrices[extendedPrices.length - 1] = predictorPrice;
		prices = extendedPrices;

		// 計算價格的最小和最大值，以設定y軸範圍
		double minPrice = Arrays.stream(prices).min().orElse(0);
		double maxPrice = Arrays.stream(prices).max().orElse(0);

		// 創建數據集並生成折線圖
		DefaultCategoryDataset dataset = createDataset(prices);
		JFreeChart chart = ChartFactory.createLineChart(symbol + " Stock Price Predictor: " + predictorPrice, "Time",
				"Price", dataset, PlotOrientation.VERTICAL, true, true, false);

		CategoryPlot plot = chart.getCategoryPlot();
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(minPrice * 0.90, maxPrice * 1.10);

		// 設置折線圖的渲染方式
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		renderer.setDefaultShapesVisible(true);
		renderer.setDefaultShape(new Ellipse2D.Double(-3, -3, 6, 6));
		renderer.setDefaultFillPaint(Color.BLACK);
		renderer.setDefaultOutlinePaint(Color.BLACK);
		renderer.setUseOutlinePaint(true);
		renderer.setDefaultOutlineStroke(new BasicStroke(1.0f));
		renderer.setSeriesPaint(0, Color.RED);
		plot.setRenderer(renderer);

		// 將折線圖放入面板並顯示
		ChartPanel panel = new ChartPanel(chart);
		JFrame frame = new JFrame("Stock Price Chart");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// 創建數據集，包含價格數據
	private static DefaultCategoryDataset createDataset(double[] prices) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < prices.length; i++) {
			dataset.addValue(prices[i], "Price", String.valueOf(i + 1));
		}
		return dataset;
	}

	// 主方法，繪製折線圖
	public static void drawLineChart(String symbol, double predictorPrice) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				double[] prices;
				try {
					prices = getClosingPrice(symbol);
					displayChart(symbol, prices, predictorPrice);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void drawLineChart(String symbol, double[] prices, double predictorPrice) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				displayChart(symbol, prices, predictorPrice);
			}
		});
	}

	// 內部類，用於解析從API獲取的股票交易數據
	private class TradingData {
		String stat;
		String date;
		String title;
		List<String> fields;
		List<List<String>> data;
		List<String> notes;
		int total;
	}
}
