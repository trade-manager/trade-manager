package org.trade.broker.request;

import java.io.CharArrayWriter;
import java.text.ParseException;
import java.util.Stack;
import org.trade.core.dao.Aspects;
import org.trade.core.xml.SaxMapper;
import org.trade.core.xml.TagTracker;
import org.trade.core.xml.XMLModelException;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.PortfolioAccount;

import org.xml.sax.Attributes;

public class TWSAllocationRequest extends SaxMapper {
	private Aspects m_target = null;
	private final Stack<Object> m_stack = new Stack<Object>();

	public TWSAllocationRequest() throws XMLModelException {
		super();
	}

	public Object getMappedObject() {
		return m_target;
	}

	public TagTracker createTagTrackerNetwork() throws ParseException {
		// -- create root: /
		final TagTracker rootTagTracker = new TagTracker() {

			public void onDeactivate() {
				// The root will be deactivated when
				// parsing a new document begins.
				// clear the stack
				m_stack.removeAllElements();

				// create the root "dir" object.
				m_target = new Aspects();
				// push the root dir on the stack...
			}
		};

		final TagTracker allocationProfileTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				Portfolio aspect = new Portfolio();
				m_target.add(aspect);
				m_stack.push(aspect);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
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
				final Portfolio temp = (Portfolio) m_stack.peek();
				temp.setName(value);
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
				final Portfolio temp = (Portfolio) m_stack.peek();
				temp.setAllocationMethod(value);
			}
		};
		allocationProfileTracker.track("AllocationProfile/type", typeTracker);
		typeTracker.track("type", typeTracker);

		final TagTracker listOfAllocationsTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				final Portfolio temp = (Portfolio) m_stack.peek();
				m_stack.push(temp);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
			}
		};

		allocationProfileTracker.track("AllocationProfile/ListOfAllocations",
				listOfAllocationsTracker);
		listOfAllocationsTracker.track("ListOfAllocations",
				listOfAllocationsTracker);

		final TagTracker allocationTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				final Portfolio portfolio = (Portfolio) m_stack.peek();
				PortfolioAccount temp = new PortfolioAccount(portfolio,
						new Account());
				portfolio.getPortfolioAccounts().add(temp);
				m_stack.push(temp);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
			}
		};

		allocationProfileTracker.track("ListOfAllocations/Allocation",
				allocationTracker);
		allocationTracker.track("Allocation", allocationTracker);

		final TagTracker acctTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				PortfolioAccount temp = (PortfolioAccount) m_stack.peek();
				temp.getAccount().setAccountNumber(value);
			}
		};
		allocationTracker.track("Allocation/acct", acctTracker);
		acctTracker.track("acct", acctTracker);

		final TagTracker amountTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// final String value = new String(contents.toString());
				// final PortfolioAccount aspect = (PortfolioAccount) m_stack
				// .peek();
				// aspect.setAmount(new BigDecimal(value));
			}
		};
		allocationTracker.track("Allocation/amount", amountTracker);
		amountTracker.track("amount", amountTracker);

		final TagTracker posEffTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// final String value = new String(contents.toString());
				// PortfolioAccount temp = (PortfolioAccount) m_stack.peek();
				// .peek();
				// temp.setPosEff(value);
			}
		};
		allocationTracker.track("Allocation/posEff", posEffTracker);
		posEffTracker.track("posEff", posEffTracker);

		return rootTagTracker;
	}
}
