package de.keithpaterson.loganair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class CustomProxySelector extends ProxySelector {

	private final ProxySelector def;

	private Proxy proxy;

	private static final Logger logger = Logger.getLogger(CustomProxySelector.class.getName());

	private List<Proxy> proxyList = new ArrayList<Proxy>();

	/**
	 * CT
	 */
	public CustomProxySelector(String proxyHost, int port) {
		this.def = ProxySelector.getDefault();
		proxy = new Proxy(Proxy.Type.HTTP,
				new InetSocketAddress(proxyHost, port));
		proxyList.add(proxy);
		ProxySelector.setDefault(this);
	}

	@Override
	public List<Proxy> select(URI uri) {
		logger.info("Trying to reach URL : " + uri);
		if (uri == null) {
			throw new IllegalArgumentException("URI can't be null.");
		}
		if (uri.getHost().contains("loganair")) {
			logger.info("We're trying to reach loganair so we're going to use the extProxy.");
			return proxyList;
		}
		return def.select(uri);
	}

	/**
	 * Method called by the handlers when it failed to connect to one of the
	 * proxies returned by select().
	 */
	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		logger.severe("Failed to connect to a proxy when connecting to " + uri.getHost());
		if (uri == null || sa == null || ioe == null) {
			throw new IllegalArgumentException("Arguments can't be null.");
		}
		def.connectFailed(uri, sa, ioe);
	}
}