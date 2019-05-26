package com.cs.oms.common;

import java.math.BigDecimal;
import java.time.Instant;

public class LimitOrder extends Order {

	private final BigDecimal price;

	public LimitOrder(long id, long instrumentId, long quantity, BigDecimal price, Instant entryDate) {
		super(id, OrderType.LIMIT, instrumentId, quantity, entryDate);
		this.price = price;
	}

	public BigDecimal getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "LimitOrder [price=" + price + "," + super.toString() + "]";
	}

}
