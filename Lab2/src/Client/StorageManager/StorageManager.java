package Client.StorageManager;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StorageManager {
    final String API_URL = "http://192.168.100.4:7777/Files/";
    private final String[] forbiddenExtensions = {".exe", ".jar"};
    private int operationsInProgress = 0;
    public final int fileSizeLimitMB = 20;
    public final int totalFileSizeLimitMB = 50;
    public double totalFileSize = 0;

    public boolean isZip(String fileName) {
        Pattern pattern = Pattern.compile("\\.zip$");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public boolean checkZipFiles(File file) {
        try(ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            String name;
            while((entry = zipInputStream.getNextEntry()) != null) {
                name = entry.getName();
                if (!isValidFile(name)) {
                    return false;
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return true;
    }

    public boolean isValidFile(String fileName) {
        for (String extension : forbiddenExtensions) {
            Pattern pattern = Pattern.compile(extension + "$");
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidFileSize(double size) {
        return size <= fileSizeLimitMB;
    }

    public boolean isValidTotalFileSize(double size) {
        return (totalFileSize + size) <= totalFileSizeLimitMB;
    }

    private boolean isComplete() {
        return operationsInProgress == 0;
    }

    public void deleteFileFromStorage(StorageFile file, Button sendButton) {
        operationsInProgress++;
        sendButton.setEnabled(false);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + file.getID()))
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        operationsInProgress--;
                        if (isComplete()) {
                            sendButton.setEnabled(true);
                        }
                    };
                });
    }

    public void getFileFromStorage(StorageFile file){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + file.getID()))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        File fileObj = new File(file.getOriginalName());
                        if (fileObj.exists()) {
                            System.out.println("File storage error: file already exists: " + file.getOriginalName());
                        } else {
                            try {
                                boolean wasCreated = fileObj.createNewFile();
                                if (wasCreated) {
                                    FileOutputStream fileWriter = new FileOutputStream(fileObj);
                                    fileWriter.write(response.body());
                                    fileWriter.flush();
                                    fileWriter.close();
                                }
                            } catch (Exception ex) {
                                System.out.println("File storage error: " + ex.getMessage());
                            }
                        }
                    };
                });
    }

    public void putFileToStorage(File file, StorageFile uniqueFile, Button sendButton) {
        operationsInProgress++;
        sendButton.setEnabled(false);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + generateName()))
                    .POST(HttpRequest.BodyPublishers.ofByteArray((new FileInputStream(file)).readAllBytes()))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            uniqueFile.setID(Integer.parseInt(response.body()));
                            operationsInProgress--;
                            if (isComplete()) {
                                sendButton.setEnabled(true);
                            }
                        };
                    });
        } catch (Exception ex) {
            System.out.println("File storage error: " + ex.getMessage());
        }
    }

    private static String generateName() {
        final int N = 20;
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz"
                + "0123456789";
        StringBuilder sb = new StringBuilder(N);
        for (int i = 0; i < N; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}