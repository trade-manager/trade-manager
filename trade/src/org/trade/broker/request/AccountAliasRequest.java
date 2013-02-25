package org.trade.broker.request;

import java.io.CharArrayWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.xml.SaxMapper;
import org.trade.core.xml.TagTracker;
import org.trade.core.xml.XMLModelException;
import org.trade.persistent.dao.FinancialAccount;

import org.xml.sax.Attributes;

public class AccountAliasRequest extends SaxMapper {

	private final static Logger _log = LoggerFactory
			.getLogger(AccountAliasRequest.class);

	private FinancialAccount m_target = null;

	private static final String DATEFORMAT = new String(
			"yyyy-MM-dd'T'hhmmss'Z'");
	private static SimpleDateFormat m_sdf = new SimpleDateFormat(DATEFORMAT);
	private final Stack<Object> m_stack = new Stack<Object>();

	public AccountAliasRequest() throws XMLModelException {
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
				m_target = new FinancialAccount();

				_log.trace("rootTagTracker onDeactivate");
				// push the root dir on the stack...
			}
		};

		final TagTracker financialAccountTracker = new TagTracker() {
			@Override
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				// Capture the directory name...
				_log.trace("financialAccountTracker onStart()");

			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				_log.trace("financialAccountTrackerTracker onEnd() "
						+ contents.toString());
			}
		};

		rootTagTracker.track("ListOfAccountAliases/AccountAlias",
				financialAccountTracker);
		financialAccountTracker.track("AccountAlias", financialAccountTracker);
		// -- create action /listing/directory and directory
		final TagTracker accountAlias = new TagTracker() {

			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
				_log.trace("AccountAlias onStart()");
				// Log a trace message...
				_log.trace("Creating AccountAlias: ");
				m_stack.push(m_target);
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				// Clean up the directory stack...
				m_stack.pop();
				_log.trace("AccountAlias onEnd() " + contents.toString());
			}
		};

		financialAccountTracker.track("AccountAlias/account", accountAlias);
		accountAlias.track("account", accountAlias);

		final TagTracker aliasTracker = new TagTracker() {
			public void onStart(String namespaceURI, String localName,
					String qName, Attributes attr) {
			}

			public void onEnd(String namespaceURI, String localName,
					String qName, CharArrayWriter contents) {
				final String alias = contents.toString();
				final FinancialAccount temp = (FinancialAccount) m_stack.peek();
				temp.setGroupName(alias);
			}
		};
		return rootTagTracker;
	}
}
