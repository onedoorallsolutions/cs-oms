package com.cs.oms.service.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.cs.oms.common.Execution;
import com.cs.oms.common.Instrument;
import com.cs.oms.common.LimitOrder;
import com.cs.oms.common.MarketOrder;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;

public class OMSServiceDaoImpl implements OMSServiceDao {
	private final static Logger logger = Logger.getLogger(OMSServiceDaoImpl.class);
	private AtomicInteger instrumentIds = new AtomicInteger(1);
	private AtomicInteger orderIds = new AtomicInteger(1);
	private AtomicInteger executionIds = new AtomicInteger(1);
	private Map<String, Instrument> instrumentMap = new HashMap<>();
	private Map<Integer, OrderBook> orderBooks = new HashMap<>();
	private Map<Integer, Order> orders = new HashMap<>();

	@Override
	public boolean createInstrument(String symbol) {
		if (instrumentMap.containsKey(symbol)) {
			logger.error("Instrument Alredy Loaded for Symbol:" + symbol);
			return false;
		}
		int id = instrumentIds.getAndIncrement();
		Instrument instrument = new Instrument(id, symbol);
		instrumentMap.put(symbol, instrument);

		return true;

	}

	@Override
	public Instrument getInstrument(String symbol) {
		return instrumentMap.get(symbol);
	}

	@Override
	public List<Instrument> getAllInstrument() {
		return instrumentMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public OrderBook createOrderBook(String symbol) {
		Instrument instrument = instrumentMap.get(symbol);
		if (instrument == null) {
			logger.error("Can not create Order book as Symbol does not exists");
			return null;
		}

		OrderBook orderBook = new OrderBook(instrument.getId());
		orderBooks.put(instrument.getId(), orderBook);
		return orderBook;

	}

	@Override
	public OrderBook getOrderBook(int instrumentId) {
		return fetchOrderBook(instrumentId);
	}

	@Override
	public Order createLimitOrder(int instrumentId, long quantity, double price) {
		OrderBook orderBook = fetchOrderBook(instrumentId);
		if (orderBook != null) {
			int id = orderIds.getAndIncrement();
			LimitOrder limitOrder = new LimitOrder(id, instrumentId, quantity, price);
			orders.put(id, limitOrder);
			orderBook.addOrder(limitOrder);
			return limitOrder;
		}
		return null;

	}

	@Override
	public Order createMarketOrder(int instrumentId, long quantity) {
		OrderBook orderBook = fetchOrderBook(instrumentId);
		if (orderBook != null) {
			int id = orderIds.getAndIncrement();
			MarketOrder marketOrder = new MarketOrder(id, instrumentId, quantity);
			orders.put(id, marketOrder);
			orderBook.addOrder(marketOrder);
			return marketOrder;
		}
		return null;

	}

	@Override
	public boolean openOrdersBook(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.open();
		return false;
	}

	@Override
	public boolean closeOrdersBook(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.close();
		return false;
	}

	@Override
	public long getAllValidOrdersQuantity(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.getValidOrderQuantity();
		return 0;
	}

	@Override
	public long getAllInvalidOrdersQuantity(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.getInvalidOrderQuantity();
		return 0;
	}

	@Override
	public Order getOrderDetail(int orderId) {
		return orders.get(orderId);
	}

	@Override
	public Order getBiggestOrder(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.getBiggestOrder();
		return null;

	}

	@Override
	public Order getSmallestOrder(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.getSmallestOrder();
		return null;
	}

	@Override
	public Order getEarliestOrder(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.getEarliestOrder();
		return null;
	}

	@Override
	public Order getLatestOrder(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.getLatestOrder();
		return null;
	}

	@Override
	public Map<Double, Long> getDemandStatistics(int instrumentId) {
		OrderBook orderbook = fetchOrderBook(instrumentId);
		if (orderbook != null)
			return orderbook.getDemandStatistics();
		return null;
	}

	private OrderBook fetchOrderBook(int instrumentId) {
		OrderBook orderbook = orderBooks.get(instrumentId);
		if (orderbook == null) {
			logger.error("No Order Book Available for placing order for instrument:" + instrumentId);
		}
		return orderbook;
	}

	@Override
	public boolean addExecution(int instrumentId, long quantity, double price) {
		OrderBook orderbook = orderBooks.get(instrumentId);
		if (orderbook != null) {
			return orderbook.addExecution(new Execution(executionIds.getAndIncrement(), instrumentId, quantity, price));
		}
		return false;
	}
}
