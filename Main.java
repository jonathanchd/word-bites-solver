import java.util.*;
import java.io.*;

class Main{
  //Shorter words aren't worth
  public static int minLength = 5;
  //Limit word length
  //9 for vertical words, 8 for horizontal words
  public static int numRow = 9, numCol = 8;
  //Acceptable words
  public static TreeSet<String> words = new TreeSet<>();
  //Suffixes to sort by later
  public static HashSet<String> suffixes = new HashSet<>();
  //All the actual word bites parts
  public static ArrayList<part> parts = new ArrayList<>();
  public static int len; //parts.size();
  public static HashSet<String> ans = new HashSet<>();
  public static HashMap<String, group> groups = new HashMap<>();
  public static PrintWriter out;
  public static HashMap<Integer, Integer> points = new HashMap<>();

  public static void main(String[] args) throws IOException{
    loadWords();
    loadPoints();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    out = new PrintWriter(new BufferedWriter(new FileWriter("answers.txt")));
    System.out.println("Do you want to sort by word length? (yes/no)");
    boolean sortByWordLength = br.readLine().toLowerCase().equals("yes");
    System.out.println("Format (white spaces don't matter): ");
    System.out.println("Singular: just type in the letter");
    System.out.println("Pair: enter in letters (left to right or top to bottom) and orientation");
    System.out.println("Example: es v (vertical) or es h (horizontal)");
    System.out.println("Type \"end\" to finish entering in parts");
    String s = br.readLine();
    while (!s.equals("end")){
      //strip whitespaces
      s = s .replaceAll("\\s+","");
      //singular word
      if (s.length() == 1){
        parts.add(new part(s, 's'));
      }
      else{
        parts.add(new part(s.substring(0, 2), s.charAt(2)));
      }
      s = br.readLine();
    }
    len = parts.size();
    //Create vertical words
    for (int i = 0; i < len; ++i){
      if (parts.get(i).o == 'h'){
        genVerWords("", parts.get(i).w.substring(0, 1),i);
        genVerWords("", parts.get(i).w.substring(1, 2),i);
      }
      else{
        genVerWords("", parts.get(i).w, i);
      }
    }
    //Create horizontal words
    for (int i = 0; i < len; ++i){
      if (parts.get(i).o == 'v'){
        genHorWords("", parts.get(i).w.substring(0, 1),i);
        genHorWords("", parts.get(i).w.substring(1, 2),i);
      }
      else{
        genHorWords("", parts.get(i).w, i);
      }
    }
    if (sortByWordLength){
      print(ans);
    }
    else{
      groupWords();
      printGroups(sortGroups());
    }  
    System.out.println("Answers in answers.txt");
    br.close();
    out.close();
  }

  //param(current string, part to add on)
  public static void genHorWords(String s, String add, int index){
    //if word is too long
    if (s.length() + add.length() > numCol || parts.get(index).used){
      return;
    }
    parts.get(index).used = true;
    s += add;
    if (words.contains(s) && s.length() >= minLength){
      ans.add(s + " hori");
    }
    if (!canContinue(s)){
      parts.get(index).used = false;
      //remove added string
      s = s.substring(0, s.length() - add.length());
      return;
    }
    for (int i = 0; i < len; ++i){
      if (parts.get(i).used){
        continue;
      }
      else{
        if (parts.get(i).o == 'v'){
          genHorWords(s, parts.get(i).w.substring(0, 1),i);
          genHorWords(s, parts.get(i).w.substring(1, 2),i);
        }
        else{
          genHorWords(s, parts.get(i).w, i);
        }
      }
    }
    parts.get(index).used = false;
    //remove added string
    s = s.substring(0, s.length() - add.length());
    return;
  }

  //param(current string, part to add on)
  public static void genVerWords(String s, String add, int index){
    //if word is too long
    if (s.length() + add.length() > numRow || parts.get(index).used){
      return;
    }
    parts.get(index).used = true;
    s += add;
    if (words.contains(s) && s.length() >= minLength){
      ans.add(s + " vert");
    }
    if (!canContinue(s)){
      parts.get(index).used = false;
      //remove added string
      s = s.substring(0, s.length() - add.length());
      return;
    }
    for (int i = 0; i < len; ++i){
      if (parts.get(i).used){
        continue;
      }
      if (parts.get(i).o == 'h'){
        genVerWords(s, parts.get(i).w.substring(0, 1),i);
        genVerWords(s, parts.get(i).w.substring(1, 2),i);
      }
      else{
        genVerWords(s, parts.get(i).w, i);
      }
    }
    parts.get(index).used = false;
    //remove added string
    s = s.substring(0, s.length() - add.length());
    return;
  }

  //Individual words under like 6 isn't worth
  //Should save time on printing the words too
  public static int minSoloLength = 6;
  public static void groupWords(){
    for (String s : ans){
      //Check if suffix exists (length 3 and 4)
      //Strip the identifier
      String word = s.substring(0, s.length() - 5);
      String id = s.substring(s.length() - 4, s.length());
      String len4sub = word.substring(word.length() - 4, word.length());
      String len3sub = word.substring(word.length() - 3, word.length());
      if (containsSuffix(len4sub)){
        if (groups.containsKey(len4sub)){
          groups.get(len4sub).add(word);
        }
        else{
          char o = id.equals("hori") ? 'h' : 'v';
          groups.put(len4sub, new group(o));
          groups.get(len4sub).add(word);
        }
      }
      else if (containsSuffix(len3sub)){
        if (groups.containsKey(len3sub)){
          groups.get(len3sub).add(word);
        }
        else{
          char o = id.equals("hori") ? 'h' : 'v';
          groups.put(len3sub, new group(o));
          groups.get(len3sub).add(word);
        }
      }
      else{
        if (word.length() < minSoloLength){
          continue;
        }
        char o = id.equals("hori") ? 'h' : 'v';
        groups.put(word, new group(o));
        groups.get(word).add(word);
      }
    }
  }

  public static ArrayList<group> sortGroups(){
    ArrayList<group> copy =  new ArrayList<>();
    for (Map.Entry<String, group> mapElement : groups.entrySet()){
      copy.add(mapElement.getValue());
    }
    Collections.sort(copy, new Comparator<group>(){
      @Override
      public int compare(group g1, group g2){
        //If same point value, shorter group comes first
        if (g1.totalPts == g2.totalPts){
          return g1.size - g2.size;
        }
        return g2.totalPts - g1.totalPts;
      }
    });

    return copy;
  }

  public static void printGroups(ArrayList<group> al) throws IOException{
    out = new PrintWriter(new BufferedWriter(new FileWriter("answers.txt")));
    for (group g : al){
      out.println(g.id == 'h' ? "HORI" : "VERT");
      for (node no = g.n; no.next != null; no = no.next){
        out.println(no.s);
      }
      out.println();
    }
    out.close();
  }

  public static boolean containsSuffix(String s){
    return suffixes.contains(s);
  }
  
  //Sort by word length
  public static void print(HashSet<String> set){
    ArrayList<String> copy = new ArrayList<>();
    for (String s : set){
      copy.add(s);
    }
    Collections.sort(copy, new Comparator<String>(){
      @Override
        public int compare(String o1, String o2) {
            if (o1.length() == o2.length()){
              return o1.compareTo(o2);
            }
            return o2.length() - o1.length();
        }
    });
    for (String s : copy){
      out.println(s);
    }
  }

  public static boolean canContinue(String s){
    if (s.compareTo(words.last()) >= 0){
      return false;
    }
    return words.higher(s).contains(s);
  }
  
  public static void loadWords() throws IOException{
    System.out.println("Loading words...");
    BufferedReader br = new BufferedReader(new FileReader("Collins Scrabble Words (2019).txt"));
    br.readLine(); //garbage first line
    String s = br.readLine();
    while (s != null){
      words.add(s.toLowerCase());
      s = br.readLine();
    }
    br.close();
    loadSuffixes();
  }

  public static void loadSuffixes() throws IOException{
    System.out.println("Loading suffixes...");
    BufferedReader br = new BufferedReader(new FileReader("suffixes.txt"));
    //Garbage first line
    br.readLine();
    String s = br.readLine();
    while (s != null){
      suffixes.add(s.toLowerCase());
      s = br.readLine();
    }
    System.out.println("Loading finished :)");
    br.close();
  }

  //Word length, point value
  public static void loadPoints(){
    points.put(3, 100);
    points.put(4, 400);
    points.put(5, 800);
    points.put(6, 1400);
    points.put(7, 1800);
    points.put(8, 2200);
    points.put(9, 2600);
  }

  //one block in the game
  //has a string, orientation (vertical, horizontal, or singular)
  //boolean is just for keeping track during the recursive calls
  public static class part{
    public String w;
    public boolean used;
    //orientation(v : vertical, h : horizontal: s : singlular)
    public char o; 
    public part(String w, char o){
      this.w = w;
      this.o = o;
      this.used = false;
    }
  }

  //common suffixes or singular words
  public static class group{
    public char id;
    public int totalPts, size;
    public node n;
    public group(char id){
      totalPts = 0;
      size = 0;
      n = new node("TERMINATE", null);
      this.id = id;
    }
    public void add(String str){
      ++size;
      totalPts += points.get(str.length());
      node temp = new node(str, n);
      n = temp;
    }
    public void print(){
      System.out.println(id == 'h' ? "hori" : "vert");
      for (node nod = n; !nod.s.equals("TERMINATE"); nod = nod.next){
        System.out.println(nod.s);
      }
    }
  }

  //use a linkedlist instead of an arraylist for each group
  //saves space because there'll be a lot of singular words
  public static class node{
    public String s;
    public node next;
    public node(String s, node next){
      this.s = s;
      this.next = next;
    }
  }
}