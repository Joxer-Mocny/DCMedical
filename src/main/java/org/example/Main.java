package org.example;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // Generate a random UUID for identifying this request
        String uuid = UUID.randomUUID().toString();
        // Create an OkHttp client – used to send HTTP requests
        OkHttpClient client = new OkHttpClient();
        // JSON body of the POST request – contains uuid and empty user field
        String jsonBody = "{ \"uuid\": \"" + uuid + "\", \"user\": \"\" }";
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        // Create a POST request to the API endpoint with the Content-Type header
        Request request = new Request.Builder()
                .url("https://zadanie.openmed.sk")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            // Execute the request synchronously – waits for the response
            Response response = client.newCall(request).execute();
            // Check if the request was successful (status code 2xx)
            if (!response.isSuccessful()) {
                System.out.println("Request failed: " + response.code());
                return;
            }
            // Get the response body as a string
            String responseBody = response.body().string();
            // Parse the JSON response
            JSONObject json = new JSONObject(responseBody);
            // Extract h (rows), w (columns), and i (cell index) from server response
            int h = json.getInt("set_y"); // number of rows
            int w = json.getInt("set_x"); // number of columns
            int i = json.getInt("set_z"); // index of the cell whose neighbors we want

            System.out.println("Received from API: h=" + h + ", w=" + w + ", i=" + i);
            // Compute the neighbors of the cell using our method
            int[] neighbors = calculateNeighbors(h, w, i);
            // Print the neighbors to console
            System.out.println("Neighbors: " + Arrays.toString(neighbors));

        } catch (IOException e) {
            e.printStackTrace(); // Print error if HTTP communication fails
        }
    }

    public static int[] calculateNeighbors(int h, int w, int index) {
        // Column (dx) and row (dy) offsets for the 8 neighbors
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1 }; // column offsets
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1}; // row offsets

        int[] neighbors = new int[8]; // array for results
        int row  = index / w;         // row of the cell
        int col = index % w;          // column of the cell

        for (int j = 0; j < 8; j++) {
            // Wrap around edges to simulate torus
            int newRow = (row + dy[j] + h) % h;
            int newCol = (col + dx[j] + w) % w;
            neighbors[j] = newRow * w + newCol; // calculate linear index
        }
        return neighbors;
    }
}
