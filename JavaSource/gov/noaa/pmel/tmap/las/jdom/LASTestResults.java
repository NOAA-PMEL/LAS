package gov.noaa.pmel.tmap.las.jdom;

import gov.noaa.pmel.tmap.las.exception.LASException;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class LASTestResults extends LASDocument {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -854538547489836923L;
	
	public LASTestResults() {
		super();
		setRootElement(new Element("tests"));
	}
	public void putTest(String name, long date) {
		Element existingTest = findTest(name);
		if ( existingTest != null ) {
			removeContent(existingTest);
		}
		Element test = new Element("test");
		test.setAttribute("name", name);
		test.setAttribute("date", Long.toString(date));
		getRootElement().addContent(test);
	}
	public void putDataset(String testName, String name, String dsID) throws LASException {
		Element existingTest = findTest(testName);
		if ( existingTest != null ) {
		    removeDataset(existingTest, dsID);
		} else {
			throw new LASException("Illegal state.  Attempt to add a data set to a test that does not exist.  Create the test first.");
		}
		Element dataset = new Element("dataset");
		dataset.setAttribute("name", name);
		dataset.setAttribute("ID", dsID);
		existingTest.addContent(dataset);
	}
	public void addProductResult(String testName, String dsID, String url, String view, String product, String time_step, String result) {
		Element dataset = findOrAddDataset(testName, dsID);
		Element resultE = new Element("result");
		resultE.setAttribute("url", url);
		resultE.setAttribute("result", result);
		resultE.setAttribute("view", view);
		resultE.setAttribute("product", product);
		resultE.setAttribute("time_step", time_step);
		dataset.addContent(resultE);
	}
	public void addResult(String testName, String dsID, String url, String result) {	
		Element dataset = findOrAddDataset(testName, dsID);
		Element resultE = new Element("result");
		resultE.setAttribute("url", url);
		resultE.setAttribute("result", result);
		dataset.addContent(resultE);
	}
	private Element findOrAddDataset(String testName, String dsID) {
		Element mytest = findTest(testName);
		if ( mytest == null ) {
			mytest = new Element("test");
			mytest.setAttribute("name", testName);
			addContent(mytest);
		}
		Element dataset = findDataset(mytest, dsID);
		if ( dataset == null ) {
			dataset = new Element("dataset");
			mytest.addContent(dataset);
		}
		return dataset;
	}
	private Element findDataset(Element test, String dsID) {
		Element mydataset = null;
		List datasets = test.getChildren("dataset");
		for (Iterator dsIt = datasets.iterator(); dsIt.hasNext();) {
			Element dataset = (Element) dsIt.next();
			if ( dataset.getAttributeValue("ID").equals(dsID) ) {
				mydataset = dataset;
			}
		}
		return mydataset;
	}
	private Element findTest (String name) {
		Element mytest = null;
		List tests = getRootElement().getChildren("test");
		for (Iterator testsIt = tests.iterator(); testsIt.hasNext();) {
			Element test = (Element) testsIt.next();
			if ( test.getAttributeValue("name").equals(name) ) {
				mytest = test;
			}
		}
		return mytest;
	}
	private void removeDataset(Element test, String dsID) {
		Element existingDataset = null;
		List datasets = test.getChildren("dataset");
		for (Iterator dsIt = datasets.iterator(); dsIt.hasNext();) {
			Element dataset = (Element) dsIt.next();
			if ( dataset.getAttributeValue("ID").equals(dsID) ) {
				existingDataset = dataset;
			}
		}
		if ( existingDataset != null ) {
			test.removeContent(existingDataset);
		}
	}
}
