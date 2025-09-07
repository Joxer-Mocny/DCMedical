package org.example;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // Generate a random UUID for this request
        String uuid = UUID.randomUUID().toString();

        // Create an HTTP client
        OkHttpClient client = new OkHttpClient();

        // Build JSON body to request the challenge
        String jsonBody = "{ \"uuid\": \"" + uuid + "\", \"user\": \"\" }";
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        // Build POST request to fetch the challenge
        Request request = new Request.Builder()
                .url("https://zadanie.openmed.sk/challenge-me-easy")
                .post(body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Accept", "application/json")
                .build();

        try {
            // Execute the request and get the response
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                System.out.println("Request failed: " + response.code());
                return;
            }

            // Parse JSON response
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            int w = json.getInt("set_x"); // matrix width
            int h = json.getInt("set_y"); // matrix height
            int i = json.getInt("set_z"); // target index

            // Calculate neighbors of the target index
            int[] neighbors = calculateNeighbors(h, w, i);
            StringBuilder neighborsStr = new StringBuilder();
            for (int j = 0; j < neighbors.length; j++) {
                neighborsStr.append(neighbors[j]);
                if (j < neighbors.length - 1) neighborsStr.append(",");
            }

            // Build the full torus matrix string for hash calculation
            String matrixStr = buildMatrixString(h, w);
            // Calculate SHA-256 hash encoded in Base64
            String hash = calculateHash(matrixStr);

            // Prepare JSON body with result and hash
            JSONObject resultJson = new JSONObject();
            resultJson.put("uuid", uuid);
            resultJson.put("result", neighborsStr.toString());
            resultJson.put("hash", hash);

            // Build POST request to send the result
            RequestBody resultBody = RequestBody.create(resultJson.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request resultRequest = new Request.Builder()
                    .url("https://zadanie.openmed.sk/challenge-me-easy")
                    .post(resultBody)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Accept", "application/json")
                    .build();

            // Execute the result request and print the response
            Response resultResponse = client.newCall(resultRequest).execute();
            System.out.println("API response: " + resultResponse.code() + " " + resultResponse.body().string());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Calculate the 8 neighbors of a given index in a torus matrix
    public static int[] calculateNeighbors(int h, int w, int index) {
        // Column (dx) and row (dy) offsets for 8 neighbors
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1 }; // horizontal movement
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1}; // vertical movement

        int[] neighbors = new int[8]; // array to store neighbor indices
        int row  = index / w;         // calculate row of the target cell
        int col = index % w;          // calculate column of the target cell

        for (int j = 0; j < 8; j++) {
            // Wrap around edges (torus)
            int newRow = (row + dy[j] + h) % h;
            int newCol = (col + dx[j] + w) % w;

            // Convert 2D coordinates back to linear index
            neighbors[j] = newRow * w + newCol;
        }
        return neighbors;
    }

    // Build a string representing the full torus matrix for hashing
    public static String buildMatrixString(int h, int w) {
        StringBuilder sb = new StringBuilder();
        // Loop from -1 to h/w to include wrapped edges
        for (int row = -1; row <= h; row++) {
            for (int col = -1; col <= w; col++) {
                int wrappedRow = (row + h) % h;
                int wrappedCol = (col + w) % w;
                sb.append(wrappedRow * w + wrappedCol);
                if (col != w) sb.append(",");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    // Calculate SHA-256 hash of the matrix string and encode it to Base64
    public static String calculateHash(String matrixString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(matrixString.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
