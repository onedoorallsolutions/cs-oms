package com.cs.oms.common;

import java.math.BigDecimal;

public class Execution {
	private final long id;
	private final long instrumentId;
	private final long quantity;
	private final BigDecimal price;

	public Execution(long id, long instrumentId, long quantity, BigDecimal price) {
		this.instrumentId = instrumentId;
		this.quantity = quantity;
		this.price = price;
		this.id = id;
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

	public BigDecimal getPrice() {
		return price;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (instrumentId ^ (instrumentId >>> 32));
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result + (int) (quantity ^ (quantity >>> 32));
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
		Execution other = (Execution) obj;
		if (id != other.id)
			return false;
		if (instrumentId != other.instrumentId)
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		if (quantity != other.quantity)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Execution [id=" + id + ", instrumentId=" + instrumentId + ", quantity=" + quantity + ", price=" + price
				+ "]";
	}

}
