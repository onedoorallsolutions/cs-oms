package com.cs.oms.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cs.oms.common.Execution;
import com.cs.oms.common.Instrument;
import com.cs.oms.common.LimitOrder;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;
import com.cs.oms.common.OrderBookStatus;
import com.cs.oms.common.OrderStatus;
import com.cs.oms.common.OrderType;
import com.cs.oms.common.exception.OMSException;
import com.cs.oms.dao.OMSServiceDao;

public class OMSServiceImpl implements OMSService {
	private final static Logger logger = Logger.getLogger(OMSServiceImpl.class);
	private OMSServiceDao omsServiceDao;

	public OMSServiceDao getOmsServiceDao() {
		return omsServiceDao;
	}

	public OMSServiceImpl(OMSServiceDao omsServiceDao) {
		this.omsServiceDao = omsServiceDao;
	}

	@Override
	public boolean addOrder(String symbol, long quantity, BigDecimal price) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = omsServiceDao.getOrderBook(instrumentId);
			if (orderBook != null && orderBook.getStatus() == OrderBookStatus.OPEN) {
				if (omsServiceDao.createOrder(instrumentId, quantity, price))
					return true;
			}
			throw new OMSException("Cannot Add Orders for Instrument:" + instrumentId);
		}
		throw new OMSException("Instrument dosnr not exixts for symbol :" + symbol);
	}

	@Override
	public boolean addOrder(String symbol, long quantity) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = omsServiceDao.getOrderBook(instrumentId);
			if (orderBook != null && orderBook.getStatus() == OrderBookStatus.OPEN) {
				if (omsServiceDao.createOrder(instrumentId, quantity))
					return true;
			}
			throw new OMSException("Cannot Add Orders for Instrument:" + symbol);
		}
		throw new OMSException("Instrument does not exixts for Symbol :" + symbol);
	}

	@Override
	public boolean addExecution(String symbol, long quantity, BigDecimal price) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = omsServiceDao.getOrderBook(instrumentId);
			if (orderBook != null && orderBook.getStatus() == OrderBookStatus.CLOSE) {
				if (omsServiceDao.createExecution(instrumentId, quantity, price)) {
					allocateExecution(orderBook, quantity, price);
					return true;
				}
			}
			throw new OMSException("Cannot Add Execution for Instrument:" + symbol);
		}

		throw new OMSException("Instrument does not exixts for Symbol :" + symbol);
	}

	private void allocateExecution(OrderBook orderBook, long executionQuantity, BigDecimal executionPrice) {
		long instrumentId = orderBook.getInstrumentId();
		Set<Execution> executions = omsServiceDao.getExecutions(instrumentId);
		if (executions.size() == 1) {
			// First Execution
			omsServiceDao.getOrders(orderBook.getInstrumentId()).stream().forEach(order -> {
				if (order.getOrderType() == OrderType.LIMIT) {
					LimitOrder limitOrder = (LimitOrder) order;
					if (limitOrder.getPrice().compareTo(executionPrice) < 0) {
						limitOrder.setExecutedPrice(BigDecimal.ZERO);
						limitOrder.setStatus(OrderStatus.INVALID);
						omsServiceDao.updateOrder(order);
					}
				}
			});
		}

		long totalValidQty = getValidOrderQuantity(instrumentId);
		long totalExecutedQty = getValidExecutedQuantity(instrumentId);
		long validRemaingOrderQuantity = totalValidQty - totalExecutedQty;
		Set<Order> validOrders = getAllValidOrder(instrumentId);

		if (validRemaingOrderQuantity <= executionQuantity) {
			validOrders.stream().forEach(o -> {
				o.setExecutedPrice(executionPrice);
				o.setExecutedQuantity(o.getQuantity());
				omsServiceDao.updateOrder(o);
			});

			orderBook.setStatus(OrderBookStatus.EXECUTED);
			omsServiceDao.updateOrderBook(orderBook);
		} else {

			validOrders.stream().forEach(o -> {
				o.setExecutedPrice(executionPrice);
				long executedQuantity = o.getExecutedQuantity();
				long bidQty = o.getQuantity();
				if (bidQty > executedQuantity) {
					long remBidQty = bidQty - executedQuantity;
					long executedQty = Math.round(((double) executionQuantity / validRemaingOrderQuantity) * remBidQty);
					o.setExecutedQuantity(executedQuantity + executedQty);
					omsServiceDao.updateOrder(o);
				}
			});

		}

	}

	@Override
	public boolean closeOrderBook(String symbol) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = omsServiceDao.getOrderBook(instrumentId);
			if (orderBook != null && orderBook.getStatus() == OrderBookStatus.OPEN) {
				orderBook.setStatus(OrderBookStatus.CLOSE);
				if (omsServiceDao.updateOrderBook(orderBook))
					return true;
			}
			throw new OMSException("Cannot Close Order Book for Instrument:" + instrumentId);
		}
		throw new OMSException("Instrument does not exist for Symbol :" + symbol);
	}

	@Override
	public boolean openOrderBook(String symbol) throws OMSException {

		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = getOrderBook(symbol);
			if (orderBook == null) {
				if (omsServiceDao.createOrderBook(instrumentId))
					return true;
			}
			throw new OMSException("Cannot Add Open Order Book for Instrument:" + symbol);
		}
		throw new OMSException("Instrument does not exist for Symbol :" + symbol);
	}

	public Set<Order> getAllValidOrder(long instrumentId) {
		return omsServiceDao.getValidOrders(instrumentId);
	}

	public Set<Order> getAllInvalidOrder(long instrumentId) {
		return omsServiceDao.getInvalidOrders(instrumentId);
	}

	public long getValidOrderQuantity(long instrumentId) {
		return getAllValidOrder(instrumentId).stream().mapToLong(o -> o.getQuantity()).sum();
	}

	public long getInvalidOrderQuantity(long instrumentId) {
		return getAllInvalidOrder(instrumentId).stream().mapToLong(o -> o.getQuantity()).sum();
	}

	private long getValidExecutedQuantity(long instrumentId) {
		return getAllValidOrder(instrumentId).stream().mapToLong(o -> o.getExecutedQuantity()).sum();
	}

	private Map<BigDecimal, Long> getDemandStatisctics(long instrumentId) {
		Map<BigDecimal, Long> table = new HashMap<>();
		getAllValidOrder(instrumentId).stream().filter(o -> o.getOrderType() == OrderType.LIMIT).forEach(o -> {
			LimitOrder limitOrder = (LimitOrder) o;
			Long qty = table.get(limitOrder.getPrice());
			if (qty == null) {
				table.put(limitOrder.getPrice(), limitOrder.getQuantity());
			} else {
				qty = qty + limitOrder.getQuantity();
				table.put(limitOrder.getPrice(), qty);
			}
		});

		getAllInvalidOrder(instrumentId).stream().filter(o -> o.getOrderType() == OrderType.LIMIT).forEach(o -> {
			LimitOrder limitOrder = (LimitOrder) o;
			Long qty = table.get(limitOrder.getPrice());
			if (qty == null) {
				table.put(limitOrder.getPrice(), limitOrder.getQuantity());
			} else {
				qty = qty + limitOrder.getQuantity();
				table.put(limitOrder.getPrice(), qty);
			}
		});

		return table;
	}

	public void printAllStatistics(String symbol) {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();

			logger.info("--------------------------------------");
			OrderBook orderBook = omsServiceDao.getOrderBook(instrumentId);
			logger.info("Order Book :" + orderBook);
			if (orderBook != null) {
				long validOrderQty = getValidOrderQuantity(instrumentId);
				long invalidOrderQty = getInvalidOrderQuantity(instrumentId);
				long totalExecutedOty = getValidExecutedQuantity(instrumentId);
				Order latestOrder = omsServiceDao.getLatestOrder(instrumentId);
				Order earliestOrder = omsServiceDao.getEarliestOrder(instrumentId);
				Order biggestOrder = omsServiceDao.getBiggestOrder(instrumentId);
				Order smallestOrder = omsServiceDao.getSmallestOrder(instrumentId);

				logger.info("Valid Executed Qty :" + totalExecutedOty);
				logger.info("Valid Order Qty :" + validOrderQty);
				logger.info("Invalid Order Qty :" + invalidOrderQty);
				logger.info("Latest Order :" + latestOrder);
				logger.info("Earliest Order :" + earliestOrder);
				logger.info("Biggest Order :" + biggestOrder);
				logger.info("Smallest Order :" + smallestOrder);

				Map<BigDecimal, Long> map = getDemandStatisctics(instrumentId);
				logger.info("Demand Statisctics");
				map.entrySet().stream().forEach(e -> {
					logger.info("Price :" + e.getKey() + " Qty :" + e.getValue());
				});
			}

		} else {
			logger.error("No Instrument Exists for Symbol:" + symbol);
		}
	}

	@Override
	public Order getOrder(long orderId) throws OMSException {
		return omsServiceDao.getOrder(orderId);

	}

	@Override
	public OrderBook getOrderBook(String symbol) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			return omsServiceDao.getOrderBook(instrumentId);
		}
		throw new OMSException("Instrument does not exist for Symbol :" + symbol);
	}

	@Override
	public Instrument getInstrument(String symbol) {

		return omsServiceDao.getInstrument(symbol);
	}

	@Override
	public Order getBiggestOrder(String symbol) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			return omsServiceDao.getBiggestOrder(instrument.getId());
		}
		throw new OMSException("Instrument does not exist for Symbol :" + symbol);
	}

	@Override
	public Order getSmallestOrder(String symbol) throws OMSException {

		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			return omsServiceDao.getSmallestOrder(instrument.getId());
		}
		throw new OMSException("Instrument does not exist for Symbol :" + symbol);
	}

	@Override
	public Order getLatestOrder(String symbol) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			return omsServiceDao.getLatestOrder(instrument.getId());
		}
		throw new OMSException("Instrument does not exist for Symbol :" + symbol);
	}

	@Override
	public Order getEarliestOrder(String symbol) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			return omsServiceDao.getEarliestOrder(instrument.getId());
		}
		throw new OMSException("Instrument does not exist for Symbol :" + symbol);
	}

}
