package com.cs.oms.common;

import java.time.Instant;

public abstract class Order {
	private final int id;
	private final int instrumentId;
	private final long quantity;
	private final Instant entryDate = Instant.now();
	private final OrderType orderType;
	private OrderStatus status = OrderStatus.VALID;
	private long executedQuantity = Long.MIN_VALUE;
	private double executedPrice = Double.NEGATIVE_INFINITY;

	public Order(OrderType orderType, int id, int instrumentId, long quantity) {
		this.orderType = orderType;
		this.id = id;
		this.instrumentId = instrumentId;
		this.quantity = quantity;
	}

	public int getId() {
		return id;
	}

	public int getInstrumentId() {
		return instrumentId;
	}

	public long getQuantity() {
		return quantity;
	}

	public Instant getEntryDate() {
		return entryDate;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public long getExecutedQuantity() {
		return executedQuantity;
	}

	public void addExecutedQuantity(long executedQuantity) {
		if (status == OrderStatus.VALID)
			this.executedQuantity += executedQuantity;
	}

	public double getExecutedPrice() {
		return executedPrice;
	}

	public void setExecutedPrice(double executedPrice) {
		this.executedPrice = executedPrice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + instrumentId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		if (id != other.id)
			return false;
		if (instrumentId != other.instrumentId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", instrumentId=" + instrumentId + ", quantity=" + quantity + ", entryDate="
				+ entryDate + ", orderType=" + orderType + ", status=" + status + ", executedQuantity="
				+ executedQuantity + ", executedPrice=" + executedPrice + "]";
	}

}
