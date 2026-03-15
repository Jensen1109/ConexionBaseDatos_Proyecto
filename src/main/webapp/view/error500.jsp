<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error interno — Tienda del Barrio</title>
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', system-ui, sans-serif;
            background: #f1f5f9;
            display: flex; align-items: center; justify-content: center;
            min-height: 100vh; text-align: center;
        }
        .card {
            background: #fff; border-radius: 16px;
            padding: 3rem 2.5rem; max-width: 420px; width: 90%;
            box-shadow: 0 4px 24px rgba(0,0,0,0.08);
        }
        .code {
            font-size: 5rem; font-weight: 800;
            color: #ef4444; line-height: 1;
        }
        h1 { font-size: 1.4rem; color: #1e293b; margin: 0.8rem 0 0.5rem; }
        p  { color: #64748b; font-size: 0.9rem; margin-bottom: 1.8rem; }
        a  {
            display: inline-block;
            background: #ef4444; color: #fff;
            padding: 0.65rem 1.5rem; border-radius: 8px;
            text-decoration: none; font-size: 0.9rem; font-weight: 600;
            transition: background 0.2s;
        }
        a:hover { background: #dc2626; }
    </style>
</head>
<body>
    <div class="card">
        <div class="code">500</div>
        <h1>Error interno del servidor</h1>
        <p>Ocurrió un problema inesperado.<br>Por favor intenta de nuevo o contacta al administrador.</p>
        <a href="<%= ctx %>/ProductoControlador">Volver al inicio</a>
    </div>
</body>
</html>
