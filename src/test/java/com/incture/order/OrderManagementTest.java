package com.incture.order;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.greaterThan;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OrderManagementTest {
	
	@Test
	@Order(1)
	public void getOrders() {
		get("http://localhost:8081/api/v1/orders/")
		.then()
		.assertThat()
		.statusCode(200)
		.body("size()",greaterThan(0));
	}
	
	
	@Test
	@Order(2)
	public void getOrderById() {
		Long orderId = 2L;
		get("http://localhost:8081/api/v1/orders/"+ orderId +"")
		.then()
		.assertThat()
		.statusCode(200)
		.body("id", Matchers.equalTo(orderId.intValue()));
	}
	
	@Test
	@Order(3)
	public void createOrder() throws JSONException {
		JSONObject order = new JSONObject();
		order.put("itemName","Moniter");
		order.put("quantity",2);
		order.put("amount",20000);
		order.put("customerName","Adithya Hebbar");
		order.put("customerAddress","Mangalore");
		order.put("modeOfPayment","COD");
		
		RestAssured.given()
		.contentType("application/json")
		.body(order.toString())
		.when()
		.post("http://localhost:8081/api/v1/orders/")
		.then()
		.statusCode(201)
		.body("itemName", Matchers.equalTo("Moniter"))
		.body("customerName", Matchers.equalTo("Adithya Hebbar"));
	}
	
	@Test
	@Order(4)
	public void updateOrder() throws JSONException {
		JSONObject order = new JSONObject();
		order.put("itemName","Laptop");
		order.put("quantity",4);
		order.put("amount",60000);
		order.put("customerName","Adithya Hebbar");
		order.put("customerAddress","Nitte Mangalore");
		order.put("modeOfPayment","COD");
		
		RestAssured.given().pathParam("id",2)
		.contentType("application/json").body(order.toString())
		.when()
		.put("http://localhost:8081/api/v1/orders/{id}")
		.then()
		.statusCode(200)
		.body("id", Matchers.equalTo(2))
		.body("quantity", Matchers.equalTo(4))
		.body("customerName", Matchers.equalTo("Adithya Hebbar"));
	}
	
	@Test
	@Order(5)
	public void deleteOrder() {
		Long orderId = 302L;
		RestAssured.given().pathParam("id", orderId)
		.when()
		.delete("http://localhost:8081/api/v1/orders/{id}")
		.then()
		.statusCode(200);
	}
	
	
}
