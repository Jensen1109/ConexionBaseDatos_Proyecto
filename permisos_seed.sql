-- ═══════════════════════════════════════════════════════════════
-- SEED DE PERMISOS — Tienda del Barrio (RF03)
-- Ejecutar sobre la BD: proyectoPersonal
-- ═══════════════════════════════════════════════════════════════

USE proyectoPersonal;

-- ── 1. Insertar permisos del sistema ─────────────────────────
INSERT IGNORE INTO Permisos (nombre, descripcion) VALUES
    ('VER_PRODUCTOS',       'Ver catálogo de productos'),
    ('GESTIONAR_PRODUCTOS', 'Crear, editar y eliminar productos'),
    ('VER_STOCK',           'Ver panel de control de stock'),
    ('REGISTRAR_VENTA',     'Registrar nuevas ventas'),
    ('VER_HISTORIAL',       'Ver historial completo de ventas'),
    ('GESTIONAR_CLIENTES',  'Crear, editar y eliminar clientes'),
    ('GESTIONAR_DEUDAS',    'Ver y gestionar deudas y abonos'),
    ('GESTIONAR_USUARIOS',  'Crear, editar y eliminar usuarios del sistema');

-- ── 2. Admin (id_rol=1): todos los permisos ──────────────────
INSERT IGNORE INTO rol_permiso (id_rol, id_permiso)
SELECT 1, id_permiso FROM Permisos;

-- ── 3. Empleado (id_rol=2): permisos limitados ───────────────
INSERT IGNORE INTO rol_permiso (id_rol, id_permiso)
SELECT 2, id_permiso FROM Permisos
WHERE nombre IN ('VER_PRODUCTOS', 'VER_STOCK', 'REGISTRAR_VENTA');

-- ── 4. Verificar ──────────────────────────────────────────────
SELECT r.nombre AS rol, p.nombre AS permiso
FROM rol_permiso rp
JOIN Rol r ON rp.id_rol = r.id_rol
JOIN Permisos p ON rp.id_permiso = p.id_permiso
ORDER BY r.id_rol, p.nombre;
