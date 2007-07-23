
<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>

<script>
var currVariable = '<bean:write name="constrain" property="variables.current" />';

function selectVariable(select_var) {
  if (currVariable != select_var) {
    currVariable = select_var;
    mysubmit();
  }
}

</script>

<!--panel with radio buttons-->
<DIV class="showHide chrome5" style="display: none;" id="variable_rd">
  <DIV class=heading>
  <img class="exp_arrow" src="./images/min.gif" onchange="javascript:switchDisplays('variable_rd', 'variable_rd_1')">
  <H2>Select Variable:  &nbsp; <span class="prod" id="prod"><bean:write name="constrain" property="variables.current" /></span></H2>
  </DIV>  
  <table border=0>
  <tr>
    <td colspan="2">
	  <logic:notEmpty name="constrain" property="variables">
		 <logic:iterate id="op" name="constrain" property="variables">
			<!--html:radio name="constrain" property="variables.current" value="value" idName="op" onchange="mysubmit()" /-->
 		       <html:radio name="constrain" property="variables.current" value="value" idName="op" onclick="selectVariable(this.value)" />
			<bean:write name="op" property="name" />
			<br>
		 </logic:iterate>
	  </logic:notEmpty>
     </td>
   </tr>
   </table>
</DIV>

<DIV class="showHide chrome5" id="variable_rd_1">
  <DIV class=heading>
  <img class="exp_arrow" src="./images/max.gif" onClick="javascript:switchDisplays('variable_rd', 'variable_rd_1');">
  <H2>Select Variable: &nbsp; <span class="prod" id="prod"><bean:write name="constrain" property="variables.current" /></span></H2>
  </DIV>
</DIV>
