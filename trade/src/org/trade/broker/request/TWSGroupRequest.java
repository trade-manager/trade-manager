package org.trade.broker.request;

import java.io.CharArrayWriter;
import java.text.ParseException;
import java.util.Stack;

import org.trade.core.dao.Aspects;
import org.trade.core.xml.SaxMapper;
import org.trade.core.xml.TagTracker;
import org.trade.core.xml.XMLModelException;
import org.trade.persistent.dao.AccountAllocation;
import org.trade.persistent.dao.FinancialAccount;

import org.xml.sax.Attributes;

public class TWSGroupRequest extends SaxMapper {

	private Aspects m_target = new Aspects();
	private final Stack<Object> m_stack = new Stack<Object>();

	public TWSGroupRequest() throws XMLModelException {
		super();
	}

	public Object getMappedObject() {
		return m_target;
	}

	public TagTracker createTagTrackerNetwork() throws ParseException {
		// -- create root: /
		final TagTracker rootTagTracker = new TagTracker() {

			public void onDeactivate() {
				/*
				 * The root will be deactivated when parsing a new document
				 * begins. clear the stack
				 */
				m_stack.removeAllElements();

				// create the root "dir" object.
				m_target = new Aspects();
			}
		};

		final TagTracker groupsTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				FinancialAccount aspect = new FinancialAccount();
				m_target.add(aspect);
				m_stack.push(aspect);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
			}
		};

		rootTagTracker.track("ListOfGroups/Group", groupsTracker);
		groupsTracker.track("Group", groupsTracker);

		final TagTracker nameTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				final FinancialAccount temp = (FinancialAccount) m_stack.peek();
				temp.setGroupName(value);
			}
		};

		groupsTracker.track("Group/name", nameTracker);
		nameTracker.track("name", nameTracker);

		final TagTracker methodTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				final FinancialAccount temp = (FinancialAccount) m_stack.peek();
				temp.setMethod(value);
			}
		};

		groupsTracker.track("Group/defaultMethod", methodTracker);
		methodTracker.track("defaultMethod", methodTracker);

		final TagTracker listOfAcctsTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				final FinancialAccount temp = (FinancialAccount) m_stack.peek();
				m_stack.push(temp);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
			}
		};

		groupsTracker.track("Group/ListOfAccts", listOfAcctsTracker);
		listOfAcctsTracker.track("ListOfAccts", listOfAcctsTracker);

		final TagTracker accountTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				AccountAllocation aspect = new AccountAllocation();
				aspect.setAccountNumber(value);
				final FinancialAccount temp = (FinancialAccount) m_stack.peek();
				aspect.setFinancialAccount(temp);
				temp.getAccountAllocation().add(aspect);
			}
		};

		listOfAcctsTracker.track("ListOfAccts/String", accountTracker);
		accountTracker.track("String", accountTracker);
		return rootTagTracker;
	}
}
