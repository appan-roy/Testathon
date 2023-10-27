package com.qa.testathon;

/**
 * PROBLEM STATEMENT
 * -----------------
 * Step 1: Get the list of crypto coins using an API.
 * Step 2: List them in order of their value in USD - Ascending order.
 * Step 3: Pick any one crypto coin randomly from the list.
 * Step 4: Goto any online crypto exchange and compare the value from the API to the web UI. (The API service should not belong to the same UI)
 * Step 5: For the same crypto coin get its value for the last 5 hours with a timespan of 1 hour.
 * Step 6: Represent the result in a dashboard.
 * 
 * @author Appan Roy
 *
 */

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

import com.qa.utils.MapUtils;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class CryptoCoinsTest {

	HashMap<String, Double> cryptoCoinsMap = new HashMap<String, Double>();
	String randomCoin;
	double coinPriceAPI;
	double coinPriceWeb;

	@Test()
	public void getListOfCryptoCoins() {
		HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("vs_currency", "usd");
		queryParams.put("order", "market_cap_desc");
		queryParams.put("per_page", "10");
		queryParams.put("page", "1");

		RestAssured.baseURI = "https://api.coingecko.com/api/v3/coins/markets";
		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.queryParams(queryParams).request(Method.GET);

		assertEquals(response.statusCode(), 200);

		JsonPath jp = new JsonPath(response.asString());
		String totalCoins = jp.get("data.size()").toString();
		System.out.println("Total coins: " + totalCoins);

		System.out.println("================= All coins list ======================");
		for (int i = 0; i < Integer.parseInt(totalCoins); i++) {
			String name = jp.getString("[" + i + "].name");
			double current_price = Double.parseDouble(jp.getString("[" + i + "].current_price"));
			System.out.println("Name: " + name + ", Current price: " + current_price);
			cryptoCoinsMap.put(name, current_price);
		}
	}

	@Test(dependsOnMethods = { "getListOfCryptoCoins" })
	public void sortCryptoCoinsInAscendingOrderUSD() {
		// sort the map by value
		Map<String, Double> cryptoCoinsSortedMap = MapUtils.sortHashMapByValue(cryptoCoinsMap);

		// print the sorted hashmap
		System.out.println("================= Sorted coins list ======================");
		for (Map.Entry<String, Double> entry : cryptoCoinsSortedMap.entrySet())
			System.out.println("Name: " + entry.getKey() + ", Current price: " + entry.getValue());
	}

	@Test(dependsOnMethods = { "sortCryptoCoinsInAscendingOrderUSD" })
	public void pickRandomCoinFromApiResponse() {
		randomCoin = MapUtils.getRandomKeyFromHashMap(cryptoCoinsMap);
		coinPriceAPI = Math.round(cryptoCoinsMap.get(randomCoin) * 100.0) / 100.0;
		System.out.println("================= Random coin details from API ======================");
		System.out.println("Random crypto coin is : " + randomCoin);
		System.out.println("Price for the crypto coin " + randomCoin + " from API is : " + coinPriceAPI);
	}

	@Test(dependsOnMethods = { "pickRandomCoinFromApiResponse" })
	public void getPriceOfTheRandomCoinFromWeb() throws InterruptedException {
		System.setProperty("webdriver.chrome.driver", "./src/main/resources/webdrivers/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://coinmarketcap.com/coins/");

		driver.findElement(By.xpath("//div[text()='Search']")).click();
		Thread.sleep(1000);
		driver.findElement(By.xpath("//input[contains(@placeholder,'Search coin')]")).sendKeys(randomCoin);
		Thread.sleep(1000);
		driver.findElement(By.xpath("(//div[@class='select-control'])[1]")).click();
		Thread.sleep(1000);

		String strCoinPriceWeb = driver.findElement(By.xpath("//*[@id='section-coin-overview']/div[2]/span")).getText()
				.trim().replaceAll("[^0-9.]", "");
		coinPriceWeb = Math.round(Double.parseDouble(strCoinPriceWeb) * 100.0) / 100.0;
		System.out.println("================= Random coin details from Web ======================");
		System.out.println("Price for the crypto coin " + randomCoin + " from web is : " + coinPriceWeb);
		driver.quit();
	}

	@Test(dependsOnMethods = { "pickRandomCoinFromApiResponse", "getPriceOfTheRandomCoinFromWeb" })
	public void comparePriceApiVsWeb() {
		double coinPriceDiff = Math.round((coinPriceAPI - coinPriceWeb) * 100.0) / 100.0;
		System.out.println("================= Coin price comparison between API & Web ======================");
		if (coinPriceDiff > 0)
			System.out.println("API coin price is greater by USD " + coinPriceDiff);
		else if (coinPriceDiff < 0)
			System.out.println("Web coin price is greater by USD " + (-coinPriceDiff));
		else
			System.out.println("Coin price is same in both API & Web");
	}

}
