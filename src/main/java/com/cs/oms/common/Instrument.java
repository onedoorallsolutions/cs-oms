package com.cs.oms.common;

public class Instrument {
	private int id;
	private String symbol;

	public Instrument(int id, String symbol) {
		this.id = id;
		this.symbol = symbol;
	}

	public int getId() {
		return id;
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return "Instrument [id=" + id + ", symbol=" + symbol + "]";
	}

}
