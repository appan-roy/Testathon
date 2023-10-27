package com.qa.testathon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.utils.ListUtils;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * PROBLEM STATEMENT
 * -----------------
 * Pre-requisite: A database is created with products table. Query --> SELECT id, product_name FROM codesense.product_services.products;
 * 
 * Step 1: Build an API service from the pre-requisite to get data from DB. Data is available is the product table.
 * Step 2: Automation script should call the API and select any one product from the list.
 * Step 3: Search the product in any two e-commerce websites.
 * Step 4: Compare the price of the item in the e-commerce websites.
 * Step 5: Select the website with the cheapest price and proceed to checkout.
 * Step 6: Run tests in parallel mode in the browsers - add the products and goto check out screen
 * 
 * @author Appan Roy
 *
 */

public class ProductPriceComparisonAndCheckoutTest {
	
	List<String> productsList = new ArrayList<String>();
	String randomProduct;
	double amazonPrice, flipkartPrice;
	WebDriver driver;
	
	@BeforeClass
	public void setUp() {
		System.setProperty("webdriver.chrome.driver", "./src/main/resources/webdrivers/chromedriver.exe");
		driver = new ChromeDriver();
		driver.manage().window().maximize();
	}
	
	@Test
	public void getProducts() {
		RestAssured.baseURI = "http://localhost:4000";
		RequestSpecification httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		Response response = httpRequest.get("/products");

		assertEquals(response.statusCode(), 200);

		JsonPath jp = new JsonPath(response.asString());
		String totalProducts = jp.get("data.size()").toString();
		System.out.println("Total products: " + totalProducts);

		System.out.println("================= All products list ======================");
		for (int i = 0; i < Integer.parseInt(totalProducts); i++) {
			String productName = jp.getString("[" + i + "].product_name");
			System.out.println("Product Name: " + productName);
			productsList.add(productName);
		}
	}
	
	@Test(dependsOnMethods = {"getProducts"})
	public void pickRandomProductFromApiResponse() {
		randomProduct = ListUtils.getRandomValueFromList(productsList);
		System.out.println("Selected random product is : " + randomProduct);
	}
	
	@Test(dependsOnMethods = {"pickRandomProductFromApiResponse"})
	public void searchProduct() {
		driver.get("https://www.amazon.in/");
		
		driver.findElement(By.cssSelector("#twotabsearchtextbox")).sendKeys(randomProduct);
		driver.findElement(By.xpath("//input[@value='Go']")).click();
		
		int NoOfProducts = driver.findElements(By.xpath("//span[@data-component-type='s-search-results']/div/div/descendant::a/span[@class='a-size-medium a-color-base a-text-normal']")).size();
		
		for(int i = 1; i <= NoOfProducts; i++) {
			String productName = driver.findElement(By.xpath("(//span[@data-component-type='s-search-results']/div/div/descendant::a/span[@class='a-size-medium a-color-base a-text-normal'])[" + i + "]")).getText();
			if(productName.toLowerCase().contains(randomProduct.toLowerCase())) {
				//String price = driver.findElement(By.xpath("(//span[@data-component-type='s-search-results']/div/descendant::span[@class='a-price-whole'])[" + i + "]")).getText().replaceAll("[^0-9]", "");
				String price = driver.findElement(By.xpath("(//span[@data-component-type='s-search-results']/div/div/descendant::a/span[@class='a-size-medium a-color-base a-text-normal']/../../../following-sibling::div[2]/descendant::span[@class='a-price-whole'])[" + i + "]")).getText().replaceAll("[^0-9]", "");
				amazonPrice = Double.parseDouble(price);
				break;
			}
		}
		
		driver.switchTo().newWindow(WindowType.TAB);
		
		driver.get("https://www.flipkart.com/");
		
		if(driver.findElements(By.xpath("//span[text()='✕']")).size() != 0)
			driver.findElement(By.xpath("//span[text()='✕']")).click();
		
		driver.findElement(By.xpath("//input[@name='q']")).sendKeys(randomProduct);
		driver.findElement(By.xpath("//input[@name='q']")).sendKeys(Keys.ENTER);
		
		int NumOfProducts = driver.findElements(By.xpath("//div[@id='container']/descendant::div[@class='_4rR01T']")).size();
		
		for(int i = 1; i <= NumOfProducts; i++) {
			String productName = driver.findElement(By.xpath("(//div[@id='container']/descendant::div[@class='_4rR01T'])[" + i + "]")).getText();
			if(productName.toLowerCase().contains(randomProduct.toLowerCase())) {
				String price = driver.findElement(By.xpath("(//div[@id='container']/descendant::div[@class='_4rR01T']/../following-sibling::div/descendant::div[@class='_30jeq3 _1_WHN1'])[" + i + "]")).getText().replaceAll("[^0-9]", "");
				flipkartPrice = Double.parseDouble(price);
				break;
			}
		}
	}
	
	@Test(dependsOnMethods = {"searchProduct"})
	public void comparePriceAndCheckout() {
		Set<String> windows = driver.getWindowHandles();
		if(amazonPrice < flipkartPrice) {
			System.out.println("Amazon price is less than flipkart price");
			for (String window : windows) {
				if(driver.switchTo().window(window).getTitle().contains("amazon")) {
					driver.switchTo().window(window);
				}
			}
			
			int NoOfProducts = driver.findElements(By.xpath("//span[@data-component-type='s-search-results']/div/div/descendant::a/span[@class='a-size-medium a-color-base a-text-normal']")).size();
			
			for(int i = 1; i <= NoOfProducts; i++) {
				String productName = driver.findElement(By.xpath("(//span[@data-component-type='s-search-results']/div/div/descendant::a/span[@class='a-size-medium a-color-base a-text-normal'])[" + i + "]")).getText();
				if(productName.toLowerCase().contains(randomProduct.toLowerCase())) {
					driver.findElement(By.xpath("(//span[@data-component-type='s-search-results']/div/div/descendant::a/span[@class='a-size-medium a-color-base a-text-normal'])[" + i + "]")).click();
					break;
				}
			}
		} else if(amazonPrice > flipkartPrice) {
			System.out.println("Flipkart price is less than amazon price");
			for (String window : windows) {
				if(driver.switchTo().window(window).getTitle().contains("flipkart")) {
					driver.switchTo().window(window);
				}
			}
			
			int NoOfProducts = driver.findElements(By.xpath("//div[@id='container']/descendant::div[@class='_4rR01T']")).size();
			
			for(int i = 1; i <= NoOfProducts; i++) {
				String productName = driver.findElement(By.xpath("(//div[@id='container']/descendant::div[@class='_4rR01T'])[" + i + "]")).getText();
				if(productName.toLowerCase().contains(randomProduct.toLowerCase())) {
					driver.findElement(By.xpath("(//div[@id='container']/descendant::div[@class='_4rR01T'])[" + i + "]")).click();
					break;
				}
			}
		} else{
			System.out.println("Both the prices are same!!");
		}
	}
	
	@AfterClass
	public void tesrDown() {
		driver.quit();
	}
	
}
