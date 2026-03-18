package controladores;

// Importar todos los DAOs necesarios para registrar ventas
import dao.ClienteDAO;     // Acceso a tabla Cliente
import dao.DeudaDAO;       // Acceso a tabla Deuda (ventas a crédito)
import dao.MetodoPagoDAO;  // Acceso a tabla MetodoPago (efectivo, nequi, tarjeta)
import dao.PedidoDAO;      // Acceso a tabla Pedido y detalle_pedido
import dao.PermisosDAO;    // Acceso a tabla rol_permiso para verificar permisos
import dao.ProductoDAO;    // Acceso a tabla Producto (consultar stock y precios)
import dao.TelefonoDAO;    // Acceso a tabla Telefono (teléfonos de clientes)
// Importar los modelos necesarios
import modelos.Deuda;          // Modelo de deuda
import modelos.DetallePedido;  // Modelo de línea de detalle (producto + cantidad + precio)
import modelos.Pedido;         // Modelo de pedido/venta
import modelos.Producto;       // Modelo de producto (para verificar stock)
import modelos.Telefono;       // Modelo de teléfono
import modelos.Usuario;        // Modelo de usuario logueado
// Importaciones de Jakarta Servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;    // Para manejar totales y precios con decimales exactos
import java.time.LocalDate;     // Para manejar fechas de filtro
import java.util.ArrayList;     // Lista dinámica para los detalles del pedido
import java.util.List;

/**
 * Controlador de ventas (Pedidos) e historial.
 * Gestiona: registrar nueva venta, ver historial de ventas, cambiar estado de pedido.
 * Admin y empleados pueden registrar ventas (permiso REGISTRAR_VENTA).
 * Solo admin puede ver el historial completo (permiso VER_HISTORIAL).
 */
@WebServlet("/PedidoControlador")
public class PedidoControlador extends HttpServlet {

    // Instancias de todos los DAOs necesarios para las operaciones de ventas
    private final PedidoDAO     pedidoDAO     = new PedidoDAO();     // Crear pedidos y listar historial
    private final ProductoDAO   productoDAO   = new ProductoDAO();   // Consultar productos y stock
    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO(); // Listar métodos de pago
    private final ClienteDAO    clienteDAO    = new ClienteDAO();    // Buscar/crear clientes
    private final DeudaDAO      deudaDAO      = new DeudaDAO();      // Registrar deudas (ventas fiadas)
    private final PermisosDAO   permisosDAO   = new PermisosDAO();   // Verificar permisos del rol
    private final TelefonoDAO   telefonoDAO   = new TelefonoDAO();   // Guardar teléfono del cliente nuevo

    /**
     * Verifica que el usuario tenga sesión activa. Si no, redirige al login.
     */
    private boolean verificarSesion(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // Headers anti-caché
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
        // Buscar sesión existente
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            res.sendRedirect(req.getContextPath() + "/LoginControlador");
            return false; // No hay sesión
        }
        return true; // Sesión válida
    }

    /**
     * Verifica sesión activa + permiso específico del rol del usuario.
     */
    private boolean verificarPermiso(HttpServletRequest req, HttpServletResponse res,
                                      String permiso, String redirectPath) throws IOException {
        if (!verificarSesion(req, res)) return false;
        Usuario u = (Usuario) req.getSession(false).getAttribute("usuarioLogueado");
        // Consultar en tabla rol_permiso si este rol tiene el permiso solicitado
        if (!permisosDAO.tienePermiso(u.getIdRol(), permiso)) {
            res.sendRedirect(req.getContextPath() + redirectPath);
            return false; // No tiene permiso
        }
        return true; // Tiene permiso
    }

    // ═══════════════════════════════════════════════════
    // GET: Mostrar formulario de venta o historial
    // ═══════════════════════════════════════════════════

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesion(request, response)) return;

        // Leer qué acción se pide: "nuevo" = formulario venta, otro = historial
        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            // ─── FORMULARIO REGISTRAR VENTA NUEVA ───
            // Verificar permiso REGISTRAR_VENTA
            if (!verificarPermiso(request, response, "REGISTRAR_VENTA", "/ProductoControlador")) return;
            // Cargar productos disponibles para el select del carrito
            request.setAttribute("productos",   productoDAO.listarTodos());
            // Cargar métodos de pago para el select (Efectivo, Nequi, Tarjeta)
            request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
            // Pasar el ID del cliente "Admin Tienda" al formulario para control de fiado
            request.setAttribute("idAdminTienda", clienteDAO.obtenerIdAdminTienda());
            // Mostrar el formulario de venta
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);

        } else {
            // ─── HISTORIAL DE VENTAS ───
            // Solo usuarios con permiso VER_HISTORIAL pueden acceder
            if (!verificarPermiso(request, response, "VER_HISTORIAL", "/PedidoControlador?accion=nuevo")) return;

            // Leer filtros de fecha (opcionales)
            String fechaIniStr = request.getParameter("fechaInicio");
            String fechaFinStr = request.getParameter("fechaFin");

            List<Pedido> pedidos;
            // Si hay filtros de fecha, buscar solo en ese rango
            if (fechaIniStr != null && !fechaIniStr.isBlank() &&
                fechaFinStr  != null && !fechaFinStr.isBlank()) {
                // Listar pedidos entre las dos fechas
                pedidos = pedidoDAO.listarPorFechas(
                    LocalDate.parse(fechaIniStr),   // Convertir String a LocalDate
                    LocalDate.parse(fechaFinStr));
                // Mantener los valores de filtro en el formulario
                request.setAttribute("fechaInicio", fechaIniStr);
                request.setAttribute("fechaFin",    fechaFinStr);
            } else {
                // Sin filtros → listar todos los pedidos
                pedidos = pedidoDAO.listarConCliente();
            }

            // Pasar la lista de pedidos al JSP
            request.setAttribute("pedidos", pedidos);
            // Mostrar el historial de ventas
            request.getRequestDispatcher("/view/historialventa.jsp").forward(request, response);
        }
    }

    // ═══════════════════════════════════════════════════
    // POST: Registrar venta nueva o cambiar estado
    // ═══════════════════════════════════════════════════

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesion(request, response)) return;
        // UTF-8 para leer caracteres especiales (tildes, ñ)
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        // ─── CAMBIAR ESTADO DE UN PEDIDO (desde historial) ───
        if ("cambiarEstado".equals(accion)) {
            if (!verificarPermiso(request, response, "VER_HISTORIAL", "/PedidoControlador?accion=nuevo")) return;
            int idPedido = Integer.parseInt(request.getParameter("idPedido"));
            // Actualizar el estado del pedido (ej: de "credito" a "pagado")
            pedidoDAO.cambiarEstado(idPedido, request.getParameter("estado"));
            response.sendRedirect(request.getContextPath() + "/PedidoControlador");
            return;
        }

        // ─── REGISTRAR VENTA NUEVA ───
        if (!verificarPermiso(request, response, "REGISTRAR_VENTA", "/ProductoControlador")) return;
        // Obtener el empleado que está registrando la venta
        Usuario empleado = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");

        // Leer los arrays de productos del carrito (cada producto es un elemento del array)
        String[] ids        = request.getParameterValues("idProducto");     // IDs de productos
        String[] cantidades = request.getParameterValues("cantidad");       // Cantidades por producto
        String[] precios    = request.getParameterValues("precioUnitario"); // Precios por producto

        // Validar que haya al menos un producto en el carrito
        if (ids == null || ids.length == 0) {
            cargarFormulario(request);
            request.setAttribute("error", "Debes agregar al menos un producto.");
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
            return;
        }

        // Validar que se haya seleccionado un método de pago
        String idPagoStr = request.getParameter("idPago");
        if (idPagoStr == null || idPagoStr.isBlank()) {
            cargarFormulario(request);
            request.setAttribute("error", "Debes seleccionar un método de pago.");
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
            return;
        }

        // ─── CONSTRUIR LA LISTA DE DETALLES DEL PEDIDO ───
        List<DetallePedido> detalles = new ArrayList<>(); // Lista de productos vendidos
        BigDecimal total = BigDecimal.ZERO;                // Total acumulado de la venta

        // Recorrer cada producto del carrito
        for (int i = 0; i < ids.length; i++) {
            int idProducto;
            int cantidad;
            BigDecimal precio;
            try {
                // Convertir los valores de texto a números
                idProducto = Integer.parseInt(ids[i]);
                cantidad   = Integer.parseInt(cantidades[i]);
                precio     = new BigDecimal(precios[i]);
            } catch (NumberFormatException e) {
                cargarFormulario(request);
                request.setAttribute("error", "Datos de producto inválidos. Vuelve a agregar los productos.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }

            // Validar que precio y cantidad sean positivos
            if (precio.compareTo(BigDecimal.ZERO) <= 0 || cantidad <= 0) {
                cargarFormulario(request);
                request.setAttribute("error", "Precio o cantidad inválidos para uno de los productos.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }

            // Verificar que haya stock suficiente del producto
            Producto prod = productoDAO.buscarPorId(idProducto);
            if (prod != null && prod.getStock() < cantidad) {
                cargarFormulario(request);
                request.setAttribute("error",
                    "Stock insuficiente para '" + prod.getNombre() +
                    "'. Disponible: " + prod.getStock());
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }

            // Crear la línea de detalle del pedido
            DetallePedido d = new DetallePedido();
            d.setIdProducto(idProducto);       // Qué producto se vendió
            d.setCantidadVendida(cantidad);     // Cuántas unidades
            d.setPrecioUnitario(precio);        // A qué precio unitario
            detalles.add(d);                    // Agregar a la lista

            // Acumular al total: total += precio * cantidad
            total = total.add(precio.multiply(BigDecimal.valueOf(cantidad)));
        }

        // ─── VERIFICAR SI ES VENTA A CRÉDITO (FIADO) ───
        boolean credito     = "on".equals(request.getParameter("fiado")); // Checkbox "fiado"
        String idClienteStr = request.getParameter("idCliente");           // Cliente seleccionado

        // ─── SI SE ENVIÓ UN CLIENTE NUEVO, CREARLO PRIMERO ───
        String nuevoNombre = request.getParameter("nuevoClienteNombre");
        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            // Leer los datos del nuevo cliente
            String nuevoApellido = request.getParameter("nuevoClienteApellido");
            String nuevoCedula   = request.getParameter("nuevoClienteCedula");
            String nuevoTelefono = request.getParameter("nuevoClienteTelefono");
            String nuevoEmail    = request.getParameter("nuevoClienteEmail");

            // Validar los datos del cliente nuevo (nombre, apellido, cédula, teléfono, email)
            String nvErr = validarNuevoCliente(nuevoNombre, nuevoApellido, nuevoCedula, nuevoTelefono, nuevoEmail);
            if (nvErr != null) {
                cargarFormulario(request);
                request.setAttribute("error", nvErr);
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }

            // Verificar que la cédula no esté registrada ya
            if (clienteDAO.cedulaExiste(nuevoCedula.trim())) {
                cargarFormulario(request);
                request.setAttribute("error", "Ya existe un cliente con esa cédula. Búscalo en la lista.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }

            // Crear el objeto Cliente y guardarlo en la BD
            modelos.Cliente nc = new modelos.Cliente();
            nc.setNombre(nuevoNombre.trim());
            nc.setApellido(nuevoApellido != null ? nuevoApellido.trim() : "");
            nc.setCedula(nuevoCedula.trim());
            nc.setEmail(nuevoEmail != null ? nuevoEmail.trim() : null);
            // Insertar en BD y obtener el ID generado automáticamente
            int idNuevo = clienteDAO.crearYObtenerIdCliente(nc);

            if (idNuevo > 0) {
                // Cliente creado exitosamente → usar su ID para el pedido
                idClienteStr = String.valueOf(idNuevo);
                // Si ingresó teléfono, guardarlo en la tabla Telefono
                if (nuevoTelefono != null && !nuevoTelefono.isBlank()) {
                    Telefono t = new Telefono(0, nuevoTelefono.trim(), idNuevo);
                    telefonoDAO.agregar(t); // Insertar teléfono vinculado al cliente
                }
            } else {
                cargarFormulario(request);
                request.setAttribute("error", "No se pudo registrar el cliente nuevo. Intenta de nuevo.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
        }

        // Obtener el ID del cliente "Admin Tienda" para validaciones
        int idAdminTienda = clienteDAO.obtenerIdAdminTienda();

        // Si NO se seleccionó cliente y NO es fiado, asignar "Admin Tienda" automáticamente
        if (!credito && (idClienteStr == null || idClienteStr.isBlank() || "0".equals(idClienteStr))) {
            if (idAdminTienda > 0) {
                idClienteStr = String.valueOf(idAdminTienda);
            }
        }

        // Si es venta a crédito, DEBE tener cliente (para saber quién debe)
        if (credito && (idClienteStr == null || idClienteStr.isBlank() || "0".equals(idClienteStr))) {
            cargarFormulario(request);
            request.setAttribute("error", "Para ventas a crédito debes seleccionar o registrar un cliente.");
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
            return;
        }

        // Bloquear fiado si el cliente seleccionado es "Admin Tienda"
        if (credito && idClienteStr != null && idAdminTienda > 0
                && String.valueOf(idAdminTienda).equals(idClienteStr)) {
            cargarFormulario(request);
            request.setAttribute("error", "No se puede fiar al cliente 'Admin Tienda'. Selecciona o registra un cliente con sus datos.");
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
            return;
        }

        // ─── CONSTRUIR EL PEDIDO ───
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(empleado.getIdUsuario());        // Quién registró la venta
        pedido.setIdPago(Integer.parseInt(idPagoStr));        // Método de pago seleccionado
        pedido.setTotal(total);                               // Total calculado

        // Asignar cliente si se seleccionó uno
        if (idClienteStr != null && !idClienteStr.isBlank() && !"0".equals(idClienteStr)) {
            pedido.setIdCliente(Integer.parseInt(idClienteStr));
        }

        // ─── REGISTRAR LA VENTA EN LA BD ───
        if (credito) {
            // VENTA A CRÉDITO (FIADO)
            pedido.setEstado("credito");
            // registrar() usa transacción atómica: inserta pedido + detalles + actualiza stock
            int idNuevo = pedidoDAO.registrar(pedido, detalles);
            if (idNuevo > 0) {
                // Venta registrada → crear la deuda asociada
                Deuda deuda = new Deuda();
                deuda.setIdPedido(idNuevo);            // Vincular deuda con el pedido
                deuda.setMontoPendiente(total);         // El cliente debe el total de la venta
                deuda.setEstado("activa");              // La deuda está pendiente de pago
                deuda.setAbono(BigDecimal.ZERO);        // No ha abonado nada aún
                deudaDAO.registrarDeuda(deuda);         // Insertar en tabla Deuda
            } else {
                // La venta falló (probablemente stock insuficiente)
                cargarFormulario(request);
                request.setAttribute("error", "No se pudo registrar la venta. Verifica el stock.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
        } else {
            // VENTA DE CONTADO (PAGADO)
            pedido.setEstado("pagado");
            // crear() usa transacción atómica: inserta pedido + detalles + actualiza stock
            if (!pedidoDAO.crear(pedido, detalles)) {
                cargarFormulario(request);
                request.setAttribute("error", "No se pudo registrar la venta. Verifica el stock.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
        }

        // Venta registrada exitosamente → redirigir al historial
        response.sendRedirect(request.getContextPath() + "/PedidoControlador");
    }

    /**
     * Carga los datos necesarios para volver a mostrar el formulario de venta
     * cuando hay un error (productos y métodos de pago).
     */
    private void cargarFormulario(HttpServletRequest request) {
        request.setAttribute("productos",   productoDAO.listarTodos());
        request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
        request.setAttribute("idAdminTienda", clienteDAO.obtenerIdAdminTienda());
    }

    /**
     * Valida los datos de un cliente nuevo antes de insertarlo.
     * Verifica formato de nombre, apellido, cédula, teléfono y email.
     * @return mensaje de error si hay un campo inválido, o null si todo está correcto
     */
    private String validarNuevoCliente(String nombre, String apellido, String cedula,
                                       String telefono, String email) {
        // Nombre: obligatorio, solo letras y espacios, mínimo 2 caracteres
        if (nombre == null || nombre.isBlank())
            return "El nombre del cliente es obligatorio.";
        if (!nombre.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El nombre del cliente solo puede contener letras (mínimo 2 caracteres).";

        // Apellido: obligatorio, solo letras y espacios
        if (apellido == null || apellido.isBlank())
            return "El apellido del cliente es obligatorio.";
        if (!apellido.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El apellido del cliente solo puede contener letras (mínimo 2 caracteres).";

        // Cédula: obligatoria, solo números, entre 8 y 15 dígitos
        if (cedula == null || cedula.isBlank())
            return "La cédula del cliente es obligatoria.";
        if (!cedula.trim().matches("\\d{8,15}"))
            return "La cédula debe contener solo números (mínimo 8, máximo 15 dígitos).";

        // Teléfono: opcional, pero si se ingresa debe ser 7-10 dígitos
        if (telefono != null && !telefono.isBlank() && !telefono.trim().matches("\\d{7,10}"))
            return "El teléfono debe contener solo números (mínimo 7, máximo 10 dígitos).";

        // Email: opcional, pero si se ingresa debe tener formato válido
        if (email != null && !email.isBlank() &&
            !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return "El correo electrónico no es válido. Ejemplo: nombre@gmail.com";

        return null; // Todo está correcto
    }
}
