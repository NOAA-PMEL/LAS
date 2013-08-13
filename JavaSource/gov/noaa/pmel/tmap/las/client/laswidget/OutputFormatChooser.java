package gov.noaa.pmel.tmap.las.client.laswidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OutputFormatChooser extends Composite {
    HorizontalPanel layout = new HorizontalPanel();
    
    String buttonWidth = "28px";
    String buttonHeight = "33px";
    String pdfUrl;

    private Image pdfImage = new Image(GWT.getModuleBaseURL()+ "../images/pdf_icon35.png");
    PushButton pdfButton = new PushButton(pdfImage);
    
    String psUrl;
    
    private Image psImage = new Image(GWT.getModuleBaseURL()+ "../images/ps_icon35.png");
    PushButton psButton = new PushButton(psImage);
    
    String svgUrl;
    
    private Image svgImage = new Image(GWT.getModuleBaseURL()+ "../images/svg_icon35.png");
    PushButton svgButton = new PushButton(svgImage);
    
    String printUrl;
    
    private Image printImage = new Image(GWT.getModuleBaseURL()+ "../images/printer_icon35.png");
    PushButton printButton = new PushButton(printImage);
    
    public OutputFormatChooser() {
        pdfButton.setSize(buttonWidth, buttonHeight);
        pdfButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                open(pdfUrl);              
            }
            
        });
        
        pdfButton.setTitle("Get this plot at a PDF file.");
        
        layout.add(pdfButton);
       
        psButton.setSize(buttonWidth, buttonHeight);
        psButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                open(psUrl);              
            }
            
        });
        
        psButton.setTitle("Get this plot as a PostScript file.");
        
        layout.add(psButton);
        
        svgButton.setSize(buttonWidth, buttonHeight);
        svgButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                open(svgUrl);              
            }
            
        });
        
        layout.add(svgButton);
        
        svgButton.setTitle("Get this plot as Scalable Vector Graphics file.");
        
        printButton.setSize(buttonWidth, buttonHeight);
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
        layout.setVisible(false);
    }
    
    public String getPdfUrl() {
        return pdfUrl;
    }


    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
        layout.setVisible(true);
        pdfButton.setVisible(true);
    }


    public String getPsUrl() {
        return psUrl;
    }


    public void setPsUrl(String psUrl) {
        this.psUrl = psUrl;
        layout.setVisible(true);
        psButton.setVisible(true);
    }

    public void setSvgUrl(String svgUrl) {
        this.svgUrl = svgUrl;
        layout.setVisible(true);
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
        layout.setVisible(true);
    }


    private void open(String url) {
        Window.open(url, "_new", null);
    }
}
