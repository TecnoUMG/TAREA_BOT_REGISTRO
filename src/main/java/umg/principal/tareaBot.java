package umg.principal;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class tareaBot extends TelegramLongPollingBot {

    private final Map<Long, String> usuariosRegistrados = new HashMap<>();
    private static final int MAX_USUARIOS = 4;

    @Override
    public String getBotUsername() {
        return "Mokssss_bot";
    }

    @Override
    public String getBotToken() {
        return "7299842197:AAHjw1momoHgSPebSpxEM-gA8kxJlg33pHc";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            Message message = update.getMessage();
            String text = message.getText();
            Long chatId = message.getChatId();
            User user = message.getFrom();
            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            String fullName = firstName;
            if (lastName != null) {
                fullName += " " + lastName;
            }

            if (!usuariosRegistrados.containsKey(chatId) && usuariosRegistrados.size() < MAX_USUARIOS) {
                usuariosRegistrados.put(chatId, fullName);
            }


            if (text.startsWith("/info")) {
                String carnet = "0905-23-18205";
                String semestre = "Cuarto semestre";
                String response = "Informacion PERSONAL\n" +
                        "Nombre: " + fullName + "\n" +
                        "Carnet: " + carnet + "\n" +
                        "Semestre: " + semestre;
                execute(new SendMessage(chatId.toString(), response));

            } else if (text.startsWith("/progra")) {
                String response = "Se me hace muy interesante y a la vez desafiante.";
                execute(new SendMessage(chatId.toString(), response));

            } else if (text.startsWith("/hola")) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm");
                String formattedDate = now.format(formatter);
                String response = "Hola, " + fullName + "! Hoy es " + formattedDate + ".";
                execute(new SendMessage(chatId.toString(), response));

            } else if (text.startsWith("/cambio")) {
                try {
                    String[] parts = text.split(" ");
                    double euros = Double.parseDouble(parts[1]);
                    double tipoCambio = 8.90;
                    double quetzales = euros * tipoCambio;
                    String response = String.format("Son %.2f quetzales.", quetzales);
                    execute(new SendMessage(chatId.toString(), response));
                } catch (NumberFormatException e) {
                    execute(new SendMessage(chatId.toString(), "Por favor, ingresa un número válido luego de /cambio."));
                }

            } else if (text.startsWith("/grupal")) {
                String[] parts = text.split(" ", 3);

                if (parts.length < 3) {
                    execute(new SendMessage(chatId.toString(), "Uso correcto: /grupal [ID | todos] [mensaje]"));
                    return;
                }

                String target = parts[1];
                String mensajeGrupal = parts[2];

                if (target.equals("Todos")) {

                    for (Long id : usuariosRegistrados.keySet()) {
                        execute(new SendMessage(id.toString(), mensajeGrupal));
                    }
                } else {
                    try {
                        Long targetId = Long.parseLong(target);
                        if (usuariosRegistrados.containsKey(targetId)) {
                            execute(new SendMessage(targetId.toString(), mensajeGrupal));
                        } else {
                            execute(new SendMessage(chatId.toString(), "El ID no está registrado."));
                        }
                    } catch (NumberFormatException e) {
                        execute(new SendMessage(chatId.toString(), "ID no válido."));
                    }
                }
            } else {

                String response = "Hola, " + fullName + "! Tu mensaje tiene " + text.length() + " caracteres.";
                execute(new SendMessage(chatId.toString(), response));
            }


            System.out.println("Usuario: " + fullName);
            System.out.println("ID de usuario: " + chatId);
            System.out.println("Mensaje: " + text);
            System.out.println("Longitud o medida: " + text.length());

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
