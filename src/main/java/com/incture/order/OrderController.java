package com.incture.order;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.incture.ship.Shipment;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
	
	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	private LoadBalancerClient loadBalancerClient;
	
	@Autowired 
	private RestTemplate restTemplate;
	
	@GetMapping("/")
	public List<Order> getOrders() {
		return orderRepository.findAll();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
		Optional<Order> orderDetails =  orderRepository.findById(id);
		if (orderDetails.isPresent()) {
            return new ResponseEntity<>(orderDetails.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@PostMapping("/")
	public ResponseEntity<Order> createOrder(@RequestBody Order order) {
		try {
			order.setTotalAmount(order.getAmount()*order.getQuantity());
			Order newOrder = orderRepository.save(order);
			return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/ship/{id}")
	public ResponseEntity<String> placeOrderToShipment(@PathVariable Long id) {
		Optional<Order> order = orderRepository.findById(id);
		if(order.isPresent()) {
			String url = getBaseUrl("SHIPMENT-MANAGEMENT") + "/api/v1/ship/";
			Shipment shipment = new Shipment();
			shipment.setOrderId(id);
			shipment.setShipmentDate(new Date());
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, 7);
			Date estimatedDeliveryDate = calendar.getTime();
			shipment.setEstimatedDeliveryDate(estimatedDeliveryDate);
			HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    HttpEntity<Shipment> request = new HttpEntity<>(shipment, headers);
			final ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
			return response;
		} else {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	@PutMapping("/{id}")
	public ResponseEntity<Order> updateOrder(@PathVariable Long id ,@RequestBody Order order) {
		Optional<Order> orderData = orderRepository.findById(id);
		
		if(orderData.isPresent()) {
			Order updateOrder = orderData.get();
			updateOrder.setItemName(order.getItemName());
			updateOrder.setAmount(order.getAmount());
			updateOrder.setQuantity(order.getQuantity());
			updateOrder.setTotalAmount(updateOrder.getAmount()*updateOrder.getQuantity());
			updateOrder.setCustomerName(order.getCustomerName());
			updateOrder.setCustomerAddress(order.getCustomerAddress());
			updateOrder.setModeOfPayment(order.getModeOfPayment());
			return new ResponseEntity<>(orderRepository.save(updateOrder),HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCandidate(@PathVariable("id") Long id) {
        try {
            orderRepository.deleteById(id);
            return new ResponseEntity<>("Deleted",HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
	public String getBaseUrl(String clientName) {
		ServiceInstance instance = loadBalancerClient.choose(clientName);
		return instance.getUri().toString();
	}
	
	
	
	
}

