package controladores;

import dao.MetodoPagoDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
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

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getIdRol() != 1) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        request.setAttribute("pedidos", pedidoDAO.listarTodos());
        request.setAttribute("productos", productoDAO.listarTodos());
        request.setAttribute("metodosPago", metodoPagoDAO.listarTodos());
        request.getRequestDispatcher("/WEB-INF/view/pedidos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/LoginControlador");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getIdRol() != 1) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String accion = request.getParameter("accion");

        if ("cambiarEstado".equals(accion)) {
            int idPedido = Integer.parseInt(request.getParameter("idPedido"));
            String estado = request.getParameter("estado");
            pedidoDAO.cambiarEstado(idPedido, estado);
        } else {
            // Crear nuevo pedido
            Pedido pedido = new Pedido();
            pedido.setIdCliente(Integer.parseInt(request.getParameter("idCliente")));
            pedido.setIdUsuario(usuario.getIdUsuario());
            pedido.setIdPago(Integer.parseInt(request.getParameter("idPago")));
            pedido.setEstado("pendiente");

            String[] productos   = request.getParameterValues("idProducto");
            String[] cantidades  = request.getParameterValues("cantidad");
            String[] precios     = request.getParameterValues("precioUnitario");

            List<DetallePedido> detalles = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            if (productos != null) {
                for (int i = 0; i < productos.length; i++) {
                    DetallePedido d = new DetallePedido();
                    d.setIdProducto(Integer.parseInt(productos[i]));
                    d.setCantidadVendida(Integer.parseInt(cantidades[i]));
                    d.setPrecioUnitario(new BigDecimal(precios[i]));
                    detalles.add(d);
                    total = total.add(d.getPrecioUnitario()
                            .multiply(BigDecimal.valueOf(d.getCantidadVendida())));
                }
            }

            pedido.setTotal(total);
            pedidoDAO.crear(pedido, detalles);
        }

        response.sendRedirect(request.getContextPath() + "/PedidoControlador");
    }
}
