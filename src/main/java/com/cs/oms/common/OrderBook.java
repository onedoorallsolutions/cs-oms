package com.cs.oms.common;

public class OrderBook {

	private volatile OrderBookStatus status = null;
	private final long instrumentId;

	public OrderBook(long instrumentId) {
		this.instrumentId = instrumentId;
		this.status = OrderBookStatus.OPEN;
	}

	public long getInstrumentId() {
		return instrumentId;
	}

	public OrderBookStatus getStatus() {
		return status;
	}

	public void setStatus(OrderBookStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (instrumentId ^ (instrumentId >>> 32));
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
		OrderBook other = (OrderBook) obj;
		if (instrumentId != other.instrumentId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OrderBook [status=" + status + ", instrumentId=" + instrumentId + "]";
	}

}
