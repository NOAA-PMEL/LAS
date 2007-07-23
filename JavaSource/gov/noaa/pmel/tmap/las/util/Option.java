/**
 * This software was developed by the Thermal Modeling and Analysis
 * Project(TMAP) of the National Oceanographic and Atmospheric
 * Administration's (NOAA) Pacific Marine Environmental Lab(PMEL),
 * hereafter referred to as NOAA/PMEL/TMAP.
 *
 * Access and use of this software shall impose the following
 * obligations and understandings on the user. The user is granted the
 * right, without any fee or cost, to use, copy, modify, alter, enhance
 * and distribute this software, and any derivative works thereof, and
 * its supporting documentation for any purpose whatsoever, provided
 * that this entire notice appears in all copies of the software,
 * derivative works and supporting documentation. Further, the user
 * agrees to credit NOAA/PMEL/TMAP in any publications that result from
 * the use of this software or in any product that includes this
 * software. The names TMAP, NOAA and/or PMEL, however, may not be used
 * in any advertising or publicity to endorse or promote any products
 * or commercial entity unless specific written permission is obtained
 * from NOAA/PMEL/TMAP. The user also understands that NOAA/PMEL/TMAP
 * is not obligated to provide the user with any support, consulting,
 * training or assistance of any kind with regard to the use, operation
 * and performance of this software nor to provide the user with any
 * updates, revisions, new versions or "bug fixes".
 *
 * THIS SOFTWARE IS PROVIDED BY NOAA/PMEL/TMAP "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL NOAA/PMEL/TMAP BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 * CONTRACT, NEGLIGENCE OR OTHER TORTUOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE 
 */
package gov.noaa.pmel.tmap.las.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class Option extends Container implements OptionInterface {
    
    public Option(Element element) {
        super(element);
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#getHelp()
     */
    public String getHelp() {
        return element.getAttributeValue("help");
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#setHelp(java.lang.String)
     */
    public void setHelp(String help) {
        element.setAttribute("help", help);
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#getMenu()
     */
    public ArrayList<NameValuePair> getMenu() {
        ArrayList<NameValuePair> menu = new ArrayList<NameValuePair>();
        List items = element.getChild("menu").getChildren("item");
        for (Iterator itemsIt = items.iterator(); itemsIt.hasNext();) {
            Element item = (Element) itemsIt.next();
            menu.add(new NameValuePair(item.getChildText("name"), item.getChildText("value")));
        }
        
        return menu;
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#setMenu(java.util.ArrayList)
     */
    public void setMenu(ArrayList<NameValuePair> menu) {
        Element menuE = new Element("menu");
        for (Iterator menuIt = menu.iterator(); menuIt.hasNext();) {
            NameValuePair menuItem = (NameValuePair) menuIt.next();
            Element item = new Element("item");
            Element name = new Element("name");
            Element value = new Element("value");
            name.setText(menuItem.getName());
            value.setText(menuItem.getValue());
            item.addContent(name);
            item.addContent(value);
            menuE.addContent(item);
        }
        element.removeContent(element.getChild("menu"));
        element.addContent(menuE);
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#getTitle()
     */
    public String getTitle() {
        return element.getAttributeValue("title");
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        element.setAttribute("title", title);
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#getType()
     */
    public String getType() {
        return element.getAttributeValue("type");
    }

    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.OptionInterface#setType(java.lang.String)
     */
    public void setType(String type) {
        element.setAttribute("type", type);
    }
    

}
