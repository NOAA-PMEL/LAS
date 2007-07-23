
<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<%@ page import="java.util.*"%>

<html>
	<head>
		<title>My JSP for Constrain form</title>
  	    <STYLE type=text/css>@import url( ./css/lasUI.css );</STYLE>

            <script language="javascript" src="./js/DateWidget.js"></script>            
  	    
  	    <script>
           window.name = "constrain";
  	    //Must put this function in jsp instead of js file because it uses struts taglib
  	    function initializeDateWidget() {
		  	DW = new DateWidget(document.constrain,'<bean:write name="constrain" property="t.lo"/>','<bean:write name="constrain" property="t.hi"/>','<bean:write name="constrain" property="t.minuteInterval"/>');
  			DW.setDate1('<bean:write name="constrain" property="t.current_lo"/>');
  			DW.setDate2('<bean:write name="constrain" property="t.current_hi"/>');          
  			//DW.alert();
		}
  	    </script>		
  	    
		<script language="JavaScript" src="./js/LASRequest.js"></script>
 	    <script language="javascript" src="./js/LASUtil.js"></script>		
		<logic:equal name="constrain" property="t.type" value="widget">
           <script language="javascript"> 
           </script>
        </logic:equal>
        <logic:equal name="constrain" property="t.type" value="menu">
           <script language="javascript">
              function initializeDataWidget() {return;}
           </script>
	    </logic:equal>
	    
	    
	</head>
	<body onload="initPage()">
	<script language="JavaScript">
	    function popup() {
            winCom = "menubar=yes,toolbar=yes,resizable=yes,scrollbars=yes";
            winCom += ",width=800,height=600";
            var dataWindow = window.open("","data", winCom);
            dataWindow.focus();
            var theForm=document.forms[0];
            theForm.target = "data";
            theForm.submit();
        }
    </script>
		<html:form action="Constrain" name="constrain" method="POST">
	       <tiles:insert page="/datasets.jsp" flush="true"/>
	       <p>
   	       <tiles:insert page="/variables.jsp" flush="true"/>
			<p>
			<table bgcolor="#ddddff" border=0>
				<tr>
					<th>
						Reference Map
					</th>
					<th>
						Drawing Tool
					</th>
				</tr>
				<tr>
					<td id="img_table_cell">
						<!-- img src="images/ref_map.gif" alt="Reference Map" /-->
						<iframe src="./html/draw.htm" id="imgWindow" name="imgWindow" frameborder="1" scrolling="no" style="width:720px; height:485px"></iframe>
					</td>
					<td>
						<logic:notEmpty name="constrain" property="xytools">
							<logic:iterate id="tool" name="constrain" property="xytools">
								<!-- html:radio name="constrain" property="toolXY" value="value" idName="tool" onchange="mysubmit()" /-->		
								<!--html:radio name="constrain" property="toolXY" value="value" idName="tool" onchange="changeDrawingTool(this.value)" /-->		
								<html:radio name="constrain" property="toolXY" value="value" idName="tool" onclick="changeDrawingTool(this.value)" />
								<bean:write name="tool" property="name" />
								<br>
							</logic:iterate>
						</logic:notEmpty>
<script>						
  currView = '<bean:write name="constrain" property="toolXY" />';
</script>						
					</td>
				</tr>
				<tr>
					<td>
						<logic:notEmpty name="constrain" property="current_x_lo">
		                    X Lo&nbsp;<html:text name="constrain" property="current_x_lo" size="6" />&nbsp;&nbsp;
		                </logic:notEmpty>
						<logic:notEmpty name="constrain" property="current_x_hi">
	                         <logic:equal name="constrain" property="xinterval" value="true">						
		                         X Hi&nbsp;<html:text name="constrain" property="current_x_hi" size="6" />&nbsp;&nbsp;
		                     </logic:equal>
						</logic:notEmpty>
						<logic:notEmpty name="constrain" property="current_y_lo">
		                     Y Lo&nbsp;<html:text name="constrain" property="current_y_lo" size="6" />&nbsp;&nbsp;
		                </logic:notEmpty>
						<logic:notEmpty name="constrain" property="current_y_hi">
							 <logic:equal name="constrain" property="yinterval" value="true">
		                        Y Hi&nbsp;<html:text name="constrain" property="current_y_hi" size="6" />&nbsp;&nbsp;
		                     </logic:equal>
						</logic:notEmpty>
						&nbsp;&nbsp;&nbsp;<html:submit property="updatexy" value="Full Region" onclick="mysubmit()"/>
					</td>
					<td>
					<html:submit property="plot" value="Generate product" onclick="popup()"/>
					</td>
					</tr>
			</table>
<script>
  x_lo = '<bean:write name="constrain" property="current_x_lo" />';
  x_hi = '<bean:write name="constrain" property="current_x_hi" />';
  y_lo = '<bean:write name="constrain" property="current_y_lo" />';  
  y_hi = '<bean:write name="constrain" property="current_y_hi" />';
</script>
			<table  bgcolor="#ffd9ff">
				<logic:equal name="constrain" property="tdefined" value="true">
					<tr>
						<td>
<script>
  var currToolT = '<bean:write name="constrain" property="toolT" />';
</script>
							<logic:notEmpty name="constrain" property="ttools">
								<logic:iterate id="tool" name="constrain" property="ttools">
									<!--html:radio name="constrain" property="toolT" value="value" idName="tool" onchange="mysubmit()" /-->
									<html:radio name="constrain" property="toolT" value="value" idName="tool" onclick="changeT(this.value)" />
									<bean:write name="tool" property="name" />
									<br>
								</logic:iterate>
							</logic:notEmpty>
						</td>
						<td>
							<logic:notEmpty name="constrain" property="t">
							T Lo:&nbsp;
							   <logic:equal name="constrain" property="t.type" value="widget">
							     <logic:equal name="constrain" property="t.yearNeeded" value="true">
							         <select name="Year1" onChange="DW.selectYear(this)"/>
							     </logic:equal>
							     <logic:equal name="constrain" property="t.monthNeeded" value="true">
                                     <select name="Month1" onChange="DW.selectMonth(this)"/>
                                 </logic:equal>
                                 <logic:equal name="constrain" property="t.dayNeeded" value="true">
                                     <select name="Day1" onChange="DW.selectDay(this)"/>
                                 </logic:equal>
                                 <logic:equal name="constrain" property="t.hourNeeded" value="true">
                                     <select name="Time1" onChange="DW.selectTime(this);document.constrain.t_lo.current=DW.getDate1()"/>
                                 </logic:equal>
                                 <html:hidden name="constrain" property="t.current_lo"/>
                                 </logic:equal>
                                 <logic:equal name="constrain" property="t.type" value="menu">
                                     <html:select name="constrain" property="t.current_lo">
	                                     <html:optionsCollection property="t.lo_items" label="name" value="value"/>
	                                 </html:select>
                                 </logic:equal>		                    
							<logic:equal name="constrain" property="toolT" value="interval">
							 T Hi:&nbsp;
							   <logic:equal name="constrain" property="t.type" value="widget">
							     <logic:equal name="constrain" property="t.yearNeeded" value="true">
							         <select name="Year2" onChange="DW.selectYear(this)"/>
							     </logic:equal>
							     <logic:equal name="constrain" property="t.monthNeeded" value="true">
                                     <select name="Month2" onChange="DW.selectMonth(this)"/>
                                 </logic:equal>
                                 <logic:equal name="constrain" property="t.dayNeeded" value="true">
                                     <select name="Day2" onChange="DW.selectDay(this)"/>
                                 </logic:equal>
                                 <logic:equal name="constrain" property="t.hourNeeded" value="true">
                                     <select name="Time2" onChange="DW.selectTime(this);document.constrain.t.current_hi=DW.getDate2()"/>
                                 </logic:equal>
                                 <html:hidden name="constrain" property="t.current_hi"/>
                                 </logic:equal>
                                 <logic:equal name="constrain" property="t.type" value="menu">
                                     <html:select name="constrain" property="t.current_hi">
	                                     <html:optionsCollection property="t.hi_items" label="name" value="value"/>
	                                 </html:select>
                                 </logic:equal>
							</logic:equal>
							</logic:notEmpty>
						</td>
					</tr>
				</logic:equal>
				<logic:equal name="constrain" property="zdefined" value="true">
				    <tr>
					    <td>
<script>
  var currToolZ = '<bean:write name="constrain" property="toolZ" />';
//  alert('<bean:write name="constrain" property="previous_view" />');
</script>

						    <logic:notEmpty name="constrain" property="ztools">
							    <logic:iterate id="tool" name="constrain" property="ztools">
								    <!--html:radio name="constrain" property="toolZ" value="value" idName="tool" onchange="mysubmit()" /-->
								    <html:radio name="constrain" property="toolZ" value="value" idName="tool" onclick="changeZ(this.value)" />
								    <bean:write name="tool" property="name" />
								    <br>
							    </logic:iterate>
						    </logic:notEmpty>
					    </td>
					    <td>
						    <logic:notEmpty name="constrain" property="current_z_lo">
		                        Z Lo:&nbsp;<html:text name="constrain" property="current_z_lo" size="10" />&nbsp;&nbsp;
		                    </logic:notEmpty>
							<logic:equal name="constrain" property="toolZ" value="interval">
		                        Z Hi:&nbsp;<html:text name="constrain" property="current_z_hi" size="10" />
							</logic:equal>
					    </td>
				    </tr>
				</logic:equal>
				</table>
<!-- 				
				<table bgcolor="#b7eaee" border=0>
				<tr>
				<th colspan="2">
				Operations:
				</th>
				<tr>
					<td colspan="2">
						<tiles:insert page="/operations.jsp" flush="true"/>						
					</td>
				</tr>
				<tr>
			</table>			
					<td colspan="2">
				<tiles:insert page="/options.jsp" flush="true"/>
					</td>
				</tr>
			</table>
-->
			<p>
			<tiles:insert page="/operations.jsp" flush="true"/>						
			<p>
			<tiles:insert page="/options.jsp" flush="true"/>
			</html:form>
	</body>
</html>
