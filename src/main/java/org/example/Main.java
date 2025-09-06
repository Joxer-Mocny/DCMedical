package org.example;


import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
      int h = 1; //number of rows
      int w = 1; // number of columns
      int i = 0; // the index of the cell to find the neighbors

      int[] neighbors = calculateNeighbors(h,w,i);
      System.out.println(Arrays.toString(neighbors));
  }

  public static int[] calculateNeighbors(int h, int w, int index) {
      // relative positions of the 8 neighbors (top-left -> bottom-right)
      int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1 }; //columns offsets
      int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1}; //row offsets

      int[] neighbors = new int[8];
      int row  = index / w;
      int col = index % w;

      for (int j = 0; j < 8; j++) {
          // wrap around edges to simulate torus
          int newRow = (row + dy[j] + h) % h;
          int newCol = (col + dx[j] + w) % w;
          neighbors[j] = newRow * w + newCol;
      }
      return neighbors;
  }
}