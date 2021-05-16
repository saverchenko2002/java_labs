<%--
  Created by IntelliJ IDEA.
  User: SergeySaber
  Date: 16.05.2021
  Time: 19:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>First Jsp</title>
</head>
<body>
    <h1>Testing Jsp</h1>
    <%@ page import ="java.util.Date, logic.TestClass"%>
    <%
        java.util.Date date = new Date();
        String currentDate = "Current time: " + date;
        TestClass testClass = new TestClass();
        String name = request.getParameter("name");
    %>
    <p>
        <%  out.println(name + " " + testClass.getInfo()); %>
    </p>
    <p>
        <%= currentDate %>
    </p>

</body>
</html>
