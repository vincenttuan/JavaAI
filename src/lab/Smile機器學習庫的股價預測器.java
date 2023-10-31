package lab;

import smile.data.DataFrame; // 引入Smile數據結構中的DataFrame
import smile.data.Tuple; // 引入Smile數據結構中的Tuple
import smile.data.formula.Formula; // 引入Smile中處理數據公式的Formula類
import smile.regression.RandomForest; // 引入Smile中的隨機森林回歸算法
import smile.data.vector.DoubleVector; // 引入Smile中用於創建雙精度向量的DoubleVector類

/**
 * Smile 機器學習庫的股價預測器
 * 
 * Smile（Statistical Machine Intelligence and Learning Engine）是一個用Java編寫的全面的機器學習函式庫，
 * 它提供了多種機器學習演算法和資料處理的方法。 Smile 的設計目標是提供一個可以輕鬆使用的機器學習工具，同時保持高效能和靈活性。 
 * 它包含的演算法涵蓋了分類、迴歸、聚類、關聯規則和維度約簡等多個領域。
 * 
 * Smile的特點包括：
 * 1. 多樣的演算法支援：Smile提供了廣泛的機器學習演算法，
 * 2. 包括支援向量機（SVM）、隨機森林、梯度提升樹（GBT）、k-最近鄰（k-NN）、邏輯迴歸、人工神經網路等。
 * 3. 易用性：Smile旨在簡化機器學習的應用，它提供了清晰的API和豐富的文檔，使得開發人員可以快速理解和使用。
 * 4. 效能：Smile在設計上著重效率和效能，可以處理大規模的資料集。
 * 5. 資料處理：Smile不僅提供機器學習演算法，還包含了資料預處理、特徵抽取和資料轉換的工具。
 * 6. 視覺化：Smile還包含了一些資料視覺化工具，用於繪製決策邊界、資料點、統計圖表等。
 * 7. 跨平台：作為一個Java庫，Smile可以在任何支援Java的平台上運行，包括Windows、Linux和macOS。
 * 
 * DataFrame 用於存儲和操作結構化數據。
 * DoubleVector 用於創建包含數字數據的列。
 * RandomForest.fit 方法用於訓練隨機森林模型。
 * Tuple 代表數據集中的一行數據，在這裡是用來做預測的最後一個觀察值。
 * 
 * 隨機森林算法是一種集成學習方法，用於回歸和分類問題。它建立於決策樹算法之上，通過結合多棵決策樹的預測結果來提高整體的預測準確度。
*/
public class Smile機器學習庫的股價預測器 {

    public static void main(String[] args) throws Exception {
    	String symbol = "0050";
        // 使用GetPrice類的getClosingPrice方法獲取這支股票過去30天的收盤價格
        double[] prices = Utils.getClosingPrice(symbol);
        // 使用GetPrice類的getVolume方法獲取這支股票過去30天的成交量
        double[] volumes = Utils.getVolume(symbol);

        // 創建一個DataFrame來存儲股票的價格和成交量數據
        // 首先創建一個只包含價格的DataFrame
        DataFrame data = DataFrame.of(DoubleVector.of("Price", prices));
        // 將成交量數據與價格數據合併，形成一個包含兩個特徵的DataFrame
        data = data.merge(DoubleVector.of("Volume", volumes));

        // 定義數據集中的響應變量（即我們想要預測的目標變量），這裡是"Price"
        Formula formula = Formula.lhs("Price");

        // 使用隨機森林算法建立回歸模型。這裡指定"Price"為因變量(你要觀察的)，其餘列作為自變量(可能會影響價格的因素例如：成交量)
        RandomForest model = RandomForest.fit(formula, data);

        // 獲取數據集中的最後一條數據（最後一天的價格和成交量），以預測下一個值（明日股價）
        Tuple lastRow = data.stream().skip(data.nrows() - 1).findFirst().orElse(null);

        // 使用隨機森林模型對最後一條數據進行預測，得出的forecast即為預測的明日股價
        double predictedPrice = model.predict(lastRow);

     	// 輸出預測結果
        System.out.println("預測下一個值（明日股價）: " + predictedPrice);
        
        // 繪圖:
        Utils.drawLineChart(symbol, prices, predictedPrice);
    }
}
