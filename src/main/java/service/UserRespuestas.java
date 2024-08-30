package service;

import dao.ResuestasDao;
import db.DatabaseConnection;
import db.TransactionManager;
import model.RespuestaModel;

import java.sql.Connection;
import java.sql.SQLException;

public class UserRespuestas {
    private ResuestasDao respuestaDao = new ResuestasDao();

    public static void createRespuesta(RespuestaModel respuestaModel) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            TransactionManager tm = new TransactionManager(connection);
            tm.beginTransaction();
            try {
                ResuestasDao.insertRespuesta(respuestaModel);
                tm.commit();
            } catch (SQLException e) {
                tm.rollback();
                throw e;
            }
        }
    }
}
