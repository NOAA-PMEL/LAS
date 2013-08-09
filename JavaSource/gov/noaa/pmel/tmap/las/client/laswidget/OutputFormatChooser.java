package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OutputFormatChooser extends Composite {
    VerticalPanel layout = new VerticalPanel();
    
    String pdfUrl;

    private static Image pdfImage = new Image(GWT.getModuleBaseURL()+ "../images/pdf_icon35.jpg");
    PushButton pdfButton = new PushButton(pdfImage);
    
    String psUrl;
    
    private static Image psImage = new Image(GWT.getModuleBaseURL()+ "../images/ps_icon35.jpg");
    PushButton psButton = new PushButton(psImage);
    
    String svgUrl;
    
    private static Image svgImage = new Image(GWT.getModuleBaseURL()+ "../images/svg_icon35.jpg");
    PushButton svgButton = new PushButton(svgImage);
    
    String printUrl;
    
    private static Image printImage = new Image(GWT.getModuleBaseURL()+ "../images/printer_icon35.jpg");
    PushButton printButton = new PushButton(printImage);
    
    public OutputFormatChooser() {
        pdfButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                open(pdfUrl);              
            }
            
        });
        
        pdfButton.setTitle("Get this plot at a PDF file.");
        
        layout.add(pdfButton);
       
        psButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                open(psUrl);              
            }
            
        });
        
        psButton.setTitle("Get this plot as a PostScript file.");
        
        layout.add(psButton);
        
        svgButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                open(svgUrl);              
            }
            
        });
        
        layout.add(svgButton);
        
        svgButton.setTitle("Get this plot as Scalable Vector Graphics file.");
        
        printButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                open(printUrl);              
            }
            
        });
        
        layout.add(printButton);
        
        printButton.setTitle("Open this plot as a printable HTML page.");
        
        initWidget(layout);
    }
    
    public void hide() {
        pdfButton.setVisible(false);
        psButton.setVisible(false);
        svgButton.setVisible(false);
    }
    
    public String getPdfUrl() {
        return pdfUrl;
    }


    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
        pdfButton.setVisible(true);
    }


    public String getPsUrl() {
        return psUrl;
    }


    public void setPsUrl(String psUrl) {
        this.psUrl = psUrl;
        psButton.setVisible(true);
    }

    public void setSvgUrl(String svgUrl) {
        this.svgUrl = svgUrl;
        svgButton.setVisible(true);
    }

    public String getSvgUrl() {
        return this.svgUrl;
    }

    public String getPrintUrl() {
        return printUrl;
    }


    public void setPrintUrl(String printUrl) {
        this.printUrl = printUrl;
    }


    private void open(String url) {
        Window.open(url, "_new", null);
    }
}
