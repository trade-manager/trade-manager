/* ===========================================================
 * TradeManager : a application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.broker.client;

import java.util.concurrent.ConcurrentHashMap;

import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Tradestrategy;

public class ClientSocket {

	private static final ConcurrentHashMap<Integer, BackTestBroker> m_backTestBroker = new ConcurrentHashMap<Integer, BackTestBroker>();
	private ClientWrapper m_client = null;

	public ClientSocket(ClientWrapper client) {
		m_client = client;
	}

	public synchronized void reqHistoricalData(int tickerId, Contract contract,
			String endDateTime, String durationStr, String barSizeSetting,
			String whatToShow, int useRTH, int formatDate) {

		for (Tradestrategy tradestrategy : contract.getTradestrategies()) {
			BackTestBroker backTestBroker = new BackTestBroker(
					tradestrategy.getDatasetContainer(),
					tradestrategy.getIdTradeStrategy(), m_client);
			m_backTestBroker.put(tradestrategy.getIdTradeStrategy(),
					backTestBroker);
		}
		m_client.historicalData(contract.getIdContract(),
				"finished- at yyyyMMdd HH:mm:ss", 0, 0, 0, 0, 0, 0, 0, false);
	}

	public synchronized void removeBackTestBroker(Integer idTradestrategy) {
		BackTestBroker backTestBroker = m_backTestBroker.get(idTradestrategy);
		if (null != backTestBroker) {
			if (backTestBroker.isDone() || backTestBroker.isCancelled()) {
				m_backTestBroker.remove(idTradestrategy);
			}
		}
	}

	public BackTestBroker getBackTestBroker(Integer idTradestrategy) {
		return m_backTestBroker.get(idTradestrategy);
	}

	public synchronized void reqContractDetails(int reqId, Contract contract) {

	}

	public synchronized void reqRealTimeBars(int tickerId, Contract contract,
			int barSize, String whatToShow, boolean useRTH) {

	}
}
