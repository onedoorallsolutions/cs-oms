package com.cs.oms.service.dao;

import java.util.List;
import java.util.Map;

import com.cs.oms.common.Instrument;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;

public interface OMSServiceDao {

	boolean createInstrument(String symbol);
	
	Instrument getInstrument(String symbol);

	List<Instrument> getAllInstrument();

	OrderBook createOrderBook(String symbol);
	
	OrderBook getOrderBook(int instrumentId);

	Order createLimitOrder(int instrumentId, long quantity, double price);

	Order createMarketOrder(int instrumentId, long quantity);
	
	boolean addExecution(int instrumentId, long quantity, double price);

	boolean openOrdersBook(int instrumentId);

	boolean closeOrdersBook(int instrumentId);

	long getAllValidOrdersQuantity(int instrumentId);

	long getAllInvalidOrdersQuantity(int instrumentId);

	Order getOrderDetail(int orderId);

	Order getBiggestOrder(int instrumentId);

	Order getSmallestOrder(int instrumentId);

	Order getEarliestOrder(int instrumentId);

	Order getLatestOrder(int instrumentId);

	Map<Double, Long> getDemandStatistics(int instrumentId);
}
