<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>

<DIV class="showHide chrome5" style="display: none;" id="options_panel">
  <DIV class=heading>
  <img class="exp_arrow" src="./images/min.gif" onClick="javascript:switchDisplays('options_panel', 'options_panel_1')">
  <H2>Options</H2>
  </DIV>  
<table border=0>
<logic:notEmpty name="constrain" property="options">
   <logic:iterate id="option" name="constrain" property="options">
      <tr>
      <logic:equal name="option" property="type" value="menu">
      <td>
	 <bean:write name="option" property="title"/>
      </td>
      <td>
	 <html:select name="option" property="items.current" style="FONT-SIZE: 7pt;">
	    <html:optionsCollection name="option" property="items" label="name" value="value"/>
	 </html:select>
      </td>
      </logic:equal>
      <logic:equal name="option" property="type" value="text">
	 <td><bean:write name="option" property="title"/></td>
	 <td><html:text name="option" property="value" size="20"/></td>
      </logic:equal>
   </tr>
   </logic:iterate>
</logic:notEmpty>
<logic:empty name="constrain" property="options">
<tr>
<td colspan="2">
No options defined for this operation.
</td>
</tr>
</logic:empty> 
</table>
</DIV>

<DIV class="showHide chrome5" id="options_panel_1">
  <DIV class=heading>
  <img class="exp_arrow" src="./images/max.gif" onClick="javascript:switchDisplays('options_panel', 'options_panel_1');">
  <H2>Options</H2>
  </DIV>
</DIV>
