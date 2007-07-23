

<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>

<DIV class="showHide chrome5" id="operations_1">
  <DIV class=heading>
  <H2>Select Operations: </H2>
  </DIV>
  <html:select name="constrain" property="operations.current" onchange="mysubmit()">
    <html:optionsCollection property="operations" label="name" value="value"/>
  </html:select>
</DIV>


