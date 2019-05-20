package com.cs.oms.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cs.oms.common.Instrument;
import com.cs.oms.common.OrderBook;
import com.cs.oms.common.OrderBookStatus;
import com.cs.oms.common.exception.OMSException;
import com.cs.oms.service.dao.OMSServiceDao;
import com.cs.oms.service.dao.OMSServiceDaoImpl;

public class OMSServiceTest {

	OMSService omsService = null;

	@Before
	public void setup() {
		OMSServiceDao omsServiceDao = OMSServiceDaoImpl.getInstance();
		omsService = new OMSServiceImpl(omsServiceDao);
		Map<String, Instrument> instrumentMap = new HashMap<>();
		AtomicLong instrumentIds = new AtomicLong(0);
		Instrument instrument1 = new Instrument(instrumentIds.incrementAndGet(), "IBM.N");
		Instrument instrument2 = new Instrument(instrumentIds.incrementAndGet(), "UBER.N");
		Instrument instrument3 = new Instrument(instrumentIds.incrementAndGet(), "APPL.N");
		Instrument instrument4 = new Instrument(instrumentIds.incrementAndGet(), "ALP.N");
		Instrument instrument5 = new Instrument(instrumentIds.incrementAndGet(), "ZIN.N");
		Instrument instrument6 = new Instrument(instrumentIds.incrementAndGet(), "CS.N");
		instrumentMap.put(instrument1.getSymbol(), instrument1);
		instrumentMap.put(instrument2.getSymbol(), instrument2);
		instrumentMap.put(instrument3.getSymbol(), instrument3);
		instrumentMap.put(instrument4.getSymbol(), instrument4);
		instrumentMap.put(instrument5.getSymbol(), instrument5);
		instrumentMap.put(instrument6.getSymbol(), instrument6);

		omsServiceDao.loadAllInstrument(instrumentMap);
	}

	@Test
	public void testAll() throws OMSException {
		Assert.assertTrue(omsService.openOrderBook("IBM.N"));
		Assert.assertTrue(omsService.openOrderBook("CS.N"));
		Assert.assertTrue(omsService.openOrderBook("ZIN.N"));
		Assert.assertTrue(omsService.openOrderBook("ALP.N"));
		Assert.assertTrue(omsService.openOrderBook("APPL.N"));
		boolean isSuccess = false;
		try {
			isSuccess = omsService.openOrderBook("IMB.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		Assert.assertFalse(isSuccess);

		try {
			isSuccess = omsService.openOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}
		Assert.assertFalse(isSuccess);
		OrderBook orderBook = omsService.getOrderBook("IBM.N");
		Assert.assertEquals(OrderBookStatus.OPEN, orderBook.getStatus());
		try {
			isSuccess = omsService.addExecution("IBM.N", 10, new BigDecimal(30.0));
		} catch (OMSException e) {
			isSuccess = false;
		}
		Assert.assertFalse(isSuccess);

		omsService.addMarketOrder("IBM.N", 10);
		omsService.addMarketOrder("IBM.N", 20);
		omsService.addLimitOrder("IBM.N", 25, new BigDecimal(34.0));
		omsService.addLimitOrder("IBM.N", 23, new BigDecimal(29.0));
		omsService.addLimitOrder("IBM.N", 50, new BigDecimal(27.0));
		omsService.addLimitOrder("IBM.N", 80, new BigDecimal(27.0));
		try {
			isSuccess = omsService.closeOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}
		Assert.assertEquals(isSuccess, true);
		orderBook = omsService.getOrderBook("IBM.N");

		Assert.assertEquals(OrderBookStatus.CLOSE, orderBook.getStatus());

		try {
			isSuccess = omsService.closeOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		Assert.assertEquals(isSuccess, false);

		try {
			isSuccess = omsService.addExecution("IBM.N", 10, new BigDecimal(30.0));
			isSuccess = omsService.addExecution("IBM.N", 20, new BigDecimal(30.0));
			isSuccess = omsService.addExecution("IBM.N", 50, new BigDecimal(30.0));
		} catch (OMSException e) {
			isSuccess = false;
		}

		Assert.assertTrue(isSuccess);

		orderBook = omsService.getOrderBook("IBM.N");
		Assert.assertEquals(OrderBookStatus.EXECUTED, orderBook.getStatus());

		try {
			isSuccess = omsService.addExecution("IBM.N", 30, new BigDecimal(30.0));
		} catch (OMSException e) {
			isSuccess = false;
		}

		Assert.assertFalse(isSuccess);

		omsService.printAllStatistics("IBM.N");

	}

}
