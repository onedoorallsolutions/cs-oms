package com.cs.oms.service.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.cs.oms.common.Execution;
import com.cs.oms.common.Instrument;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;

public class OMSServiceDaoImpl implements OMSServiceDao {
	private volatile static OMSServiceDaoImpl theInstance = null;
	private Set<Order> orders = new HashSet<>();
	private Set<OrderBook> orderBooks = new HashSet<>();
	private Set<Execution> executions = new HashSet<>();
	private Map<String, Instrument> instrumentMap = new HashMap<>();

	private OMSServiceDaoImpl() {

	}

	@Override
	public void save(Order order) {
		orders.add(order);
	}

	@Override
	public void update(Order order) {
		if (orders.contains(order)) {
			orders.add(order);
		}

	}

	@Override
	public void save(Execution execution) {
		executions.add(execution);
	}

	@Override
	public void update(OrderBook orderBook) {
		if (orderBooks.contains(orderBooks)) {
			orderBooks.add(orderBook);
		}
	}

	@Override
	public void save(OrderBook orderBook) {
		orderBooks.add(orderBook);
	}

	@Override
	public Set<Execution> getExecutions(long instrumentId) {
		return executions.stream().filter(e -> e.getInstrumentId() == instrumentId)
				.collect(Collectors.toSet());
	}

	@Override
	public Set<Order> getOrders(long instrumentId) {
		return orders.stream().filter(o -> o.getInstrumentId() == instrumentId)
				.collect(Collectors.toSet());
	}

	public static OMSServiceDaoImpl getInstance() {
		if (theInstance == null) {
			synchronized (OMSServiceDaoImpl.class) {
				if (theInstance == null) {
					theInstance = new OMSServiceDaoImpl();
				}
			}
		}
		return theInstance;
	}

	@Override
	public OrderBook getOrderBook(long instrumentId) {
		Optional<OrderBook> optional = orderBooks.stream().filter(e -> e.getInstrumentId() == instrumentId)
				.findFirst();

		if (optional.isPresent()) {
			return optional.get();
		} else {
			return null;
		}
	}

	@Override
	public Instrument getInstrument(String symbol) {
		return instrumentMap.get(symbol);
	}

	@Override
	public Order getOrder(long orderId) {
		Optional<Order> optional = orders.stream().filter(o -> o.getId() == orderId).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	@Override
	public void loadAllInstrument(Map<String, Instrument> instrumentMap) {
		this.instrumentMap.putAll(instrumentMap);
	}

}
