<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Мега-чат: Письма</title>
</head>
<frameset rows="30, *, 60">
    <frame name="status" src="/chat/status">
    <frame name="messages" src="/chat/messages">
    <frame name="message" src="/chat/composeMessage.jsp">
    <noframes>
        <body>
        <p>Для работы этого чата необходима поддержка фреймов в Вашем браузере.</p>
        </body>
    </noframes>
</frameset>
</html>