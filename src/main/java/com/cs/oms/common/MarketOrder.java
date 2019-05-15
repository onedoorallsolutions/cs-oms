package com.cs.oms.common;

public class MarketOrder extends Order {

	public MarketOrder(int id, int instrumentId, long quantity) {
		super(OrderType.MARKET, id, instrumentId, quantity);

	}
	

	@Override
	public String toString() {
		return "MarketOrder [base class :" + super.toString() + "]";
	}
}
