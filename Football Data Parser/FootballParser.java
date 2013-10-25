/*******************************************************************************
  * Paulius Paulauskas
  * 
  * Football season parser. Takes in a team name without the state from the NFL
  * e.g. Bears, and a year for the season in question e.g. 2011 for the 2011
  * season.
  * 
  * It will also take a value 1 or 0 to include (1) or exclude (0) penalties
  * in the output.
  * 
  * Compilation: javac FootballParser
  * Execution: java FootballParser Bears 2011 1 output.txt
  * Dependencies: stdlib.jar
  * 
  * The output will consist of a series of CSV values for the whole season.
  * 
  * It will also make an attempt to parse the play types. If it cannot be 
  * parsed will output with the original data.
  * 
  *****************************************************************************/

import java.net.URL;
import java.util.Hashtable;
import java.util.Enumeration;

public class FootballParser {
  static String team;
  static String year;
  static int penalty;
  static String output;
  static Out out;
  static String teamorig;
  static int possession = -1;
  static int initpos = -1;
  static int week = -1;
  
  // A method that parses a play
  public static String playparse(String play) {
      // Splits the string
      String[] arr = play.split(">");
      
      // Finds the length of the array
      int length = arr.length;
      
      // Creates a new string
      String play2 = "";
      
      // Loops through and creates the play
      for (int i = 2; i < length; i++) {
          String[] arr2 = arr[i].split("<"); 
          play2 = play2.concat(arr2[0]);
      }
      
      // Splits the string at commas to facilitate CSV output
      arr = play2.split(",");
      
      // Recreates the string
      play2 = "";
      
      // Reassembles the string
      length = arr.length;
      for (int i = 0; i < length; i++) {
          play2 = play2.concat(arr[i]);
      }
      
      // returns the string
      return play2;
  }
  
  // A method for printing out the information
  public static void output(String[] row) {
      // Finds the length of the first element
      int a = row[0].length();
      
      // Prints out the first element
      out.print(row[0].substring(a-1,a));
      out.print(",");
      
      // Finds time length
      a = row[1].length();
      if (a > 19)
          out.print(row[1].substring(19,a));
      out.print(",");
      
      // Finds the quarter length
      a = row[2].length();
      if (a > 19)
          out.print(row[2].substring(19,a));
      out.print(",");
      
      // Finds the ToGo length
      a = row[3].length();
      if (a > 19)
          out.print(row[3].substring(19,a));
      out.print(",");
      
      // Finds the possession
      a = row[4].length();
      if (a > 25)
          out.print(row[4].substring(25,a));
      out.print(",");
      
      // Parses the play
      String play = playparse(row[5]);
      out.print(play);
      out.print(",");
      
      // Finds the current score for first team
      a = row[6].length();
      if (a > 17)
          out.print(row[6].substring(17,a));
      out.print(",");
      
      // Finds the current score for the second team
      a = row[7].length();
      if (a > 17)
          out.print(row[7].substring(17,a));
      out.print(",");
      
      // Finds the EPB
      a = row[8].length();
      if (a > 17)
          out.print(row[8].substring(17,a));
      out.print(",");
      
      // Finds the EPA
      a = row[9].length();
      if (a > 17)
          out.print(row[9].substring(17,a));
      out.print(",");
      
      // Outputs the possession
      out.println(possession);
  }
  
  // Takes in the array of team game links. Can be called from another method
  // links should be an array of strings in the form:
  // /boxscores/201201010min.htm
  // /boxscores/201112250gnb.htm
  // Such as the first part of the link should not be included
  public static void decode(String[] links) {
      // Finds the array length
      int length = links.length;
      
      // Creates a new array for full links
      String[] fixed = new String[length];
      
      // Creates the game links
      for (int i = 0; i < length; i++) {
          String temp = "http://widgets.sports-reference.com/wg.fcgi?css=1&site=pfr&url=";
          temp = temp.concat(links[i]);
          temp = temp.concat("&div=div_pbp_data");
          fixed[i] = temp;
      }
      
      // Starts parsing the data
      for (int i = 0; i < length; i++) {
          String game = fixed[i];
          
          // Creates a new input stream from the website and reads it
          In in = new In(game);
          String f = in.readAll();
      
          // Splits the top off
          String[] break1 = f.split("</style>\\\\");
    
          // Splits the bottom off
          break1 = break1[1].split("</table>\\\\");
          
          // Splits it into single rows
          break1 = break1[0].split("</tr>");
      
          // Begins a loop where it takes each line and splits it into
          // individual segments, then runs some checks to make sure
          // what it read is a play. Also checks for penalties.
          int length2 = break1.length;
          
          for (int j = 0; j < length2; j++) {
              // Splits the row into individual elements
              String[] break2 = break1[j].split("</td>");
              
              // Finds the length of the elements
              int length3 = break2.length;
              
              // Checks for initial assignment of a possession
              if (possession == -1 && length3 == 10) {
                  // Gets the yardage at the beginning
                  int plength = break2[4].length();
                  
                  // Checks that there was a possession assigned
                  if (plength > 25) {
                      String pplay = break2[4].substring(25,28);
                      
                      // Assigns possession
                      if (pplay.equals(teamorig)) {
                          possession = 0;
                          initpos = 0;
                      }
                      
                      else {
                          possession = 1;
                          initpos = 1;
                      }
                  }
              }
              
              // If it is less than 10, it is not a play
              if (length3 == 10) {
                  
                  // Checks for the penalty
                  int pen = break2[0].lastIndexOf("has_penalty");
                  if (pen != -1) {
                      
                      // Checks for include or exclude pentalty
                      if (penalty == 1) {
                          
                          // Checks for change of possession
                          int chpos = break2[0].lastIndexOf("pos_change");
                          if (chpos != -1) {
                              // Reassigns possession
                              if (possession == 1) 
                                  possession = 0;
                              else if (possession == 0)
                                  possession = 1;
                          }
                          
                          // Prints out the info
                          output(break2);
                      }
                      
                      if (penalty == 0) {
                          // Checks for change of possession
                          int chpos = break2[0].lastIndexOf("pos_change");
                          if (chpos != -1) {
                              // Reassigns possession
                              if (possession == 1) 
                                  possession = 0;
                              else if (possession == 0)
                                  possession = 1;
                          }
                      }
                      
                  }
                  
                  else {
                      // Checks for change of possession
                      int chpos = break2[0].lastIndexOf("pos_change");
                      if (chpos != -1) {
                          // Reassigns possession
                          if (possession == 1) 
                              possession = 0;
                          else if (possession == 0)
                              possession = 1;
                      }
                      
                      output(break2);
                  }
              }
          }    
          out.println("");
          possession = -1;
      }
  }
  
  // A method for reading in all of the teams in a particular season
  public static void allteams() {
      // Creates new string array for all the teams
      String[] all = new String[32];
      
      // Fills the string array
      all[0] = ("crd");
      all[1] = ("atl");
      all[2] = ("rav");
      all[3] = ("buf");
      all[4] = ("car");
      all[5] = ("chi");
      all[6] = ("cin");
      all[7] = ("cle");
      all[8] = ("dal");
      all[9] = ("den");
      all[10] = ("det");
      all[11] = ("gnb");
      all[12] = ("htx");
      all[13] = ("clt");
      all[14] = ("jax");
      all[15] = ("kan");
      all[16] = ("mia");
      all[17] = ("min");
      all[18] = ("nwe");
      all[19] = ("nor");
      all[20] = ("nyg");
      all[21] = ("nyj");
      all[22] = ("rai");
      all[23] = ("phi");
      all[24] = ("pit");
      all[25] = ("sdg");
      all[26] = ("sfo");
      all[27] = ("sea");
      all[28] = ("ram");
      all[29] = ("tam");
      all[30] = ("oti");
      all[31] = ("was");
      
      // Creates a new hashtable to store games
      Hashtable<String,String> teams = new Hashtable<String,String>();
      
      // Starts to loop through all teams to get links
      for (int i = 0; i < 32; i++) {
          // Gets the team in question
          String curteam = all[i];
          
         // Appends stuff to create a link
          String webbase = "http://widgets.sports-reference.com/wg.fcgi?css=1&site=pfr&url=/teams/";
          webbase = webbase.concat(curteam);
          webbase = webbase.concat("/");
          webbase = webbase.concat(year);
          webbase = webbase.concat(".htm&div=div_team_gamelogs&del_col=1,2,3,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24"); 
          
          // Creates a new input stream from assembled website
          In in = new In(webbase);
          String f = in.readAll();
          
          // Splits the top off
          String[] break1 = f.split("</style>\\\\");
          
          // Splits the bottom off
          break1 = break1[1].split("</table>\\\\");
          
          // Splits it into single rows
          break1 = break1[0].split("</tr>");
          
          // Finds length of the rows
          int length = break1.length;
          int lengthtwo = length;
          
          // Size checking
          lengthtwo = length-4;
          
          // Does some size checking
          if (length < 20)
              lengthtwo = length-3;
          
          // Some ints for management
          int g = 0;
          int j = 0;
    
          // Goes through the single row array and pulls just the links of the games
          while (g < lengthtwo) {
              String check = break1[j];
              
              if (check.length() > 90) {
                  teams.put(check.substring(78,105), check.substring(78,105));
                  g++;
              }
              
              j++;
          }
      }
      
      // Creates a string array from the hashtable links
      int size = teams.size();
      
      // Creates a new string array for all the games
      String[] links = new String[size];
      
      // Creates a manual loop variable
      int cur = 0;
      
      // Gets an enumeration of all the keys and puts then into a string array
      for (Enumeration e = teams.elements(); e.hasMoreElements(); ) {
          links[cur] = (String) e.nextElement();
          cur++;
      }
      
      // Sends off the links to be decoded
      decode(links);
      
          
  }
  
  // Main method takes in a team name, year, and a penalty argument
  public static void main(String[] args) {
    team = args[0];
    year = args[1];
    penalty = Integer.parseInt(args[2]);
    output = args[3];
    if (args.length > 4)
      week = Integer.parseInt(args[4]);
    
    // Creates the output file
    out = new Out(output);
    
    if (team.equals("All") || team.equals("all")) {
        allteams();
    }
    
    else{
    
    // Creates a new hashtable to store teams
    Hashtable<String,String> teams = new Hashtable<String,String>(32);
    
    // Fills up the Hashtable
    teams.put("Cardinals", "crd");
    teams.put("Falcons", "atl");
    teams.put("Ravens", "rav");
    teams.put("Bills", "buf");
    teams.put("Panthers","car");
    teams.put("Bears","chi");
    teams.put("Bengals", "cin");
    teams.put("Browns", "cle");
    teams.put("Cowboys","dal");
    teams.put("Broncos", "den");
    teams.put("Lions", "det");
    teams.put("Packers", "gnb");
    teams.put("Texans", "htx");
    teams.put("Colts", "clt");
    teams.put("Jaguars", "jax");
    teams.put("Chiefs", "kan");
    teams.put("Dolphins", "mia");
    teams.put("Vikings", "min");
    teams.put("Patriots", "nwe");
    teams.put("Saints", "nor");
    teams.put("Giants", "nyg");
    teams.put("Jets", "nyj");
    teams.put("Raiders", "rai");
    teams.put("Eagles", "phi");
    teams.put("Steelers", "pit");
    teams.put("Chargers", "sdg");
    teams.put("49ers", "sfo");
    teams.put("Seahawks", "sea");
    teams.put("Rams", "ram");
    teams.put("Buccaneers", "tam");
    teams.put("Titans", "oti");
    teams.put("Redskins", "was");
    
    // Gets the code for the input team
    team = (String) teams.get(team);
    
    // Creates a new string for the team in question
    teamorig = team.toUpperCase();
    
    // Some error checking
    if (team == null) {
        StdOut.println("The team entered could not be processed.");
        StdOut.println("Please enter a capitalized team name e.g. Bears");
    }
    
    // Appends stuff to create a link
    String webbase = "http://widgets.sports-reference.com/wg.fcgi?css=1&site=pfr&url=/teams/";
    webbase = webbase.concat(team);
    webbase = webbase.concat("/");
    webbase = webbase.concat(year);
    webbase = webbase.concat(".htm&div=div_team_gamelogs&del_col=1,2,3,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24");
        
    // Creates a new input stream from assembled website
    In in = new In(webbase);
    String f = in.readAll();
    
    // Splits the top off
    String[] break1 = f.split("</style>\\\\");
    
    // Splits the bottom off
    break1 = break1[1].split("</table>\\\\");
    
    // Splits it into single rows
    break1 = break1[0].split("</tr>");
    
    // Finds length of the rows
    int length = break1.length;
    int lengthtwo = length;
    
    // Creates a new array to store the links
    // -4 due to first two headers and a bye week + playoff
    lengthtwo = length-4;
    String[] links = new String[lengthtwo];
    
    if (length < 20) {
        lengthtwo = length-3;
        links = new String[lengthtwo];
    }
    
    // Some ints for management
    int i = 0;
    int j = 0;
    
    // Goes through the single row array and pulls just the links of the games
    while (i < lengthtwo) {
        String check = break1[j];
        
        if (check.length() > 90) {
            links[i] = check.substring(78,105);
            i++;
        }
        
        j++;
    }
    
    // At this points links[] contains the links to all the games
    // played by the given team in the given year.
    decode(links);
    }
  }
}