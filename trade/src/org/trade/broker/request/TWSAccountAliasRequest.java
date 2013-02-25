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
import org.trade.persistent.dao.TradeAccount;

import org.xml.sax.Attributes;

public class TWSAccountAliasRequest extends SaxMapper {

	private final static Logger _log = LoggerFactory
			.getLogger(TWSAccountAliasRequest.class);

	private Aspects m_target = null;
	private final Stack<Object> m_stack = new Stack<Object>();

	public TWSAccountAliasRequest() throws XMLModelException {
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

		final TagTracker accountAliasTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				_log.trace("accountAliasTracker onStart()");
				TradeAccount aspect = new TradeAccount();
				m_target.add(aspect);
				m_stack.push(aspect);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
				_log.trace("accountAliasTracker onEnd() " + contents.toString());
			}
		};

		rootTagTracker.track("ListOfAccountAliases/AccountAlias",
				accountAliasTracker);
		accountAliasTracker.track("AccountAlias", accountAliasTracker);

		final TagTracker accountTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				final TradeAccount temp = (TradeAccount) m_stack.peek();
				temp.setAccountNumber(value);
				_log.trace("accountTracker: " + value);
			}
		};

		accountAliasTracker.track("AccountAlias/account", accountTracker);
		accountTracker.track("account", accountTracker);

		final TagTracker aliasTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String value = new String(contents.toString());
				final TradeAccount temp = (TradeAccount) m_stack.peek();
				temp.setAlias(value);
				_log.trace("aliasTracker: " + value);
			}
		};

		accountAliasTracker.track("AccountAlias/alias", aliasTracker);
		aliasTracker.track("alias", aliasTracker);
		return rootTagTracker;
	}
}
