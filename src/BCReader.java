import java.io.*;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.charset.Charset;
import java.nio.file.Path;

// Extracts BC scores and ranks of all nodes of a network
public class BCReader {
  public static void main(String[] args) {
    // Define charset and file path
    Charset c = Charset.forName("US-ASCII");
    // assumes the file is located in the same folder as the java class
    Path f = FileSystems.getDefault().getPath("input/", args[0]);

    // open a file using the specified charset
    try (BufferedReader r = Files.newBufferedReader(f, c)) {
      String ln = null; // a line in the file
      boolean start = false; // a flag to indicate start of reading data

     // for each line, process the string to extract needed data
      while ((ln = r.readLine()) != null) {
        // split the line by comma and form an array of the component strings
        String[] str = ln.split(",");
        int j = 0;
        // print out each component in one line
        for (String i : str) {
          if (!start && i.contains("edu.uci.ics.jung.algorithms.importance.Ranking")) start = true;
          if (start && j <= 100) { 
            System.out.println((++j)+". "+i);

            // Your code starts here: capture needed data and store them in a hashmap





            // Your code ends here
          }
        }
      }
    } catch (IOException e) {
      System.err.format("IOException: %s%n", e);
    }
  }
}
