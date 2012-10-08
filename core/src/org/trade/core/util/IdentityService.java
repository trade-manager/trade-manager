/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
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
package org.trade.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.Date;

/**
 * The Identity Service component. The identity service provides globally unique
 * identities for the application.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class IdentityService {
	private static InetAddress m_localHost = null;

	/**
	 * Method create.
	 * @return String
	 * @throws IdentityServiceException
	 */
	public static String create() throws IdentityServiceException {
		// Obtain a current timestamp.
		Date date = new Date();
		// Get an identity unique within the local host.
		UID hostUniqueId = new UID();

		// Obtain the host name.
		if (m_localHost == null) {
			try {
				m_localHost = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				throw new IdentityServiceException(e);
			}
		}

		if (null == m_localHost) {
			throw new IdentityServiceException("Unable to resolve hostname.  "
					+ "Is your networking configured Properly?");
		}

		String hostName;

		hostName = m_localHost.getHostName();

		// Construct the identity and return it.
		String identity;

		identity = "AT-" + date.toString() + "-" + hostName + "-"
				+ hostUniqueId.toString();

		return (identity);
	}
}
