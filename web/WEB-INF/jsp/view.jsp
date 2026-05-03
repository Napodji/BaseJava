<%@ page import="com.basejava.webapp.model.*" %>
<%@ page import="com.basejava.webapp.util.HtmlHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="css/style.css">
    <jsp:useBean id="resume" type="com.basejava.webapp.model.Resume" scope="request"/>
    <title>Резюме ${resume.fullName}</title>
</head>
<body>
<jsp:include page="fragments/header.jsp"/>
<section>
    <h1>${resume.fullName}&nbsp;
        <a href="resume?uuid=${resume.uuid}&action=edit">
            <img src="img/pencil.png">
        </a>
    </h1>

    <%-- Контакты --%>
    <p>
        <c:forEach var="contactEntry" items="${resume.contacts}">
            <jsp:useBean id="contactEntry"
                         type="java.util.Map.Entry<com.basejava.webapp.model.ContactType, java.lang.String>"/>
            <%=contactEntry.getKey().toHtml(contactEntry.getValue())%><br/>
        </c:forEach>
    </p>
    <hr>

    <%-- Секции --%>
    <table cellpadding="2">
        <c:forEach var="sectionEntry" items="${resume.sections}">
            <jsp:useBean id="sectionEntry"
                         type="java.util.Map.Entry<com.basejava.webapp.model.SectionType,
                                                   com.basejava.webapp.model.AbstractSection>"/>
            <c:set var="type" value="${sectionEntry.key}"/>
            <c:set var="section" value="${sectionEntry.value}"/>
            <jsp:useBean id="section" type="com.basejava.webapp.model.AbstractSection"/>

            <%-- Заголовок секции --%>
            <tr>
                <td colspan="2"><h2>${type.title}</h2></td>
            </tr>

            <c:choose>
                <%-- OBJECTIVE — выделяем жирным как заголовок --%>
                <c:when test="${type=='OBJECTIVE'}">
                    <tr>
                        <td colspan="2">
                            <h3><%=((TextSection) section).getContent()%></h3>
                        </td>
                    </tr>
                </c:when>

                <%-- PERSONAL — обычный текст --%>
                <c:when test="${type=='PERSONAL'}">
                    <tr>
                        <td colspan="2"><%=((TextSection) section).getContent()%></td>
                    </tr>
                </c:when>

                <%-- ACHIEVEMENT, QUALIFICATIONS — маркированный список --%>
                <c:when test="${type=='ACHIEVEMENT' || type=='QUALIFICATIONS'}">
                    <tr>
                        <td colspan="2">
                            <ul>
                                <c:forEach var="item" items="<%=((ListSection) section).getItems()%>">
                                    <li>${item}</li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:when>

                <%-- EXPERIENCE, EDUCATION — таблица организаций с позициями --%>
                <c:when test="${type=='EXPERIENCE' || type=='EDUCATION'}">
                    <c:forEach var="org" items="<%=((OrganizationSection) section).getOrganizations()%>">
                        <%-- Название организации со ссылкой (если есть url) --%>
                        <tr>
                            <td colspan="2">
                                <c:choose>
                                    <c:when test="${empty org.homePage.url}">
                                        <h3>${org.homePage.name}</h3>
                                    </c:when>
                                    <c:otherwise>
                                        <h3><a href="${org.homePage.url}">${org.homePage.name}</a></h3>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <%-- Позиции: даты слева, должность + описание справа --%>
                        <c:forEach var="position" items="${org.positions}">
                            <jsp:useBean id="position"
                                         type="com.basejava.webapp.model.Organization.Position"/>
                            <tr>
                                <td width="15%" style="vertical-align: top; color: gray">
                                    <%=HtmlHelper.formatDates(position)%>
                                </td>
                                <td>
                                    <b>${position.title}</b><br>
                                    <c:if test="${not empty position.description}">
                                        ${position.description}
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:forEach>
                </c:when>
            </c:choose>
        </c:forEach>
    </table>

    <br>
    <button onclick="window.history.back()">ОК</button>
</section>
<jsp:include page="fragments/footer.jsp"/>
</body>
</html>