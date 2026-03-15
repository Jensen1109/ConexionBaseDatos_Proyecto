<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <title>Bienvenido — Tienda Don Pedro</title>
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', system-ui, sans-serif;
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 60%, #1e3a5f 100%);
            min-height: 100vh;
            display: flex; align-items: center; justify-content: center;
            padding: 1.5rem;
        }

        .hero {
            text-align: center;
            max-width: 480px; width: 100%;
        }

        /* Icono/logo superior */
        .hero__icon {
            width: 80px; height: 80px; border-radius: 22px;
            background: linear-gradient(135deg, #22c55e, #16a34a);
            display: flex; align-items: center; justify-content: center;
            font-size: 2.2rem; color: #fff;
            margin: 0 auto 1.5rem;
            box-shadow: 0 8px 32px rgba(34,197,94,0.35);
        }

        .hero__title {
            font-size: 2rem; font-weight: 800;
            color: #f8fafc; margin-bottom: 0.4rem;
            letter-spacing: -0.02em;
        }
        .hero__title span { color: #22c55e; }

        .hero__sub {
            color: #94a3b8; font-size: 0.95rem;
            line-height: 1.6; margin-bottom: 2.5rem;
        }

        /* Tarjeta de acciones */
        .card {
            background: rgba(255,255,255,0.05);
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 16px;
            padding: 2rem 1.8rem;
            backdrop-filter: blur(12px);
        }

        .card__label {
            font-size: 0.75rem; font-weight: 700; color: #64748b;
            text-transform: uppercase; letter-spacing: 0.08em;
            margin-bottom: 1rem;
        }

        .btn {
            display: flex; align-items: center; justify-content: center;
            gap: 0.6rem; width: 100%; padding: 0.85rem 1rem;
            border-radius: 10px; font-size: 0.95rem; font-weight: 600;
            text-decoration: none; transition: all 0.2s; border: none;
            cursor: pointer;
        }
        .btn i { font-size: 0.9rem; }

        .btn--primary {
            background: #22c55e; color: #fff;
            box-shadow: 0 4px 14px rgba(34,197,94,0.3);
            margin-bottom: 0.75rem;
        }
        .btn--primary:hover {
            background: #16a34a;
            box-shadow: 0 6px 20px rgba(34,197,94,0.45);
            transform: translateY(-1px);
        }

        .btn--secondary {
            background: rgba(255,255,255,0.08);
            color: #e2e8f0;
            border: 1px solid rgba(255,255,255,0.15);
        }
        .btn--secondary:hover {
            background: rgba(255,255,255,0.14);
            transform: translateY(-1px);
        }

        /* Footer */
        .hero__footer {
            margin-top: 1.8rem;
            color: #475569; font-size: 0.78rem;
        }
        .hero__footer i { margin-right: 0.3rem; }
    </style>
</head>
<body>
    <div class="hero">

        <div class="hero__icon">
            <i class="fas fa-store"></i>
        </div>

        <h1 class="hero__title">Tienda <span>Don Pedro</span></h1>
        <p class="hero__sub">
            Sistema de gestión de ventas, inventario y clientes.<br>
            Inicia sesión para acceder al panel.
        </p>

        <div class="card">
            <p class="card__label">Acceder al sistema</p>

            <a href="<%= ctx %>/LoginControlador" class="btn btn--primary">
                <i class="fas fa-sign-in-alt"></i>
                Iniciar sesión
            </a>

            <a href="<%= ctx %>/RegistroControlador" class="btn btn--secondary">
                <i class="fas fa-user-plus"></i>
                Crear cuenta nueva
            </a>
        </div>

        <p class="hero__footer">
            <i class="fas fa-lock"></i>
            Acceso restringido — solo personal autorizado
        </p>
    </div>
</body>
</html>
