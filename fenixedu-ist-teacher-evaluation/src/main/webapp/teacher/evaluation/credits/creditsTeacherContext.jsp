<%--

    Copyright © 2013 Instituto Superior Técnico

    This file is part of FenixEdu IST Teacher Credits.

    FenixEdu IST Teacher Credits is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu IST Teacher Credits is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu IST Teacher Credits.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:xhtml/>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<p class="infoselected">
	<b>Ano Lectivo :</b> <bean:write name="creditsView" property="infoCredits.infoExecutionPeriod.name"/>
	<br />
	<br />
	<b><bean:message key="label.teacher" /></b> <bean:write name="creditsView" property="infoCredits.infoTeacher.infoPerson.nome"/><br />
	<b><bean:message key="label.teacher.number" /></b> <bean:write name="creditsView" property="infoCredits.infoTeacher.teacherId"/>
</p>