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
package org.trade.ui.base;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.MaskFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.properties.ConfigProperties;

/**
 */
public class ConnectionPane extends JPanel {

	private static final long serialVersionUID = -4696247761711464150L;
	private final static Logger _log = LoggerFactory
			.getLogger(ConnectionPane.class);

	private JTextField hostTextField = new JTextField();
	private JFormattedTextField portTextField = null;
	private JFormattedTextField clientIdTextField = null;

	public ConnectionPane() {

		portTextField = new JFormattedTextField(createFormatter("####"));
		clientIdTextField = new JFormattedTextField(createFormatter("#"));

		Integer clientId = new Integer(0);
		Integer port = new Integer(7496);
		String host = "localhost";
		try {
			clientId = new Integer(
					ConfigProperties.getPropAsString("trade.tws.clientId"));
			port = new Integer(
					ConfigProperties.getPropAsString("trade.tws.port"));
			host = ConfigProperties.getPropAsString("trade.tws.host");
		} catch (Exception ex) {
			_log.error("Could not find config.properties in root Dir", ex);
		}
		hostTextField.setText(host);
		portTextField.setText(port.toString());
		clientIdTextField.setText(clientId.toString());
		GridBagLayout gridBagLayout1 = new GridBagLayout();
		JPanel jPanel1 = new JPanel(gridBagLayout1);
		this.setLayout(new BorderLayout());
		JLabel jLabel1 = new JLabel("Host:");
		jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel1.setHorizontalTextPosition(SwingConstants.RIGHT);
		JLabel jLabel2 = new JLabel("Port:");
		jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel2.setHorizontalTextPosition(SwingConstants.RIGHT);
		JLabel jLabel3 = new JLabel("Client Id:");
		jLabel3.setHorizontalTextPosition(SwingConstants.RIGHT);
		jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);

		jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 0, 0), 20, 5));
		jPanel1.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						1, 0, 0), 20, 5));
		jPanel1.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						1, 0, 0), 20, 5));

		jPanel1.add(hostTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 43), 196, 0));
		jPanel1.add(portTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(1, 0, 0, 43), 196, 0));
		jPanel1.add(clientIdTextField, new GridBagConstraints(1, 2, 1, 1, 1.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 43), 196, 0));
		this.add(jPanel1);
	}

	/**
	 * Method getHost.
	 * @return String
	 */
	public String getHost() {
		return hostTextField.getText();
	}

	/**
	 * Method getPort.
	 * @return Integer
	 */
	public Integer getPort() {
		return new Integer(portTextField.getText());
	}

	/**
	 * Method getClientId.
	 * @return Integer
	 */
	public Integer getClientId() {
		return new Integer(clientIdTextField.getText());
	}

	/**
	 * Method createFormatter.
	 * @param s String
	 * @return MaskFormatter
	 */
	private MaskFormatter createFormatter(String s) {
		MaskFormatter formatter = null;
		try {
			formatter = new MaskFormatter(s);
		} catch (ParseException exc) {
			_log.error("Error creating formatter: " + exc.getMessage());
		}
		return formatter;
	}
}
