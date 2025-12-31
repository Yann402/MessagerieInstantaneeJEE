<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Erreur - Messagerie Instantanée</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f8f9fa;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .error-container {
            text-align: center;
            padding: 2rem;
            background: white;
            border-radius: 10px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            max-width: 500px;
        }
        h1 {
            color: #e74c3c;
            margin-bottom: 1rem;
        }
        .btn {
            display: inline-block;
            padding: 0.75rem 1.5rem;
            background: #667eea;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin-top: 1rem;
        }
        .btn:hover {
            background: #5a6fd8;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>Oups ! Une erreur est survenue</h1>
        <p>Désolé, une erreur s'est produite lors du traitement de votre demande.</p>
        
        <c:if test="${not empty requestScope['jakarta.servlet.error.status_code']}">
            <p><strong>Code d'erreur :</strong> ${requestScope['jakarta.servlet.error.status_code']}</p>
        </c:if>
        
        <c:if test="${not empty requestScope['jakarta.servlet.error.message']}">
            <p><strong>Message :</strong> ${requestScope['jakarta.servlet.error.message']}</p>
        </c:if>
        
        <a href="${pageContext.request.contextPath}/login" class="btn">Retour à l'accueil</a>
    </div>
</body>
</html>