package com.cs.oms.dao;

import java.math.BigDecimal;
import java.util.Set;

import com.cs.oms.common.Execution;
import com.cs.oms.common.Instrument;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;

public interface OMSServiceDao {
	static final int LIMIT_ORDER = 0;
	static final int MARKET_ORDER = 1;
	static final int VALID_ORDER = 0;
	static final int INVALID_ORDER = 1;
	static final int BOOK_OPEN = 0;
	static final int BOOK_CLOSE = 1;
	static final int BOOK_EXECUTED = 2;

	boolean createOrder(long instrumentId, long quantity, BigDecimal price);

	boolean createOrder(long instrumentId, long quantity);

	boolean updateOrder(Order order);

	boolean createExecution(long instrumentId, long quantity, BigDecimal price);

	boolean updateOrderBook(OrderBook orderBook);

	boolean createOrderBook(long instrumentId);

	Set<Execution> getExecutions(long instrumentId);

	Set<Order> getOrders(long instrumentId);

	Set<Order> getValidOrders(long instrumentId);

	Set<Order> getInvalidOrders(long instrumentId);

	OrderBook getOrderBook(long instrumentId);

	Instrument getInstrument(String symbol);

	Order getOrder(long orderId);

	Order getBiggestOrder(long instrumentId);

	Order getSmallestOrder(long instrumentId);

	Order getEarliestOrder(long instrumentId);

	Order getLatestOrder(long instrumentId);

}
