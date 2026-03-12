package controladores;

import dao.DeudaDAO;
import dao.MetodoPagoDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import dao.UsuarioDAO;
import modelos.Deuda;
import modelos.DetallePedido;
import modelos.Pedido;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/PedidoControlador")
public class PedidoControlador extends HttpServlet {

    private final PedidoDAO     pedidoDAO     = new PedidoDAO();
    private final ProductoDAO   productoDAO   = new ProductoDAO();
    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();
    private final UsuarioDAO    usuarioDAO    = new UsuarioDAO();
    private final DeudaDAO      deudaDAO      = new DeudaDAO();

    // ─────────────────────────────────────────────
    // Solo admin puede gestionar ventas
    // ─────────────────────────────────────────────
    private boolean verificarAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return false;
        }
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u.getIdRol() != 1) {
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────
    // GET: historial de ventas / formulario nueva venta
    // ─────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        String accion = request.getParameter("accion");

        if ("nuevo".equals(accion)) {
            // Cargar datos para el formulario de registro de venta
            request.setAttribute("productos",   productoDAO.listarTodos());
            request.setAttribute("clientes",    usuarioDAO.listarClientes());
            request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
            request.getRequestDispatcher("/view/registrarventa.jsp").forward(request, response);

        } else {
            // Historial: lista de pedidos con nombre de cliente
            request.setAttribute("pedidos", pedidoDAO.listarConCliente());
            request.getRequestDispatcher("/view/historialventa.jsp").forward(request, response);
        }
    }

    // ─────────────────────────────────────────────
    // POST: registrar venta / cambiar estado
    // ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarAdmin(request, response)) return;

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("cambiarEstado".equals(accion)) {
            int idPedido = Integer.parseInt(request.getParameter("idPedido"));
            pedidoDAO.cambiarEstado(idPedido, request.getParameter("estado"));

        } else {
            // accion=registrar → nueva venta
            Usuario admin = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");

            Pedido pedido = new Pedido();
            pedido.setIdCliente(Integer.parseInt(request.getParameter("idCliente")));
            pedido.setIdUsuario(admin.getIdUsuario());
            pedido.setIdPago(Integer.parseInt(request.getParameter("idPago")));

            // Construir detalles y recalcular total en servidor (no confiar en JS)
            String[] ids        = request.getParameterValues("idProducto");
            String[] cantidades = request.getParameterValues("cantidad");
            String[] precios    = request.getParameterValues("precioUnitario");

            List<DetallePedido> detalles = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            if (ids != null) {
                for (int i = 0; i < ids.length; i++) {
                    DetallePedido d = new DetallePedido();
                    d.setIdProducto(Integer.parseInt(ids[i]));
                    d.setCantidadVendida(Integer.parseInt(cantidades[i]));
                    d.setPrecioUnitario(new BigDecimal(precios[i]));
                    detalles.add(d);
                    total = total.add(
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidadVendida()))
                    );
                }
            }

            pedido.setTotal(total);
            boolean fiada = "on".equals(request.getParameter("fiado"));

            if (fiada) {
                // Venta fiada: estado pendiente + crear registro de Deuda
                pedido.setEstado("pendiente");
                int idNuevo = pedidoDAO.registrar(pedido, detalles);
                if (idNuevo > 0) {
                    Deuda deuda = new Deuda();
                    deuda.setIdPedido(idNuevo);
                    deuda.setMontoPendiente(total);
                    deuda.setEstado("pendiente");
                    deuda.setAbono(BigDecimal.ZERO);
                    deudaDAO.registrarDeuda(deuda);
                }
            } else {
                // Venta normal contado
                pedido.setEstado("completado");
                pedidoDAO.crear(pedido, detalles);
            }
        }

        response.sendRedirect(request.getContextPath() + "/PedidoControlador");
    }
}
