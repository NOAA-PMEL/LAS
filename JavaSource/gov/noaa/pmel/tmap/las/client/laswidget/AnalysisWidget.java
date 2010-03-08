package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AnalysisWidget extends Composite {

	HTML title;
	public AnalysisWidget() {
		
		ListBox analysisType = new ListBox();
		analysisType.addItem("None");
		analysisType.addItem("Average");
		analysisType.addItem("Minimum");
		analysisType.addItem("Maximum");
		analysisType.addItem("Sum");
		analysisType.addItem("Variance");

		LASDecoratorPanel myPanel = new LASDecoratorPanel("Select and Analysis...",analysisType, false);
		myPanel.setWidth("260px");
		initWidget(myPanel);
		

		
	}
}
