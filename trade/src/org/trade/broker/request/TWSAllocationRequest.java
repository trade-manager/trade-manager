package org.trade.broker.request;

import java.io.CharArrayWriter;
import java.text.ParseException;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.Aspects;
import org.trade.core.xml.SaxMapper;
import org.trade.core.xml.TagTracker;
import org.trade.core.xml.XMLModelException;
import org.trade.persistent.dao.FinancialAccount;

import org.xml.sax.Attributes;

public class TWSAllocationRequest extends SaxMapper {

	private final static Logger _log = LoggerFactory
			.getLogger(TWSAllocationRequest.class);

	private Aspects m_target = null;
	private final Stack<Object> m_stack = new Stack<Object>();

	public TWSAllocationRequest() throws XMLModelException {
		super();
	}

	public Object getMappedObject() {
		return m_target;
	}

	public TagTracker createTagTrackerNetwork() throws ParseException {
		_log.trace("creating tag track network");

		// -- create root: /
		final TagTracker rootTagTracker = new TagTracker() {

			public void onDeactivate() {
				// The root will be deactivated when
				// parsing a new document begins.
				// clear the stack
				m_stack.removeAllElements();

				// create the root "dir" object.
				m_target = new Aspects();

				_log.trace("rootTagTracker onDeactivate");
				// push the root dir on the stack...
			}
		};

		final TagTracker allocationProfileTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				_log.trace("allocationProfileTracker onStart()");
				FinancialAccount aspect = new FinancialAccount();
				m_target.add(aspect);
				m_stack.push(aspect);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
				_log.trace("allocationProfileTracker onEnd() "
						+ contents.toString());
			}
		};

		rootTagTracker.track("ListOfAllocationProfiles/AllocationProfile",
				allocationProfileTracker);
		allocationProfileTracker.track("AllocationProfile",
				allocationProfileTracker);

		final TagTracker nameTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				final FinancialAccount temp = (FinancialAccount) m_stack.peek();
				temp.setProfileName(value);
				_log.trace("nameTracker: " + value);
			}
		};

		allocationProfileTracker.track("AllocationProfile/name", nameTracker);
		nameTracker.track("name", nameTracker);

		final TagTracker typeTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				final FinancialAccount temp = (FinancialAccount) m_stack.peek();
				temp.setType(new Integer(value));
				_log.trace("typeTracker: " + value);
			}
		};

		allocationProfileTracker.track("AllocationProfile/type", typeTracker);
		typeTracker.track("type", typeTracker);
		return rootTagTracker;
	}
}
