import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;

public class ApiHandler implements HttpHandler {
    private static final String FILES_FOLDER_NAME = "/Users/falxns/Downloads/KSiS/Lab3/Files/";
    private static HashMap<Integer, String> filesMap = new HashMap<>();

    @Override
    public void handle(HttpExchange httpExchange) {
        String requestMethod = httpExchange.getRequestMethod();
        switch (requestMethod) {
            case "POST":
                handlePostRequest(httpExchange);
                break;
            case "HEAD":
                handleHeadRequest(httpExchange);
                break;
            case "GET":
                handleGetRequest(httpExchange);
                break;
            case "DELETE":
                handleDeleteRequest(httpExchange);
                break;
            default:
                handleUnrecognizedRequest(httpExchange);
                System.out.println("Unrecognized request method:" + httpExchange.getRequestMethod());
        }
        httpExchange.close();
    }

    private void handleUnrecognizedRequest(HttpExchange httpExchange) {
        try {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_IMPLEMENTED, 0);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.flush();
        } catch (IOException ex) {
            System.out.println(this.toString() + ": " + ex.getMessage());
        }
    }

    private void handleDeleteRequest(HttpExchange httpExchange) {
        OutputStream outputStream = httpExchange.getResponseBody();
        final int fileId = Integer.parseInt(httpExchange.getRequestURI().toString().split("/")[2]);
        try {
            boolean wasDeleted = deleteFileFromStorage(fileId);
            if (wasDeleted) {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            } else {
                final String FILE_DELETE_MESSAGE = "File could not be deleted: " + fileId;
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, FILE_DELETE_MESSAGE.length());
                outputStream.write(FILE_DELETE_MESSAGE.getBytes());
                outputStream.flush();
            }
        } catch (Exception ex) {
            System.out.println(this.toString() + ": " + ex.getMessage());
            try {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage().length());
                outputStream.write(ex.getMessage().getBytes());
                outputStream.flush();
            } catch (IOException io) {
                System.out.println(this.toString() + ": " + io.getMessage());
            }
        }
    }

    private boolean deleteFileFromStorage(int fileId) throws IOException {
        File fileObj = new File(filesMap.get(fileId));
        if (fileObj.exists()) {
            boolean wasDeleted = fileObj.delete();
            if (wasDeleted) {
                filesMap.remove(fileId);
            }
            return wasDeleted;
        } else {
            throw new FileNotFoundException("No file with such ID: " + fileId);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) {
        final int fileId = Integer.parseInt(httpExchange.getRequestURI().toString().split("/")[2]);
        OutputStream outputStream = httpExchange.getResponseBody();
        try {
            byte[] fileData = getFileFromStorage(fileId);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, fileData.length);
            outputStream.write(fileData);
            outputStream.flush();
        } catch (Exception ex) {
            try {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage().length());
                outputStream.write(ex.getMessage().getBytes());
                outputStream.flush();
            } catch (IOException io) {
                System.out.println(this.toString() + ": " + io.getMessage());
            }
        }
    }

    private void handleHeadRequest(HttpExchange httpExchange) {
        final int fileId = Integer.parseInt(httpExchange.getRequestURI().toString().split("/")[2]);
        OutputStream outputStream = httpExchange.getResponseBody();
        try {
            final FileData fileData = getFileData(fileId);
            httpExchange.getResponseHeaders().add("FileName", fileData.getFileName());
            httpExchange.getResponseHeaders().add("FileSize", String.valueOf(fileData.getFileSize()));
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            outputStream.flush();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            try {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                outputStream.flush();
            } catch (IOException exception) {
                System.out.println(ex.getMessage());
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private byte[] getFileFromStorage(int fileId) throws IOException {
        File fileObj = new File(filesMap.get(fileId));
        if (fileObj.exists()) {
            FileInputStream fileInputStream = new FileInputStream(fileObj);
            byte[] res = fileInputStream.readAllBytes();
            System.out.println(res.length);
            return res;
        } else {
            throw new FileNotFoundException("No file with such ID: "+ fileId);
        }
    }

    private FileData getFileData(int fileId) throws FileNotFoundException {
        File fileObj = new File(filesMap.get(fileId));
        if (fileObj.exists()) {
            return new FileData(fileObj.getName(), fileObj.length());
        } else {
            throw new FileNotFoundException("No file with such ID: "+ fileId);
        }
    }

    private void handlePostRequest(HttpExchange httpExchange) {
        InputStream inputStream = httpExchange.getRequestBody();
        OutputStream outputStream = httpExchange.getResponseBody();
        try {
            byte[] fileData = inputStream.readAllBytes();
            String fileName = httpExchange.getRequestURI().toString().split("/")[2];
            int fileID = addFileToStorage(fileName, fileData);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, String.valueOf(fileID).length());
            outputStream.write(String.valueOf(fileID).getBytes());
            outputStream.flush();
        } catch (Exception ex) {
            try {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage().length());
                outputStream.write(ex.getMessage().getBytes());
                outputStream.flush();
            } catch (IOException io) {
                System.out.println(this.toString() + ": " + io.getMessage());
            }
        }
    }

    public int addFileToStorage(String fileName, byte[] fileData) throws IOException {
        File fileObj = new File(FILES_FOLDER_NAME + fileName);
        if (fileObj.exists()) {
            throw new FileAlreadyExistsException("File already exists: " + fileName);
        } else {
            boolean wasCreated = fileObj.createNewFile();
            if (wasCreated) {
                FileOutputStream fileWriter = new FileOutputStream(fileObj);
                fileWriter.write(fileData);
                fileWriter.flush();
                fileWriter.close();
            }
        }
        filesMap.put(fileObj.hashCode(), fileObj.getPath());
        return fileObj.hashCode();
    }
}
