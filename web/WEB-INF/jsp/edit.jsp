<%@ page import="com.basejava.webapp.model.*" %>
<%@ page import="com.basejava.webapp.util.DateHelper" %>
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
    <form method="post" action="resume" enctype="application/x-www-form-urlencoded">
        <input type="hidden" name="uuid" value="${resume.uuid}">
        <dl>
            <dt>Имя:</dt>
            <dd><input type="text" name="fullName" size=55 value="${resume.fullName}"></dd>
        </dl>

        <h2>Контакты:</h2>
        <c:forEach var="type" items="<%=ContactType.values()%>">
            <dl>
                <dt>${type.title}</dt>
                <dd><input type="text" name="${type.name()}" size=30
                           value="${resume.getContact(type)}"></dd>
            </dl>
        </c:forEach>

        <hr>
        <c:forEach var="type" items="<%=SectionType.values()%>">
            <c:set var="section" value="${resume.getSection(type)}"/>
            <h2>${type.title}</h2>
            <c:choose>
                <c:when test="${type=='OBJECTIVE'}">
                    <input type="text" name="${type}" size=75 value="${section}">
                </c:when>
                <c:when test="${type=='PERSONAL'}">
                    <textarea name="${type}" cols=75 rows=3>${section}</textarea>
                </c:when>
                <c:when test="${type=='ACHIEVEMENT' || type=='QUALIFICATIONS'}">
                    <% ListSection ls = (ListSection) pageContext.getAttribute("section"); %>
                    <textarea name="${type}" cols=75 rows=5><%=
                    ls == null ? "" : String.join("\n", ls.getItems())
                    %></textarea>
                </c:when>
                <c:when test="${type=='EXPERIENCE' || type=='EDUCATION'}">
                    <c:if test="${section != null}">
                        <% OrganizationSection os = (OrganizationSection) pageContext.getAttribute("section"); %>
                        <c:forEach var="org" items="<%=os.getOrganizations()%>" varStatus="counter">
                            <dl>
                                <dt>Название:</dt>
                                <dd><input type="text" name="${type}" size=80
                                           value="${org.homePage.name}"></dd>
                            </dl>
                            <dl>
                                <dt>Сайт:</dt>
                                <dd><input type="text" name="${type}url" size=80
                                           value="${org.homePage.url}"></dd>
                            </dl>
                            <div style="margin-left: 30px">
                                <c:forEach var="pos" items="${org.positions}">
                                    <% Organization.Position p = (Organization.Position) pageContext.getAttribute("pos"); %>
                                    <dl>
                                        <dt>Начало (MM/yyyy):</dt>
                                        <dd><input type="text"
                                                   name="${type}${counter.index}startDate" size=10
                                                   value="<%=DateHelper.format(p.getStartDate())%>"
                                                   placeholder="MM/yyyy"></dd>
                                    </dl>
                                    <dl>
                                        <dt>Конец (MM/yyyy):</dt>
                                        <dd><input type="text"
                                                   name="${type}${counter.index}endDate" size=10
                                                   value="<%=DateHelper.format(p.getEndDate())%>"
                                                   placeholder="MM/yyyy"></dd>
                                    </dl>
                                    <dl>
                                        <dt>Должность:</dt>
                                        <dd><input type="text"
                                                   name="${type}${counter.index}title" size=75
                                                   value="${pos.title}"></dd>
                                    </dl>
                                    <dl>
                                        <dt>Описание:</dt>
                                        <dd><textarea name="${type}${counter.index}description"
                                                      cols=75 rows=3>${pos.description}</textarea></dd>
                                    </dl>
                                    <hr>
                                </c:forEach>
                            </div>
                        </c:forEach>
                    </c:if>
                </c:when>
            </c:choose>
        </c:forEach>

        <button type="submit">Сохранить</button>
        <button type="button" onclick="window.history.back()">Отменить</button>
    </form>
</section>
<jsp:include page="fragments/footer.jsp"/>
</body>
</html>