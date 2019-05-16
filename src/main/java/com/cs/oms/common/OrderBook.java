package com.cs.oms.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderBook {

	private volatile OrderBookStatus status = OrderBookStatus.NONE;
	private final int instrumentId;
	private final Set<Order> orders = new HashSet<>();
	private Set<Order> invalidOrders = null;
	private Set<Execution> executions = new HashSet<>();
	private volatile static boolean isFirstExecution = true;
	private Order earliestOrder = null;
	private Order latestOrder = null;
	private Order biggestOrder = null;
	private Order smallestOrder = null;
	private Map<Integer, Order> orderMap = new HashMap<>();
	private Map<Double, Long> demandStatistics = new HashMap<>();

	public OrderBook(int instrumentId) {
		this.instrumentId = instrumentId;
	}

	public boolean addOrder(Order order) {
		if (order.getInstrumentId() != instrumentId) {
			throw new RuntimeException(
					"Can not add order of different instrument in book for Instrument:" + instrumentId);
		}
		if (status == OrderBookStatus.OPEN) {
			orders.add(order);
			orderMap.put(order.getId(), order);
			if (earliestOrder == null) {
				earliestOrder = latestOrder = smallestOrder = biggestOrder = order;
			} else {
				latestOrder = order;
				if (biggestOrder.getQuantity() < order.getQuantity()) {
					biggestOrder = order;
				}
				if (smallestOrder.getQuantity() > order.getQuantity()) {
					smallestOrder = order;
				}
			}
			if (order.getOrderType() == OrderType.LIMIT) {
				LimitOrder limitOrder = (LimitOrder) order;
				double price = limitOrder.getPrice();
				Long qtyForThisPrice = demandStatistics.get(price);
				if (qtyForThisPrice == null) {
					demandStatistics.put(price, limitOrder.getQuantity());
				} else {
					demandStatistics.put(price, qtyForThisPrice + limitOrder.getQuantity());
				}

			}
			return true;
		} else {
			order.setStatus(OrderStatus.REJECTED);
		}
		return false;
	}

	public boolean addExecution(Execution execution) {
		if (execution.getInstrumentId() != instrumentId) {
			throw new RuntimeException(
					"Can not add execution of different instrument in book for Instrument:" + instrumentId);
		}
		if (status != OrderBookStatus.EXECUTED) {
			if (status != OrderBookStatus.CLOSE) {
				allocateExecutionQuantities(execution);
			}
			executions.add(execution);
			return true;
		} else {
			throw new RuntimeException("Can not add executions: Order Book is already closed and executed");
		}
	}

	public boolean open() {
		if (status == OrderBookStatus.NONE) {
			status = OrderBookStatus.OPEN;
			return true;
		}
		throw new RuntimeException("Book Already Opened for Instrument:" + instrumentId);
	}

	public boolean close() {
		if (status == OrderBookStatus.OPEN) {
			status = OrderBookStatus.CLOSE;
			executions.forEach(this::allocateExecutionQuantities);
			return true;
		}
		throw new RuntimeException("Book Already Closed for Instrument:" + instrumentId);
	}

	private void allocateExecutionQuantities(final Execution execution) {
		if (status == OrderBookStatus.CLOSE && status != OrderBookStatus.EXECUTED) {
			if (isFirstExecution) {
				invalidOrders = orders.stream().filter(o -> {
					if (o.getOrderType() == OrderType.LIMIT) {
						LimitOrder limitOrder = (LimitOrder) o;
						return limitOrder.getPrice() < execution.getPrice();
					}
					return false;
				}).collect(Collectors.toSet());

				orders.removeAll(invalidOrders);
				invalidOrders.stream().forEach(o -> {
					o.setExecutedPrice(0);
					o.setStatus(OrderStatus.INVALID);

				});
				isFirstExecution = false;

			}

			double price = execution.getPrice();
			long executionQuantity = execution.getQuantity();
			long validRemaingOrderQuantity = getValidOrderQuantity() - getExecutedQuantity();
			if (executionQuantity < validRemaingOrderQuantity) {
				orders.stream().forEach(o -> {
					o.setExecutedPrice(price);
					long executedQuantity = o.getExecutedQuantity();
					long bidQty = o.getQuantity();
					if (bidQty > executedQuantity) {
						long remBidQty = bidQty - executedQuantity;
						long executedQty = (long) (validRemaingOrderQuantity / executionQuantity) * remBidQty;
						o.addExecutedQuantity(executedQty);
					}

				});
			} else {
				orders.stream().forEach(o -> {
					o.setExecutedPrice(price);
					long executedQuantity = o.getExecutedQuantity();
					long bidQty = o.getQuantity();
					if (bidQty > executedQuantity) {
						long remBidQty = bidQty - executedQuantity;
						long executedQty = (long) (validRemaingOrderQuantity / executionQuantity) * remBidQty;
						o.addExecutedQuantity(executedQty);
					}

				});
			}

			if (getValidOrderQuantity() == getExecutedQuantity()) {
				status = OrderBookStatus.EXECUTED;
			}

		}
	}

	public long getValidOrderQuantity() {
		return orders.stream().mapToLong(o -> o.getQuantity()).sum();
	}

	public long getExecutedQuantity() {
		return orders.stream().mapToLong(o -> o.getExecutedQuantity()).sum();
	}

	public double getExecutionPrice() {
		OptionalDouble optional = executions.stream().mapToDouble(e -> e.getPrice()).findFirst();
		if (optional.isPresent()) {
			return optional.getAsDouble();
		} else {
			return Double.NEGATIVE_INFINITY;
		}
	}

	public long getInvalidOrderQuantity() {
		if (invalidOrders != null) {
			return invalidOrders.stream().mapToLong(o -> o.getQuantity()).sum();
		}

		return 0;
	}

	public Order getOrderDetail(int orderId) {
		return orderMap.get(orderId);
	}

	public Order getBiggestOrder() {
		return biggestOrder;
	}

	public Order getSmallestOrder() {
		return smallestOrder;
	}

	public Order getEarliestOrder() {
		return earliestOrder;
	}

	public Order getLatestOrder() {
		return latestOrder;
	}

	public Map<Double, Long> getDemandStatistics() {
		return demandStatistics;
	}

	public OrderBookStatus getStatus() {
		return status;
	}
	
	

}
