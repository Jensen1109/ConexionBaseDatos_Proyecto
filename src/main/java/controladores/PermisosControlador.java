package controladores;

import dao.PermisosDAO;
import modelos.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/PermisosControlador")
public class PermisosControlador extends HttpServlet {

    private final PermisosDAO permisosDAO = new PermisosDAO();

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

        request.setAttribute("permisos", permisosDAO.listarTodos());
        request.setAttribute("roles",    permisosDAO.listarRoles());

        String idRolParam = request.getParameter("idRol");
        if (idRolParam != null && !idRolParam.isBlank()) {
            int idRol = Integer.parseInt(idRolParam);
            request.setAttribute("idRolSeleccionado", idRol);
            request.setAttribute("permisosDelRol", permisosDAO.listarIdPermisosPorRol(idRol));
        }

        request.getRequestDispatcher("/WEB-INF/view/permisos.jsp").forward(request, response);
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

        int idRol     = Integer.parseInt(request.getParameter("idRol"));
        int idPermiso = Integer.parseInt(request.getParameter("idPermiso"));
        String accion = request.getParameter("accion");

        if ("quitar".equals(accion)) {
            permisosDAO.quitarPermiso(idRol, idPermiso);
        } else {
            permisosDAO.asignarPermiso(idRol, idPermiso);
        }

        response.sendRedirect(request.getContextPath() +
                "/PermisosControlador?idRol=" + idRol);
    }
}
