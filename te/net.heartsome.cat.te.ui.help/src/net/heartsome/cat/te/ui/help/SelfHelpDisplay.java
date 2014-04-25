package net.heartsome.cat.te.ui.help;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.base.AbstractHelpDisplay;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.HelpBaseResources;
import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;

@SuppressWarnings("restriction")
public class SelfHelpDisplay {

	private String hrefOpenedFromHelpDisplay;
	private static AbstractHelpDisplay helpDisplay;
	private static final String HELP_DISPLAY_EXTENSION_ID = "org.eclipse.help.base.display"; //$NON-NLS-1$
	private static final String HELP_DISPLAY_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	
	private static class DefaultDisplay extends AbstractHelpDisplay {
		
		public String getHelpHome(String hostname, int port, String tab) {
			String helpURL = getFramesetURL();	
			if (tab != null) {
				helpURL += "?tab=" + tab; //$NON-NLS-1$
			}
			return helpURL;
		}

		public String getHelpForTopic(String topic, String hostname, int port) {
			return getFramesetIndexURL() + "?topic=" + topic; //$NON-NLS-1$			
		}
	}
	
	/**
	 * Constructor.
	 */
	public SelfHelpDisplay() {
		super();
	}

	/**
	 * Displays help.
	 */
	public void displayHelp(boolean forceExternal) {
		displayHelpURL(null, forceExternal);
	}

	/**
	 * Displays a help resource specified as a url.
	 * <ul>
	 * <li>a URL in a format that can be returned by
	 * {@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
	 * <li>a URL query in the format format
	 * <em>key=value&amp;key=value ...</em> The valid keys are: "tab", "toc",
	 * "topic", "contextId". For example,
	 * <em>toc="/myplugin/mytoc.xml"&amp;topic="/myplugin/references/myclass.html"</em>
	 * is valid.
	 * </ul>
	 */
	public void displayHelpResource(String href, boolean forceExternal) {
		setHrefOpenedFromHelpDisplay(href);
		if (href.startsWith("/file")) { //$NON-NLS-1$
			displayHelpResource(href.substring(1), forceExternal);
			return;
		}
		if (href != null && (href.startsWith("tab=") //$NON-NLS-1$
				|| href.startsWith("toc=") //$NON-NLS-1$
				|| href.startsWith("topic=") //$NON-NLS-1$
		|| href.startsWith("contextId="))) { //$NON-NLS-1$ // assume it is a query string
			displayHelpURL(href, forceExternal);
		} else { // assume this is a topic
			if (getNoframesURL(href) == null) {
				try {
					displayHelpURL(
							"topic=" + URLEncoder.encode(href, "UTF-8"), forceExternal); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (UnsupportedEncodingException uee) {
				}
			} else if (href.startsWith("jar:") || href.startsWith("platform:")) { //$NON-NLS-1$ //$NON-NLS-2$
				// topic from a jar/workspace to display without frames
				displayHelpURL(
						getBaseURL() + "nftopic/" + getNoframesURL(href), true); //$NON-NLS-1$
			} else {
				displayHelpURL(getNoframesURL(href), true);
			}
		}
	}

	/**
	 * Display help for the a given topic and related topics.
	 * 
	 * @param context
	 *            context for which related topics will be displayed
	 * @param topic
	 *            related topic to be selected
	 */
	public void displayHelp(IContext context, IHelpResource topic,
			boolean forceExternal) {
		if (context == null || topic == null || topic.getHref() == null)
			return;
		String topicURL = getTopicURL(topic.getHref());
		displayHelpResource(topicURL, false);
		/*
		 * links tab removed 11/2007, Bug 120947
		if (getNoframesURL(topicURL) == null) {
			try {
				String url = "tab=links" //$NON-NLS-1$
						+ "&contextId=" //$NON-NLS-1$
						+ URLEncoder.encode(getContextID(context), "UTF-8") //$NON-NLS-1$
						+ "&topic=" //$NON-NLS-1$
						+ URLEncoder.encode(topicURL, "UTF-8"); //$NON-NLS-1$
				displayHelpURL(url, forceExternal);
			} catch (UnsupportedEncodingException uee) {
			}

		} else if (topicURL.startsWith("jar:file:")) { //$NON-NLS-1$
			// topic from a jar to display without frames
			displayHelpURL(
					getBaseURL() + "nftopic/" + getNoframesURL(topicURL), true); //$NON-NLS-1$
		} else {
			displayHelpURL(getNoframesURL(topicURL), true);
		}
		*/
	}

	/**
	 * Display help to search view for given query and selected topic.
	 * 
	 * @param searchQuery
	 *            search query in URL format key=value&key=value
	 * @param topic
	 *            selected from the search results
	 */
	public void displaySearch(String searchQuery, String topic,
			boolean forceExternal) {
		if (searchQuery == null || topic == null)
			return;
		if (getNoframesURL(topic) == null) {
			try {
				String url = "tab=search&" //$NON-NLS-1$
						+ searchQuery + "&topic=" //$NON-NLS-1$
						+ URLEncoder.encode(getTopicURL(topic), "UTF-8"); //$NON-NLS-1$
				displayHelpURL(url, forceExternal);
			} catch (UnsupportedEncodingException uee) {
			}

		} else {
			displayHelpURL(getNoframesURL(topic), true);
		}
	}

	/**
	 * Displays the specified url. The url can contain query parameters to
	 * identify how help displays the document
	 */
	private void displayHelpURL(String helpURL, boolean forceExternal) {
		if (!BaseHelpSystem.ensureWebappRunning()) {
			return;
		}
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
			// wait for Display to be created
			SelfDisplayUtils.waitForDisplay();
		}

		try {
			if (helpURL == null || helpURL.length() == 0) {
				helpURL = getHelpDisplay().getHelpHome( WebappManager.getHost(),  WebappManager.getPort(), null);
			} else if (helpURL.startsWith("tab=")) { //$NON-NLS-1$
				String tab = helpURL.substring("tab=".length()); //$NON-NLS-1$
				helpURL = getHelpDisplay().getHelpHome( WebappManager.getHost(),  WebappManager.getPort(), tab);	
			} else if (helpURL.startsWith("topic=")) { //$NON-NLS-1$
				String topic = helpURL.substring("topic=".length()); //$NON-NLS-1$
				helpURL = getHelpDisplay().getHelpForTopic( topic, WebappManager.getHost(),  WebappManager.getPort());	
			} 
			
			
			String systemname = System.getProperty("os.name").toUpperCase();
			if (systemname.contains("LINUX") || systemname.contains("MAC OS X")) { 
				Program.launch(helpURL);
			}else if (systemname.contains("WINDOW")) { 
				BaseHelpSystem.getHelpBrowser(forceExternal).displayURL(helpURL);
			} 
			
		} catch (Exception e) {
			HelpBasePlugin
					.logError(
							"An exception occurred while launching help.  Check the log at " + Platform.getLogFileLocation().toOSString(), e); //$NON-NLS-1$
			BaseHelpSystem.getDefaultErrorUtil()
					.displayError(
							NLS.bind(HelpBaseResources.HelpDisplay_exceptionMessage, Platform.getLogFileLocation().toOSString()));
		}
	}

	/*
	private String getContextID(IContext context) {
		if (context instanceof Context) {
			return ((Context)context).getId();
		}
		return HelpPlugin.getContextManager().addContext(context);
	}
	*/

	private static String getBaseURL() {
		return "http://" //$NON-NLS-1$
				+ WebappManager.getHost() + ":" //$NON-NLS-1$
				+ WebappManager.getPort() + "/help/"; //$NON-NLS-1$
	}

	private static String getFramesetURL() {
		String tocPath = MessageFormat.format("topic/net.heartsome.cat.te.ui.help/html/{0}/ch01.html", CommonFunction.getSystemLanguage());
		return getBaseURL() + tocPath; //$NON-NLS-1$
	}
	
	private static String getFramesetIndexURL(){
		return getBaseURL() + "index.jsp"; //$NON-NLS-1$
	}

	private String getTopicURL(String topic) {
		if (topic == null)
			return null;
		if (topic.startsWith("../")) //$NON-NLS-1$
			topic = topic.substring(2);
		/*
		 * if (topic.startsWith("/")) { String base = "http://" +
		 * AppServer.getHost() + ":" + AppServer.getPort(); base +=
		 * "/help/content/help:"; topic = base + topic; }
		 */
		return topic;
	}

	/**
	 * If href contains URL parameter noframes=true return href with that
	 * paramter removed, otherwise returns null
	 * 
	 * @param href
	 * @return String or null
	 */
	private String getNoframesURL(String href) {
		if (href == null) {
			return null;
		}
		int ix = href.indexOf("?noframes=true&"); //$NON-NLS-1$
		if (ix >= 0) {
			//remove noframes=true&
			return href.substring(0, ix + 1)
					+ href.substring(ix + "?noframes=true&".length()); //$NON-NLS-1$

		}
		ix = href.indexOf("noframes=true"); //$NON-NLS-1$
		if (ix > 0) {
			//remove &noframes=true
			return href.substring(0, ix - 1)
					+ href.substring(ix + "noframes=true".length()); //$NON-NLS-1$
		}
		// can be displayed in frames
		return null;
	}

	public String getHrefOpenedFromHelpDisplay() {
		return hrefOpenedFromHelpDisplay;
	}

	public void setHrefOpenedFromHelpDisplay(String hrefOpenedFromHelpDisplay) {
		this.hrefOpenedFromHelpDisplay = hrefOpenedFromHelpDisplay;
	}
	
	private static void createHelpDisplay() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(HELP_DISPLAY_EXTENSION_ID );
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			if (extensions.length != 0) {
				// We need to pick up the non-default configuration
				IConfigurationElement[] elements = extensions[0]
						.getConfigurationElements();
				if (elements.length == 0) 
					return;
				IConfigurationElement displayElement  = elements[0];
				// Instantiate the help display
				try {
					helpDisplay = (AbstractHelpDisplay) (displayElement
							.createExecutableExtension(HELP_DISPLAY_CLASS_ATTRIBUTE));
				} catch (CoreException e) {
					HelpBasePlugin.logStatus(e.getStatus());
				}
			}
		}
	}

	private static AbstractHelpDisplay getHelpDisplay() {
    	if (helpDisplay == null) {
    		createHelpDisplay();
    	}
    	if (helpDisplay == null) {
    		helpDisplay = new DefaultDisplay();
    	}
    	return helpDisplay;
    }

}
