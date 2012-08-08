package gov.noaa.pmel.tmap.las;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TestMessages {
	private static final String BUNDLE_NAME = "gov.noaa.pmel.tmap.las.testmessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private TestMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
