package dao;
import db.DatabaseConnection;
import model.RespuestaModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class ResuestasDao {
    public static void insertRespuesta(model.RespuestaModel respuestaModel) throws SQLException {
        String query = "INSERT INTO tb_respuestas (seccion, telegram_id, pregunta_id, respuesta_texto) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            // Usa los métodos de instancia del objeto respuestaModel
            statement.setString(1, respuestaModel.getSeccion());
            statement.setLong(2, respuestaModel.getTelegramId());
            statement.setInt(3, respuestaModel.getPreguntaId());
            statement.setString(4, respuestaModel.getRespuestaTexto());
            statement.executeUpdate();
        }
    }
}


