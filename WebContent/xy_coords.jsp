
<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>

<div style="border: solid 0px red; margin-left: 0px">
  <div style="border: solid 0px green; width: 40%">
    <div id="rangeid" class="option_box">
      <div id="laN" style="text-align:center;">
        <input id="highY" type="text" class="txt" size="6" value="<bean:write name="constrain" property="current_y_hi"/>">
      </div>
      <div id="lo" style="text-align:center;">
         <table align="center">
           <tr>
           <td><input id="lowX" type="text" class="txt" size="6" value="<bean:write name="constrain" property="current_x_lo"/>"></td>
	   <td><input id="highX" type="text" class="txt" size="6" value="<bean:write name="constrain" property="current_x_hi"/>"></td>
	   </tr>
	 </table>
      </div>
      <div id="laS" style="text-align:center;">
        <input id="lowY" type="text" class="txt" size="6" value="<bean:write name="constrain" property="current_y_lo"/>">
      </div>
    </div>
  </div>
</div>
