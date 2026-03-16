package controladores;

import dao.ClienteDAO;
import dao.DeudaDAO;
import dao.MetodoPagoDAO;
import dao.PedidoDAO;
import dao.PermisosDAO;
import dao.ProductoDAO;
import dao.TelefonoDAO;
import modelos.Deuda;
import modelos.DetallePedido;
import modelos.Pedido;
import modelos.Producto;
import modelos.Telefono;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de ventas y historial de pedidos.
 * Admin y empleados pueden registrar ventas.
 * Solo admin puede ver el historial completo.
 */
@WebServlet("/PedidoControlador")
public class PedidoControlador extends HttpServlet {

    private final PedidoDAO     pedidoDAO     = new PedidoDAO();
    private final ProductoDAO   productoDAO   = new ProductoDAO();
    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();
    private final ClienteDAO    clienteDAO    = new ClienteDAO();
    private final DeudaDAO      deudaDAO      = new DeudaDAO();
    private final PermisosDAO   permisosDAO   = new PermisosDAO();
    private final TelefonoDAO   telefonoDAO   = new TelefonoDAO();

    private boolean verificarSesion(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            res.sendRedirect(req.getContextPath() + "/LoginControlador");
            return false;
        }
        return true;
    }

    private boolean verificarPermiso(HttpServletRequest req, HttpServletResponse res,
                                      String permiso, String redirectPath) throws IOException {
        if (!verificarSesion(req, res)) return false;
        Usuario u = (Usuario) req.getSession(false).getAttribute("usuarioLogueado");
        if (!permisosDAO.tienePermiso(u.getIdRol(), permiso)) {
            res.sendRedirect(req.getContextPath() + redirectPath);
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesion(request, response)) return;

        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            if (!verificarPermiso(request, response, "REGISTRAR_VENTA", "/ProductoControlador")) return;
            request.setAttribute("productos",   productoDAO.listarTodos());
            request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);

        } else {
            // Historial solo con permiso VER_HISTORIAL
            if (!verificarPermiso(request, response, "VER_HISTORIAL", "/PedidoControlador?accion=nuevo")) return;

            String fechaIniStr = request.getParameter("fechaInicio");
            String fechaFinStr = request.getParameter("fechaFin");

            List<Pedido> pedidos;
            if (fechaIniStr != null && !fechaIniStr.isBlank() &&
                fechaFinStr  != null && !fechaFinStr.isBlank()) {
                pedidos = pedidoDAO.listarPorFechas(
                    LocalDate.parse(fechaIniStr),
                    LocalDate.parse(fechaFinStr));
                request.setAttribute("fechaInicio", fechaIniStr);
                request.setAttribute("fechaFin",    fechaFinStr);
            } else {
                pedidos = pedidoDAO.listarConCliente();
            }

            request.setAttribute("pedidos", pedidos);
            request.getRequestDispatcher("/view/historialventa.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesion(request, response)) return;
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if ("cambiarEstado".equals(accion)) {
            if (!verificarPermiso(request, response, "VER_HISTORIAL", "/PedidoControlador?accion=nuevo")) return;
            int idPedido = Integer.parseInt(request.getParameter("idPedido"));
            pedidoDAO.cambiarEstado(idPedido, request.getParameter("estado"));
            response.sendRedirect(request.getContextPath() + "/PedidoControlador");
            return;
        }

        // Registrar nueva venta — requiere permiso REGISTRAR_VENTA
        if (!verificarPermiso(request, response, "REGISTRAR_VENTA", "/ProductoControlador")) return;
        Usuario empleado = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");

        String[] ids        = request.getParameterValues("idProducto");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios    = request.getParameterValues("precioUnitario");

        if (ids == null || ids.length == 0) {
            cargarFormulario(request);
            request.setAttribute("error", "Debes agregar al menos un producto.");
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
            return;
        }

        String idPagoStr = request.getParameter("idPago");
        if (idPagoStr == null || idPagoStr.isBlank()) {
            cargarFormulario(request);
            request.setAttribute("error", "Debes seleccionar un método de pago.");
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
            return;
        }

        // Construir detalles y validar stock
        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < ids.length; i++) {
            int idProducto;
            int cantidad;
            BigDecimal precio;
            try {
                idProducto = Integer.parseInt(ids[i]);
                cantidad   = Integer.parseInt(cantidades[i]);
                precio     = new BigDecimal(precios[i]);
            } catch (NumberFormatException e) {
                cargarFormulario(request);
                request.setAttribute("error", "Datos de producto inválidos. Vuelve a agregar los productos.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
            if (precio.compareTo(BigDecimal.ZERO) <= 0 || cantidad <= 0) {
                cargarFormulario(request);
                request.setAttribute("error", "Precio o cantidad inválidos para uno de los productos.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }

            Producto prod = productoDAO.buscarPorId(idProducto);
            if (prod != null && prod.getStock() < cantidad) {
                cargarFormulario(request);
                request.setAttribute("error",
                    "Stock insuficiente para '" + prod.getNombre() +
                    "'. Disponible: " + prod.getStock());
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }

            DetallePedido d = new DetallePedido();
            d.setIdProducto(idProducto);
            d.setCantidadVendida(cantidad);
            d.setPrecioUnitario(precio);
            detalles.add(d);
            total = total.add(precio.multiply(BigDecimal.valueOf(cantidad)));
        }

        boolean credito     = "on".equals(request.getParameter("fiado"));
        String idClienteStr = request.getParameter("idCliente");

        // Si se envió un cliente nuevo, crearlo primero y usar su ID
        String nuevoNombre = request.getParameter("nuevoClienteNombre");
        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            String nuevoApellido = request.getParameter("nuevoClienteApellido");
            String nuevoCedula   = request.getParameter("nuevoClienteCedula");
            String nuevoTelefono = request.getParameter("nuevoClienteTelefono");
            String nuevoEmail    = request.getParameter("nuevoClienteEmail");

            // Validaciones backend
            String nvErr = validarNuevoCliente(nuevoNombre, nuevoApellido, nuevoCedula, nuevoTelefono, nuevoEmail);
            if (nvErr != null) {
                cargarFormulario(request);
                request.setAttribute("error", nvErr);
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
            if (clienteDAO.cedulaExiste(nuevoCedula.trim())) {
                cargarFormulario(request);
                request.setAttribute("error", "Ya existe un cliente con esa cédula. Búscalo en la lista.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
            modelos.Cliente nc = new modelos.Cliente();
            nc.setNombre(nuevoNombre.trim());
            nc.setApellido(nuevoApellido != null ? nuevoApellido.trim() : "");
            nc.setCedula(nuevoCedula.trim());
            nc.setEmail(nuevoEmail != null ? nuevoEmail.trim() : null);
            int idNuevo = clienteDAO.crearYObtenerIdCliente(nc);
            if (idNuevo > 0) {
                idClienteStr = String.valueOf(idNuevo);
                // Guardar teléfono en tabla Telefono
                if (nuevoTelefono != null && !nuevoTelefono.isBlank()) {
                    Telefono t = new Telefono(0, nuevoTelefono.trim(), idNuevo);
                    telefonoDAO.agregar(t);
                }
            } else {
                cargarFormulario(request);
                request.setAttribute("error", "No se pudo registrar el cliente nuevo. Intenta de nuevo.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
        }

        if (credito && (idClienteStr == null || idClienteStr.isBlank() || "0".equals(idClienteStr))) {
            cargarFormulario(request);
            request.setAttribute("error", "Para ventas a crédito debes seleccionar o registrar un cliente.");
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
            return;
        }

        Pedido pedido = new Pedido();
        pedido.setIdUsuario(empleado.getIdUsuario());
        pedido.setIdPago(Integer.parseInt(idPagoStr));
        pedido.setTotal(total);

        if (idClienteStr != null && !idClienteStr.isBlank() && !"0".equals(idClienteStr)) {
            pedido.setIdCliente(Integer.parseInt(idClienteStr));
        }

        if (credito) {
            pedido.setEstado("credito");
            int idNuevo = pedidoDAO.registrar(pedido, detalles);
            if (idNuevo > 0) {
                Deuda deuda = new Deuda();
                deuda.setIdPedido(idNuevo);
                deuda.setMontoPendiente(total);
                deuda.setEstado("activa");
                deuda.setAbono(BigDecimal.ZERO);
                deudaDAO.registrarDeuda(deuda);
            } else {
                cargarFormulario(request);
                request.setAttribute("error", "No se pudo registrar la venta. Verifica el stock.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
        } else {
            pedido.setEstado("pagado");
            if (!pedidoDAO.crear(pedido, detalles)) {
                cargarFormulario(request);
                request.setAttribute("error", "No se pudo registrar la venta. Verifica el stock.");
                request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/PedidoControlador");
    }

    private void cargarFormulario(HttpServletRequest request) {
        request.setAttribute("productos",   productoDAO.listarTodos());
        request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
    }

    private String validarNuevoCliente(String nombre, String apellido, String cedula,
                                       String telefono, String email) {
        if (nombre == null || nombre.isBlank())
            return "El nombre del cliente es obligatorio.";
        if (!nombre.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El nombre del cliente solo puede contener letras (mínimo 2 caracteres).";

        if (apellido == null || apellido.isBlank())
            return "El apellido del cliente es obligatorio.";
        if (!apellido.trim().matches("[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,60}"))
            return "El apellido del cliente solo puede contener letras (mínimo 2 caracteres).";

        if (cedula == null || cedula.isBlank())
            return "La cédula del cliente es obligatoria.";
        if (!cedula.trim().matches("\\d{8,15}"))
            return "La cédula debe contener solo números (mínimo 8, máximo 15 dígitos).";

        if (telefono != null && !telefono.isBlank() && !telefono.trim().matches("\\d{1,15}"))
            return "El teléfono solo puede contener números (máximo 15 dígitos).";

        if (email != null && !email.isBlank() &&
            !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return "El correo electrónico no es válido. Ejemplo: nombre@gmail.com";

        return null;
    }
}
