package com.cs.oms.service.dao;

import java.util.Map;
import java.util.Set;

import com.cs.oms.common.Execution;
import com.cs.oms.common.Instrument;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;

public interface OMSServiceDao {
	void save(Order order);
	void update(Order order);
	void save(Execution execution);
	void update(OrderBook orderBook);
	void save(OrderBook orderBook);
	Set<Execution> getExecutions(long instrumentId);
	Set<Order> getOrders(long instrumentId);
	OrderBook getOrderBook(long instrumentId);
	Instrument getInstrument(String symbol);
	Order getOrder(long orderId);
	void loadAllInstrument(Map<String, Instrument> instrumentMap);
	
}
