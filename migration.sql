-- ═══════════════════════════════════════════════════════════════
-- SCRIPT DE MIGRACIÓN — Tienda del Barrio
-- Ejecutar en MySQL Workbench sobre la BD: proyectoPersonal
-- ═══════════════════════════════════════════════════════════════

USE proyectoPersonal;

-- ── 1. Tabla Cliente (nueva) ─────────────────────────────────
CREATE TABLE IF NOT EXISTS Cliente (
    id_cliente INT PRIMARY KEY AUTO_INCREMENT,
    nombre     VARCHAR(100) NOT NULL,
    apellido   VARCHAR(100) NOT NULL,
    cedula     VARCHAR(20)  NOT NULL UNIQUE,
    telefono   VARCHAR(20)
);

-- ── 2. Pedido.id_cliente ahora apunta a Cliente (nullable para ventas sin cliente) ──
-- Primero quitar datos de id_cliente existentes si referencian Usuario
ALTER TABLE Pedido MODIFY id_cliente INT NULL;

-- Agregar FK a la tabla Cliente (solo si no existe ya)
ALTER TABLE Pedido
    ADD CONSTRAINT fk_pedido_cliente
    FOREIGN KEY (id_cliente) REFERENCES Cliente(id_cliente)
    ON DELETE SET NULL;

-- ── 3. Deuda.id_pedido nullable para deudas manuales ─────────
ALTER TABLE Deuda MODIFY id_pedido INT NULL;

-- Recrear la restricción UNIQUE (MySQL la elimina al hacer MODIFY)
-- Si da error porque ya no existe, ignorar:
ALTER TABLE Deuda ADD UNIQUE INDEX ux_deuda_pedido (id_pedido);

-- ── 4. Actualizar estados existentes en Pedido ───────────────
-- Antes: 'completado' / 'pendiente' / 'fiada'
-- Ahora: 'pagado' / 'credito'
UPDATE Pedido SET estado = 'pagado'  WHERE estado IN ('completado', 'entregado');
UPDATE Pedido SET estado = 'credito' WHERE estado IN ('pendiente', 'fiada');

-- ── 5. Actualizar estados existentes en Deuda ────────────────
-- Antes: 'pendiente' / 'pagado'
-- Ahora: 'activa' / 'pagada'
UPDATE Deuda SET estado = 'activa'  WHERE estado = 'pendiente';
UPDATE Deuda SET estado = 'pagada'  WHERE estado = 'pagado';

-- ── 6. Insertar métodos de pago si no existen ────────────────
INSERT IGNORE INTO metodo_pago (nombre) VALUES ('Efectivo'), ('Nequi'), ('Tarjeta');

-- ── 7. Verificar estructura final ────────────────────────────
SELECT 'Cliente' AS tabla, COUNT(*) AS registros FROM Cliente
UNION ALL
SELECT 'Pedido',  COUNT(*) FROM Pedido
UNION ALL
SELECT 'Deuda',   COUNT(*) FROM Deuda
UNION ALL
SELECT 'metodo_pago', COUNT(*) FROM metodo_pago;
