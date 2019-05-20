package com.cs.oms.common;

public class MarketOrder extends Order {

	public MarketOrder(long id, long instrumentId, long quantity) {
		super(OrderType.MARKET, id, instrumentId, quantity);

	}
	

	@Override
	public String toString() {
		return "MarketOrder [" + super.toString() + "]";
	}
}
