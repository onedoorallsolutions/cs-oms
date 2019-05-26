package com.cs.oms.common;

import java.time.Instant;

public class MarketOrder extends Order {

	public MarketOrder(long id, long instrumentId, long quantity, Instant entryDate) {
		super(id, OrderType.MARKET, instrumentId, quantity, entryDate);

	}

	@Override
	public String toString() {
		return "MarketOrder [" + super.toString() + "]";
	}
}
