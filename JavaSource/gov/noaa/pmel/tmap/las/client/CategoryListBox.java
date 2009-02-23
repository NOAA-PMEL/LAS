package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;

import com.google.gwt.user.client.ui.ListBox;

public class CategoryListBox extends ListBox {
	CategorySerializable category;

	/**
	 * @return the category
	 */
	public CategorySerializable getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(CategorySerializable category) {
		this.category = category;
	}

}
