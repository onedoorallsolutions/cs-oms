package com.cs.oms.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.cs.oms.common.Execution;
import com.cs.oms.common.Instrument;
import com.cs.oms.common.LimitOrder;
import com.cs.oms.common.MarketOrder;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;
import com.cs.oms.common.OrderBookStatus;
import com.cs.oms.common.OrderStatus;
import com.cs.oms.common.OrderType;
import com.cs.oms.common.exception.OMSException;
import com.cs.oms.service.dao.OMSServiceDao;

public class OMSServiceImpl implements OMSService {
	private final static Logger logger = Logger.getLogger(OMSServiceImpl.class);
	private OMSServiceDao omsServiceDao;
	private AtomicLong orderIds = new AtomicLong(0);
	private AtomicLong executionIds = new AtomicLong(0);

	public OMSServiceDao getOmsServiceDao() {
		return omsServiceDao;
	}

	public OMSServiceImpl(OMSServiceDao omsServiceDao) {
		this.omsServiceDao = omsServiceDao;
	}

	@Override
	public boolean addLimitOrder(String symbol, long quantity, BigDecimal price) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = omsServiceDao.getOrderBook(instrumentId);
			if (orderBook != null && orderBook.getStatus() == OrderBookStatus.OPEN) {
				Order order = new LimitOrder(orderIds.incrementAndGet(), instrumentId, quantity, price);
				omsServiceDao.save(order);
				return true;
			}
			throw new OMSException("Cannot Add Orders for Instrument:" + instrumentId);
		}
		throw new OMSException("Instrument dosnr not exixts for symbol :" + symbol);
	}

	@Override
	public boolean addMarketOrder(String symbol, long quantity) throws OMSException {
		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = omsServiceDao.getOrderBook(instrumentId);
			if (orderBook != null && orderBook.getStatus() == OrderBookStatus.OPEN) {
				Order order = new MarketOrder(orderIds.incrementAndGet(), instrumentId, quantity);
				omsServiceDao.save(order);
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
				Execution execution = new Execution(executionIds.incrementAndGet(), instrumentId, quantity, price);
				omsServiceDao.save(execution);
				allocateExecution(orderBook, execution);
				return true;
			}
			throw new OMSException("Cannot Add Execution for Instrument:" + symbol);
		}

		throw new OMSException("Instrument does not exixts for Symbol :" + symbol);
	}

	private void allocateExecution(OrderBook orderBook, Execution execution) {
		long instrumentId = orderBook.getInstrumentId();
		long executionQuantity = execution.getQuantity();
		BigDecimal executionPrice = execution.getPrice();
		Set<Execution> executions = omsServiceDao.getExecutions(instrumentId);
		if (executions.size() == 1) {
			// First Execution
			omsServiceDao.getOrders(orderBook.getInstrumentId()).stream().forEach(order -> {
				if (order.getOrderType() == OrderType.LIMIT) {
					LimitOrder limitOrder = (LimitOrder) order;
					if (limitOrder.getPrice().compareTo(execution.getPrice()) < 0) {
						limitOrder.setExecutedPrice(BigDecimal.ZERO);
						limitOrder.setStatus(OrderStatus.INVALID);
						omsServiceDao.update(limitOrder);
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
				omsServiceDao.update(o);
			});

			orderBook.setStatus(OrderBookStatus.EXECUTED);
			omsServiceDao.update(orderBook);
		} else {

			validOrders.stream().forEach(o -> {

				o.setExecutedPrice(executionPrice);
				long executedQuantity = o.getExecutedQuantity();
				long bidQty = o.getQuantity();
				if (bidQty > executedQuantity) {
					long remBidQty = bidQty - executedQuantity;
					long executedQty = Math.round(((double) executionQuantity / validRemaingOrderQuantity) * remBidQty);
					o.setExecutedQuantity(executedQuantity + executedQty);
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
				omsServiceDao.update(orderBook);
				return true;
			}
			throw new OMSException("Cannot Close Order Book for Instrument:" + instrumentId);
		}
		throw new OMSException("Instrument does not exixts for Symbol :" + symbol);
	}

	@Override
	public boolean openOrderBook(String symbol) throws OMSException {

		Instrument instrument = omsServiceDao.getInstrument(symbol);
		if (instrument != null) {
			long instrumentId = instrument.getId();
			OrderBook orderBook = getOrderBook(symbol);
			if (orderBook == null) {
				omsServiceDao.save(new OrderBook(instrumentId));
				return true;
			}
			throw new OMSException("Cannot Add Open Order Book for Instrument:" + symbol);
		}
		throw new OMSException("Instrument does not exixts for Symbol :" + symbol);
	}

	public Set<Order> getAllValidOrder(long instrumentId) {
		return omsServiceDao.getOrders(instrumentId).stream().filter(o -> {
			return o.getStatus() == OrderStatus.VALID;
		}).collect(Collectors.toSet());
	}

	public Set<Order> getAllInvalidOrder(long instrumentId) {
		return omsServiceDao.getOrders(instrumentId).stream().filter(o -> {
			return o.getStatus() == OrderStatus.INVALID;
		}).collect(Collectors.toSet());
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

	private Optional<Order> getBiggestOrder(long instrumentId) {
		return getAllValidOrder(instrumentId).stream().max((o1, o2) -> {
			return Long.valueOf(o1.getQuantity()).compareTo(o2.getQuantity());
		});
	}

	private Optional<Order> getSmallestOrder(long instrumentId) {
		return getAllValidOrder(instrumentId).stream().max((o1, o2) -> {
			return Long.valueOf(o2.getQuantity()).compareTo(o1.getQuantity());
		});
	}

	private Optional<Order> getLatestOrder(long instrumentId) {
		return getAllValidOrder(instrumentId).stream().max((o1, o2) -> {
			return o1.getEntryDate().compareTo(o2.getEntryDate());
		});
	}

	private Optional<Order> getEarliestOrder(long instrumentId) {
		return getAllValidOrder(instrumentId).stream().max((o1, o2) -> {
			return o2.getEntryDate().compareTo(o1.getEntryDate());
		});
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
			long validOrderQty = getValidOrderQuantity(instrumentId);
			long invalidOrderQty = getInvalidOrderQuantity(instrumentId);
			long totalExecutedOty = getValidExecutedQuantity(instrumentId);
			Order latestOrder = null;
			Order biggestOrder = null;
			Order smallestOrder = null;
			Order earliestOrder = null;
			Optional<Order> optional = getLatestOrder(instrumentId);

			if (optional.isPresent()) {
				latestOrder = optional.get();
			}

			optional = getEarliestOrder(instrumentId);
			if (optional.isPresent()) {
				earliestOrder = optional.get();
			}

			optional = getBiggestOrder(instrumentId);
			if (optional.isPresent()) {
				biggestOrder = optional.get();
			}

			optional = getSmallestOrder(instrumentId);
			if (optional.isPresent()) {
				smallestOrder = optional.get();
			}
			logger.info("Order Book :" + omsServiceDao.getOrderBook(instrumentId));
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
		throw new OMSException("Instrument does not exixts for Symbol :" + symbol);
	}

}
