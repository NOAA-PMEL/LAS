
<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>

<script>
function setDrawingTool(v) {
  if (Img.getView() != v) {
    Img.setView(v);
    alert(Img.getView());
    Img.setDrawingArea(1, 30, 5, 66);
  }
}
</script>



<!-- gets the selected radio button -->
<!--
<bean:define id="selectedRadio" property="xytools" name="constrain"/>
-->

<logic:notEmpty name="constrain" property="xytools">
  <logic:iterate id="tool" name="constrain" property="xytools">
    <html:radio name="constrain" property="toolXY" value="value" idName="tool" onclick="setDrawingTool(this.value)" />
    <bean:write name="tool" property="name" />
    <br>
  </logic:iterate>
<br>

</logic:notEmpty>
