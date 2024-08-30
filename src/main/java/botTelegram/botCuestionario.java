package botTelegram;

import model.RespuestaModel;
import model.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.UserRespuestas;
import service.UserService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class botCuestionario extends TelegramLongPollingBot {
    private Map<Long, String> estadoConversacion = new HashMap<>();
    User usuarioConectado = null;
    UserService userService = new UserService();

    private final Map<Long, Integer> indicePregunta = new HashMap<>();
    private final Map<Long, String> seccionActiva = new HashMap<>();
    private final Map<String, String[]> preguntas = new HashMap<>();

    public botCuestionario() {

        preguntas.put("SECTION_1", new String[]{"🤦‍♂️1.1- Estas aburrido?", "😂😂 1.2- Te bañaste hoy?", "🤡🤡 Pregunta 1.3"});
        preguntas.put("SECTION_2", new String[]{"Pregunta 2.1", "Pregunta 2.2", "Pregunta 2.3"});
        preguntas.put("SECTION_3", new String[]{"Pregunta 3.1", "Pregunta 3.2", "Pregunta 3.3"});
        preguntas.put("SECTION_4", new String[]{"Pregunta 4.1", "4.1- Estas bien?😃","Si dijiste si, esoo. Si dijste que no, deja de sentirte mal ;D", "Pregunta 4.2", "Cuantos años tienes (pregunta seria)🤔👀?", "Pregunta 4.3","Ya comiste?🙌","Si dijiste si, provecho pa. Si dijiste no, pos come", "Pregunta 4.4", "Eres feliz?👀","Si dijiste que si, me alegro por ti. Si dijiste no, recuerda que la vida es de subidas y bajadas, solamente no te rindas y sigue adelante, tu puedes campeon o campeona👌" });
    }
    @Override
    public String getBotUsername() {
        return "@Mokssss_bot";
    }

    @Override
    public String getBotToken() {
        return "7299842197:AAHjw1momoHgSPebSpxEM-gA8kxJlg33pHc";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                String userFirstName = update.getMessage().getFrom().getFirstName();
                String userLastName = update.getMessage().getFrom().getLastName();
                String nickName = update.getMessage().getFrom().getUserName();

                //Verificacion para comprobar si el usuario esta registrado
                String state = estadoConversacion.getOrDefault(chatId, "");
                usuarioConectado = userService.getUserByTelegramId(chatId);

                if (usuarioConectado == null) {
                    //Proceso para el registro
                    if (state.isEmpty()) {
                        sendText(chatId, "Hola " + formatUserInfo(userFirstName, userLastName, nickName) + ", Tu usuuario no esa registrado en el sistema. Por favor ingresa tu correo electronico:");
                        estadoConversacion.put(chatId, "ESPERANDO_CORREO.");
                        return;
                    } else if (state.equals("ESPERANDO_CORREO.")) {
                        processEmailInput(chatId, messageText);
                        return;
                    }
                } else {
                    //Usuario Registrado
                    if (messageText.equals("/menu")) {
                        sendMenu(chatId);
                        return;
                    } else if (seccionActiva.containsKey(chatId)) {
                        enviarRespuesta(seccionActiva.get(chatId), indicePregunta.get(chatId), messageText, chatId);
                        manejaCuestionario(chatId, messageText);
                        return;
                    } else {
                        sendText(chatId, "Hola " + formatUserInfo(userFirstName, userLastName, nickName) + ", envía '/menu' para iniciar el cuestionario. 🙂");
                    }
                }
            } else if (update.hasCallbackQuery()) {
                //Control de Respuestas
                String callbackData = update.getCallbackQuery().getData();
                long chatId = update.getCallbackQuery().getMessage().getChatId();
                inicioCuestionario(chatId, callbackData);
            }
        } catch (Exception e) {
            //Control de errores
            long chatId = update.getMessage().getChatId();
            sendText(chatId, "ERROR, Por favor intenta de nuevo.");
            e.printStackTrace();  // Loguear la excepción
        }
    }

    private void sendMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona una sección:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        //Botones del menu
        rows.add(crearFilaBoton("Sección 1", "SECTION_1"));
        rows.add(crearFilaBoton("Sección 2", "SECTION_2"));
        rows.add(crearFilaBoton("Sección 3", "SECTION_3"));
        rows.add(crearFilaBoton("Sección 4", "SECTION_4"));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private List<InlineKeyboardButton> crearFilaBoton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    private void inicioCuestionario(long chatId, String section) {
        seccionActiva.put(chatId, section);
        indicePregunta.put(chatId, 0);
        enviarPregunta(chatId);
    }

    private void enviarPregunta(long chatId) {
        String seccion = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        String[] questions = preguntas.get(seccion);

        if (index < questions.length) {
            sendText(chatId, questions[index]);
        } else {
            sendText(chatId, "¡Cuestionario Completado!");
            seccionActiva.remove(chatId);
            indicePregunta.remove(chatId);
        }
    }

    private void manejaCuestionario(long chatId, String response) {
        String section = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        if (indicePregunta.get(chatId) == 1) {
            int intresponse = Integer.parseInt(response);
            if (intresponse < 5) {
                sendText(chatId, "Tu respuesta: " + response);
                sendText(chatId, "Muy joven no crees?\n Si te equivocaste, ingresa otra vez la edad.");
                enviarPregunta(chatId);
            } else if (intresponse > 95) {
                sendText(chatId, "Tu respuesta: " + response);
                sendText(chatId, "¿Ya estas jubilado, verdad?.\n ingresa otra edad.");
                enviarPregunta(chatId);
            } else {
                siguientepregunta(chatId, response, index);
            }
        } else {
            siguientepregunta(chatId, response, index);
        }
    }

    private void enviarRespuesta(String seccion, Integer preguntaId, String response, Long telegramId) {

        RespuestaModel respuestaModel = new RespuestaModel();

        //Colocando valores en ciertas instancias
        respuestaModel.setSeccion(seccion);
        respuestaModel.setPreguntaId(preguntaId);
        respuestaModel.setRespuestaTexto(response);
        respuestaModel.setTelegramId(telegramId);

        try {
            UserRespuestas.createRespuesta(respuestaModel);
            System.out.println("Respuesta creada.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void siguientepregunta(long chatId, String response, int index) {
        sendText(chatId, "Tu respuesta: " + response);
        indicePregunta.put(chatId, index + 1);
        enviarPregunta(chatId);
    }

    private String formatUserInfo(String firstName, String lastName, String userName) {
        return firstName + " " + lastName;
    }

    private void processEmailInput(long chatId, String email) {
        sendText(chatId, " Correo recibido " + email);
        estadoConversacion.remove(chatId);
        try {
            usuarioConectado = userService.getUserByEmail(email);
        } catch (Exception e) {
                System.err.println("ERROR" + e.getMessage());
            e.printStackTrace();
        }
        if (usuarioConectado == null) {
            sendText(chatId, "Correo no encontrado en el sistema.");
        } else {
            usuarioConectado.setTelegramid(chatId);
            try {
                userService.updateUser(usuarioConectado);
            } catch (Exception e) {
                System.err.println(" " + e.getMessage());
                e.printStackTrace();
            }
            sendText(chatId, "Usuario actualizado.");
        }
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
