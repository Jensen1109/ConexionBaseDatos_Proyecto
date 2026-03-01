package controladores;

import dao.ImagenDAO;
import modelos.Imagen;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/ImagenControlador")
public class ImagenControlador extends HttpServlet {

    private final ImagenDAO imagenDAO = new ImagenDAO();

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

        String idParam = request.getParameter("idProducto");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/ProductoControlador");
            return;
        }

        int idProducto = Integer.parseInt(idParam);
        request.setAttribute("imagenes", imagenDAO.listarPorProducto(idProducto));
        request.setAttribute("idProducto", idProducto);
        request.getRequestDispatcher("/WEB-INF/view/imagenes.jsp").forward(request, response);
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
        int idProducto = Integer.parseInt(request.getParameter("idProducto"));

        if ("eliminar".equals(accion)) {
            int idImagen = Integer.parseInt(request.getParameter("idImagen"));
            imagenDAO.eliminar(idImagen);
        } else {
            String url = request.getParameter("url");
            if (url != null && !url.isBlank()) {
                Imagen img = new Imagen();
                img.setIdProducto(idProducto);
                img.setUrl(url.trim());
                imagenDAO.guardar(img);
            }
        }

        response.sendRedirect(request.getContextPath() +
                "/ImagenControlador?idProducto=" + idProducto);
    }
}
