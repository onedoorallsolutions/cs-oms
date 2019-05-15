package com.cs.oms.common;

public class LimitOrder extends Order {

	private final double price;

	public LimitOrder(int id, int instrumentId, long quantity, double price) {
		super(OrderType.LIMIT, id, instrumentId, quantity);
		this.price = price;
	}

	
	public double getPrice() {
		return price;
	}


	@Override
	public String toString() {
		return "LimitOrder [price=" + price + "base class :" + super.toString() + "]";
	}

}
