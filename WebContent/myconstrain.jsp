
<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>

<html>
	<head>
	  <title>JSP for MyConstrain form</title>
	  <STYLE type=text/css>@import url( ./css/lasUI.css );</STYLE>
	  <script language="javascript" src="./js/LASUtil.js"></script>
	</head>
<body>


<html:form action="/MyConstrain" method="POST">
<div>
<tiles:insert page="/datasets.jsp" flush="true"/>
</div>
<p>
<div>
<tiles:insert page="/variables.jsp" flush="true"/>
</div>
<p>
<fieldset style="width: 50%;">
<legend>Select Region</legend>
<div>
<tiles:insert page="/refmap.jsp" flush="true"/>
</div>
<p>
<div>
<tiles:insert page="/xy_coords.jsp" flush="true"/>
</div>
</fieldset>
<p>
<div>
<tiles:insert page="/operations.jsp" flush="true"/>
</div>
<p>
<div>
<tiles:insert page="/options.jsp" flush="true"/>
</div>
</html:form>
</body>
</html>
