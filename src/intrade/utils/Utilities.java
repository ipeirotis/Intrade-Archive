/*
 * Created on Mar 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package intrade.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * @author Panos Ipeirotis
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Utilities {

	private static final char c[] = { '<', '>', '&', '\"' };
	private static final String expansion[] = { "&lt;", "&gt;", "&amp;",
			"&quot;" };

	public static Document getThrottledURL(String url)
			throws FactoryConfigurationError {
		byte[] page = null;

		boolean done = false;
		int trial = 0;
		do {
			page = Utilities.getFile(url);
			if (page == null && trial < 3) {
				Utilities.sleep(2);
				trial++;
			} else {
				done = true;
			}
		} while (!done);

		Document d;
		if (page == null) {
			System.out.println("Error:" + url);
			// Utilities.sleep(5);
			d = null;
		} else {
			d = Utilities.getXMLFromString(page);
		}
		return d;
	}

	public static String cleanLine(String line) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < line.length(); i++) {
			char c1 = line.charAt(i);
			if (c1 < 128 && Character.isLetter(c1)) {
				buffer.append(c1);
			} else {
				buffer.append(' ');
			}
		}
		return buffer.toString().toLowerCase();
	}

	public static String cleanForXML(String line) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < line.length(); i++) {
			char c1 = line.charAt(i);
			if (c1 < 128 && Character.isLetter(c1)) {
				buffer.append(c1);
			} else {
				buffer.append('_');
			}
		}
		return buffer.toString();
	}

	public static TreeSet<String> getWords(String TextFile) {
		TreeSet<String> result = new TreeSet<String>();
		StringTokenizer st = new StringTokenizer(TextFile);
		while (st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		return result;
	}

	public static byte[] getFile(String URLName) {

		try {
			URL url = new URL(URLName);
			URLFetchService u = URLFetchServiceFactory.getURLFetchService();
			HTTPResponse r = u.fetch(url);

			return r.getContent();

		} catch (MalformedURLException e) {
			System.err.println("Malformed URL:" + URLName);
			e.printStackTrace();
			return null;

		} catch (IOException e) {
			System.err.println("I/O exception:" + e.getMessage());
			e.printStackTrace();
			return null;

		} catch (com.google.appengine.api.urlfetch.ResponseTooLargeException e) {
			System.err
					.println("Response Too Large Exception:" + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			return null;

		}
	}

	public static String getPage(String URLName) {
		StringBuffer buffer = new StringBuffer();

		try {
			URL url = new URL(URLName);

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append('\n');
			}
			reader.close();

		} catch (MalformedURLException e) {
			System.err.println("Malformed URL:" + URLName);
			e.printStackTrace();
			return null;

		} catch (IOException e) {
			System.err.println("I/O exception:" + e.getMessage());
			e.printStackTrace();
			return null;

		} catch (com.google.appengine.api.urlfetch.ResponseTooLargeException e) {
			System.err
					.println("Response Too Large Exception:" + e.getMessage());
			e.printStackTrace();
			return null;
		}

		return buffer.toString();
	}

	public static void sleep(int secs) {
		try {
			Thread.sleep(secs * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper function. It reads an XML file and returns the in-memory
	 * representation
	 * 
	 * It accepts only valid documents
	 * 
	 * @param file
	 * @return the XML in-memory representation of the string
	 * @throws FactoryConfigurationError
	 */
	public static Document getXMLFromString(byte[] file)
			throws FactoryConfigurationError {

		Document MIQuery = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			// factory.setValidating(true);
			// Amazon does not put a DTD
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			factory.setCoalescing(true);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new org.xml.sax.ErrorHandler() {

				// ignore fatal errors (an exception is guaranteed)
				public void fatalError(SAXParseException exception) {

					System.out.println("** Error" + ", line "
							+ exception.getLineNumber() + ", uri "
							+ exception.getSystemId());
					System.out.println("   " + exception.getMessage());
				}

				// treat validation errors as fatal
				public void error(SAXParseException e) throws SAXParseException {

					System.out.println("** Error" + ", line "
							+ e.getLineNumber() + ", uri " + e.getSystemId());
					System.out.println("   " + e.getMessage());
					throw e;
				}

				// dump warnings too
				public void warning(SAXParseException err) {

					System.out.println("** Warning" + ", line "
							+ err.getLineNumber() + ", uri "
							+ err.getSystemId());
					System.out.println("   " + err.getMessage());
				}
			});
			InputSource inputSource = new InputSource(new StringReader(
					new String(file)));
			MIQuery = builder.parse(inputSource);
		} catch (SAXException sxe) {
			// Error generated during parsing
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
		} catch (FactoryConfigurationError fce) {
			// Factory configuration error
			fce.printStackTrace();
		}
		return MIQuery;
	}

	/**
	 * Helper function. It reads an XML file and returns the in-memory
	 * representation
	 * 
	 * It accepts only valid documents
	 * 
	 * @param MIxmlQuery
	 * @return an empty XML document
	 * @throws FactoryConfigurationError
	 */
	public static Document getXML() throws FactoryConfigurationError {

		Document MIQuery = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			// factory.setValidating(true);
			// Amazon does not put a DTD
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			factory.setCoalescing(true);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new org.xml.sax.ErrorHandler() {

				// ignore fatal errors (an exception is guaranteed)
				public void fatalError(SAXParseException exception) {

					System.out.println("** Error" + ", line "
							+ exception.getLineNumber() + ", uri "
							+ exception.getSystemId());
					System.out.println("   " + exception.getMessage());
				}

				// treat validation errors as fatal
				public void error(SAXParseException e) throws SAXParseException {

					System.out.println("** Error" + ", line "
							+ e.getLineNumber() + ", uri " + e.getSystemId());
					System.out.println("   " + e.getMessage());
					throw e;
				}

				// dump warnings too
				public void warning(SAXParseException err) {

					System.out.println("** Warning" + ", line "
							+ err.getLineNumber() + ", uri "
							+ err.getSystemId());
					System.out.println("   " + err.getMessage());
				}
			});

			MIQuery = builder.newDocument();
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
		} catch (FactoryConfigurationError fce) {
			// Factory configuration error
			fce.printStackTrace();
		}
		return MIQuery;
	}

	// This method writes a DOM document to a file
	public static void writeXmlFile(Document doc, String filename) {
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			// Prepare the output file
			File file = new File(filename);
			Result result = new StreamResult(file);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
	}

	// This method writes a DOM document to a file
	public static void writeXmlFile(Document doc, File file) {
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			// Prepare the output file
			Result result = new StreamResult(file);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
	}

	// This method retuns an XML string from the DOM document
	public static String writeXmlString(Document doc) {

		StringWriter sw = new StringWriter();

		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			Result result = new StreamResult(sw);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
		return sw.toString();

	}

	public static String httpget(String url) {
		try {
			StringBuffer sb = new StringBuffer();
			URL href = new URL(url);
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection hc = (HttpURLConnection) href.openConnection();
			String ua = "Mozilla/4.0 (compatible; MSIE 6.0; WINDOWS; .NET CLR 1.1.4322)";
			hc.setRequestProperty("user-agent", ua);
			hc.setRequestMethod("GET");
			hc.connect();

			InputStream is = hc.getInputStream();
			int i;
			while ((i = is.read()) != -1) {
				char c1 = (char) i;
				sb.append(c1);
			}
			is.close();
			hc.disconnect();
			return new String(sb);
		} catch (Exception e) {
			return null;
		}
	}

	public static String HTMLEncode(String s) {
		StringBuffer st = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			boolean copy = true;
			char ch = s.charAt(i);
			for (int j = 0; j < c.length; j++) {
				if (c[j] == ch) {
					st.append(expansion[j]);
					copy = false;
					break;
				}
			}
			if (copy)
				st.append(ch);
		}
		return st.toString();
	}

}