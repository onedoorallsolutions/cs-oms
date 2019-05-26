package com.cs.oms.common;

import java.math.BigDecimal;
import java.time.Instant;

public abstract class Order {
	private final long id;
	private final long instrumentId;
	private final long quantity;
	private final Instant entryDate;
	private final OrderType orderType;
	private OrderStatus status = OrderStatus.VALID;
	private long executedQuantity;
	private BigDecimal executedPrice = BigDecimal.ZERO;

	public Order(long id, OrderType orderType, long instrumentId, long quantity, Instant entryDate) {
		this.id = id;
		this.orderType = orderType;
		this.instrumentId = instrumentId;
		this.quantity = quantity;
		this.entryDate = entryDate;
	}

	public long getId() {
		return id;
	}

	public long getInstrumentId() {
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

	public long getExecutedQuantity() {
		return executedQuantity;
	}

	public BigDecimal getExecutedPrice() {
		return executedPrice;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public void setExecutedQuantity(long executedQuantity) {
		this.executedQuantity = executedQuantity;
	}

	public void setExecutedPrice(BigDecimal executedPrice) {
		this.executedPrice = executedPrice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		return true;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", instrumentId=" + instrumentId + ", quantity=" + quantity + ", entryDate="
				+ entryDate + ", orderType=" + orderType + ", status=" + status + ", executedQuantity="
				+ executedQuantity + ", executedPrice=" + executedPrice + "]";
	}

}
