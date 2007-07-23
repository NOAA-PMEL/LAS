<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>

<script>
var currDataset = '<bean:write name="constrain" property="datasets.current" />';

function selectDataset(dataset) {
  if (currDataset != dataset) {
    currDataset = dataset;
    mysubmit();
  }
}
</script>

<!--panel with radio buttons-->
<DIV class="showHide chrome5" style="display: none;" id="datasets">
  <DIV class=heading>
  <img class="exp_arrow" src="./images/min.gif" onClick="javascript:switchDisplays('datasets', 'datasets_1')">
  <H2>Select Dataset:  &nbsp; <span class="prod" id="prod"><bean:write name="constrain" property="datasets.current" /></span></H2>
  </DIV>  
  <table border=0>
  <tr>
    <td colspan="2">
	  <logic:notEmpty name="constrain" property="datasets">
		 <logic:iterate id="op" name="constrain" property="datasets">
			<!--html:radio name="constrain" property="datasets.current" value="value" idName="op" onclick="mysubmit()"/-->
			<html:radio name="constrain" property="datasets.current" value="value" idName="op" onclick="selectDataset(this.value);"/>			
			<bean:write name="op" property="name" />
			<br>
		 </logic:iterate>
	  </logic:notEmpty>
     </td>
   </tr>
   </table>
</DIV>

<DIV class="showHide chrome5" id="datasets_1">
  <DIV class=heading>
  <img class="exp_arrow" src="./images/max.gif" onClick="javascript:switchDisplays('datasets', 'datasets_1');">
  <H2>Select Dataset: &nbsp; <span class="prod" id="prod"><bean:write name="constrain" property="datasets.current" /></span></H2>
  </DIV>
</DIV>
