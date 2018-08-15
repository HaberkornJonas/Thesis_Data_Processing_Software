package ch.fhnw.thesis;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

    private static final String CONVERT_LOG_CSV = "Convert log into csv";
    private static final String CONVERT_CSV_KML = "Convert csv into KML";
    private static final String ADD_DATA_TO_DATABASE  = "Save data from csv file into a database";
    private static final String CLOSE_APPLICATION  = "Close the application";
    private static String ACTION;
    
    public static final String[] MODULE_NAME = {
            "Error or APM not connected",
            "No module",
            "Spotlight module",
            "Sprinkler module",
            "Delivery module",
            "Meteorological module"
        };
    public static final String[] MODULE_NAME_SQL_TABLE = {
            "No_module",
            "Spotlight_module",
            "Sprinkler_module",
            "Delivery_module",
            "Meteorological_module"
        };
    public static final String[][] PARAMETER_NAME = { //No space character in parameter/column name
            new String[]{ },
            new String[]{ },
            //new String[]{ "Spotlight status", "PWM value" }, //first attempt
            new String[]{ "Spotlight_status", "PWM_value", "Blinking_frequency_in_ms"},
            new String[]{ "Pump_status", "Water_level_in_percent", "Water_level_value"},
            new String[]{ "Package_presence", "Pin_status", "Echo_distance_in_cm", "PWM_value"},
            new String[]{ "Temperature_in_C", "Humidity_in_percent", "Luminosity_High_is_0"}
    };

    // These 3 parameter are of type string to avoid convertion lost
    public static final String[] POSITION_FIELDS_NAME = { "Latitude", "Longitude", "Altitude" };
    
    private static final String[] KML_COLOR = {
            "7fff00ff", //Purple
            "7f00ff00", //Green
            "7fff0000", //Red
            "7f3333ff", //Blue
            "7fffff00", //Yellow
            "7fffffff", //White
            "7f000000"  //Black
    };
    
    
    public static void main(String[] args) {
        do {
            ACTION = dropDown("Application by Jonas Haberkorn.\nThe used files must follow the specifications given for the Thesis.\n\nWhat do you want to do?", new String[] {CONVERT_LOG_CSV, CONVERT_CSV_KML, ADD_DATA_TO_DATABASE, CLOSE_APPLICATION});
            if(ACTION != null) 
                switch(ACTION) {
                    case CONVERT_LOG_CSV:
                        convertLogToCsv();
                        break;
                    case CONVERT_CSV_KML:
                        convertCsvToKml();
                        break;
                    case ADD_DATA_TO_DATABASE:
                        addDataToDataBase();
                        break;
                }
        }while(ACTION!= null && !ACTION.equals(CLOSE_APPLICATION));
    }
    
    
    
    private static void convertLogToCsv() {
        
        FileNameExtensionFilter[] filtersTlogRlog = { new FileNameExtensionFilter("tlog","tlog"), new FileNameExtensionFilter("rlog","rlog")};
        File logFile = chooseFile("C:\\Users\\user\\Documents\\Mission Planner\\logs\\HEXAROTOR\\1", "Choose the log file to convert into csv", JFileChooser.FILES_ONLY, filtersTlogRlog);
        if(logFile==null)
            return;
        File saveDirectory = chooseFile("C:\\Users\\user\\Documents\\Mission Planner\\logs\\CSV", "Choose the directory where you want to save the csv file", JFileChooser.DIRECTORIES_ONLY, null);
        if(saveDirectory==null)
            return;
        String fileName = inputDialog("What name will the new csv file have? (Without the csv extention)", "New csv file's name", "csv_");
        if(fileName == null ||fileName.equals(null))
            return;
        File csvFile = new File(saveDirectory.toString()+"\\"+fileName+".csv");

        try {
            PrintWriter writer = new PrintWriter(csvFile, "UTF-8");
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            
            long nb_line = reader.lines().count();
            System.out.println("[INFO] "+nb_line+" lines to convert\n");
            reader.close();
            reader = new BufferedReader(new FileReader(logFile));
            
            String line;
            long i = 0;
            int first_line = 1;
            
            while ((line = reader.readLine()) != null) {
                i++;
                if(line.startsWith("APM") && line.matches("[a-zA-Z0-9 < -:/;.]*") && line.endsWith(";")) {
                   String[] tmpLine = line.split("/");
                   try {
                       if((7 + PARAMETER_NAME[Integer.parseInt(tmpLine[5].split(":")[1])+1].length) == (tmpLine.length)) {
                           String[] tmpData = tmpLine[0].split(":");
                           if(first_line == 1) {
                               writer.print(tmpData[1]+",");
                               first_line = 0;
                           } else {
                               writer.print("\n"+tmpData[1]+",");
                           }
                           for(int x=1; x<tmpLine.length-1; x++) {
                               writer.print(tmpLine[x].split(":")[1]+",");
                           }
                           writer.print(tmpLine[tmpLine.length-1].split(":")[1]);
                       }else
                       {
                           System.out.println("[CONVERTING] Line "+i+" : "+(7 + PARAMETER_NAME[Integer.parseInt(tmpLine[5].split(":")[1])+1].length)+" = "+tmpLine.length+" ?");
                       }
                   }
                   catch(Exception e)
                   {
                       System.err.println("[CONVERTING] Line "+i+" could not be converted. Wrong format or corrupted data.");
                       System.err.println(e);
                   }
               }
            }
            System.out.println();
            
            reader.close();
            writer.close();
            JOptionPane.showMessageDialog(null, "Convertion log to csv completed.\nReturning to menu.");
        }catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Error while converting the log file to a csv file.");
            System.out.println("[ERROR] Error while converting the log file to a csv file");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 
    }
    
    
    private static void addDataToDataBase() {
        File dbDirectory = chooseFile("C:\\Users\\user\\Documents\\Mission Planner\\logs\\DATABASE", "Choose the directory where the database you want to use is.", JFileChooser.DIRECTORIES_ONLY, null);
        if(dbDirectory == null)
            return;
        String dbName = inputDialog("What is the name of the database?", "Database's name", "thesis_database");
        if(dbName == null)
            return;
        
        Connector conn;
        try {
            conn = new Connector(dbDirectory, dbName, "sa", ""); //Connection as System Admin
        } catch (Exception e) {
            System.out.println("[ERROR] Error while connecting to the database");
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        JOptionPane.showMessageDialog(null, "You are connected to the database.\nPlease choose a csv file to add to the data.");
        
        FileNameExtensionFilter[] filterCsv = { new FileNameExtensionFilter("csv","csv")};
        try {
            File csvFile = chooseFile("C:\\Users\\user\\Documents\\Mission Planner\\logs\\CSV", "Choose the csv file you want to convert into kml.", JFileChooser.FILES_ONLY, filterCsv);
            if(csvFile == null) {
                conn.close();
                return;
            }
        
            String tableName = inputDialog("What is the name of the table in wicht the data will be stored?", "Table's name", "Mission");
            if(tableName == null){
                conn.close();
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            long nb_line = reader.lines().count();
            System.out.println("[INFO] "+nb_line+" lines of data to add to the database\n");
            reader.close();
            reader = new BufferedReader(new FileReader(csvFile));
            String line;

            long i = 0;
            int last_index_number = -1;
            int actual_module = -1;
            int stage_number = 0;
            String actual_table_name = "";
            { //subsection having all writing elements, to avoid that the writer.close() close the file before the end of the last print
                while((line = reader.readLine()) != null) {
                    i++;
                    String[] tmpLine = line.split(",");
                    if(Integer.parseInt(tmpLine[0])<last_index_number || Integer.parseInt(tmpLine[5]) != actual_module) {
                        System.out.println("[DATABASE] New stage at line "+i);
                        stage_number++;
                        last_index_number = Integer.parseInt(tmpLine[0]);
                        actual_module = Integer.parseInt(tmpLine[5]);
                        actual_table_name = tableName+"_"+String.format("%03d",stage_number)+"_"+MODULE_NAME_SQL_TABLE[actual_module];
                        conn.createTable(actual_table_name, actual_module);
                    } 
                    if(PARAMETER_NAME[actual_module+1].length == 0)
                        conn.insertRow(actual_table_name, Integer.parseInt(tmpLine[0]), Float.parseFloat(tmpLine[1]), Float.parseFloat(tmpLine[2]), Float.parseFloat(tmpLine[3]), Integer.parseInt(tmpLine[4]), actual_module, Integer.parseInt(tmpLine[6]), null);
                    else
                        conn.insertRow(actual_table_name, Integer.parseInt(tmpLine[0]), Float.parseFloat(tmpLine[1]), Float.parseFloat(tmpLine[2]), Float.parseFloat(tmpLine[3]), Integer.parseInt(tmpLine[4]), actual_module, Integer.parseInt(tmpLine[6]), Arrays.copyOfRange(tmpLine, 7, tmpLine.length));
                }
            }
            System.out.println();
            conn.close();
            reader.close();
            JOptionPane.showMessageDialog(null, "Data successfully added to the database.\nReturning to menu.");
            
        } catch (Exception e) {
            System.out.println("[ERROR] Error while adding data to the database");
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        
    }
    
    
    private static void convertCsvToKml() {
        FileNameExtensionFilter[] filterCsv = { new FileNameExtensionFilter("csv","csv")};
        File csvFile = chooseFile("C:\\Users\\user\\Documents\\Mission Planner\\logs\\CSV", "Choose the csv file you want to convert into kml.", JFileChooser.FILES_ONLY, filterCsv);
        if(csvFile == null)
            return;
        File kmlDirectory = chooseFile("C:\\Users\\user\\Documents\\Mission Planner\\logs\\KML", "Choose the directory where you want to save your kml file.", JFileChooser.DIRECTORIES_ONLY, null);
        if(kmlDirectory == null)
            return;
        String kmlName = inputDialog("What is the name of the generated kml file? (Without the kml extension)", "kml file's name", "");
        if(kmlName == null)
            return;
        File kmlFile = new File(kmlDirectory.toString()+"\\"+kmlName+".kml");
        
        try {
            PrintWriter writer = new PrintWriter(kmlFile, "UTF-8");
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            
            long nb_line = reader.lines().count();
            System.out.println("[INFO] "+nb_line+" lines to convert\n");
            reader.close();
            reader = new BufferedReader(new FileReader(csvFile));
            
            String line;
            long i = 1;
            int last_index_number;
            int actual_module;
            int stage_number = 1;
            { //subsection having all writing elements, to avoid that the writer.close() close the file before the end of the last print
                while((line = reader.readLine()) != null) {
                    String[] tmpStart = line.split(",");
                    if(Float.parseFloat(tmpStart[1]) != 0 && Float.parseFloat(tmpStart[2]) != 0){
                        last_index_number = Integer.parseInt(tmpStart[0]);
                        actual_module = Integer.parseInt(tmpStart[5]);
                        kmlFirstStage(writer, kmlName);
                        writer.print(
                                        "    <Placemark>\n"+
                                        "      <name>Stage_"+String.format("%03d",stage_number)+"_"+MODULE_NAME_SQL_TABLE[actual_module]+"</name>\n"+
                                        "      <visibility>0</visibility>\n"+
                                        "      <description>Track generated with the converter.</description>\n"+
                                        "      <LookAt>\n"+
                                        "        <longitude>"+tmpStart[1]+"</longitude>\n"+
                                        "        <latitude>"+tmpStart[2]+"</latitude>\n"+
                                        "        <altitude>"+(tmpStart[3]+50)+"</altitude>\n"+
                                        "        <heading>0</heading>\n"+
                                        "        <tilt>0</tilt>\n"+
                                        "        <range>550</range>\n"+
                                        "      </LookAt>\n"+
                                        "      <styleUrl>#1</styleUrl>\n"+
                                        "      <LineString>\n"+
                                        "        <extrude>1</extrude>\n"+
                                        "        <tessellate>1</tessellate>\n"+
                                        "        <altitudeMode>relativeToGround</altitudeMode>\n"+
                                        "        <coordinates>\n");
                        writer.print(   "          "+tmpStart[1]+","+tmpStart[2]+","+tmpStart[3]+"\n");
                        System.out.println("[CONVERTING] Line "+i+" out of "+nb_line);
    
                        while ((line = reader.readLine()) != null) {
                            i++;
                            String[] tmpLine = line.split(",");
                            if(Integer.parseInt(tmpLine[0])<last_index_number || Integer.parseInt(tmpLine[5]) != actual_module)  {
                                stage_number++;
                                last_index_number = Integer.parseInt(tmpLine[0]);
                                actual_module = Integer.parseInt(tmpLine[5]);
                                kmlNewStage(writer, tmpLine, stage_number, actual_module);
                                System.out.println("[CONVERTING] New stage");
                            }
                            writer.print("          "+tmpLine[1]+","+tmpLine[2]+","+tmpLine[3]+"\n");
                            System.out.println("[CONVERTING] Line "+i+" out of "+nb_line);
                        }
                    }
                    i++;
                }
                writer.print(
                        "        </coordinates>\n"+
                        "      </LineString>\n"+
                        "    </Placemark>\n"+
                        "  </Document>\n"+
                        "</kml>");
            }
            System.out.println();
            writer.close();
            reader.close();
            JOptionPane.showMessageDialog(null, "Convertion csv to kml completed.\nReturning to menu.");
        }catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Error while converting the csv file to a kml file.");
            System.out.println("[ERROR] Error while converting the csv file to a kml file");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        
    }

    
    private static void kmlFirstStage(PrintWriter writer, String kmlName) {
        writer.print(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"+
                "  <Document>\n"+
                "    <name>"+kmlName+"</name>\n"+
                "    <open>1</open>\n"+
                "    <description>Generated kml file from a csv file.</description>\n");
        for(int i=0; i<KML_COLOR.length; i++ ) {
            writer.print(
                    "    <Style id=\""+i+"\">\n"+
                    "      <LineStyle>\n"+
                    "        <color>"+KML_COLOR[i]+"</color>\n"+
                    "        <width>4</width>\n"+
                    "      </LineStyle>\n"+
                    "      <PolyStyle>\n"+
                    "        <color>"+KML_COLOR[i]+"</color>\n"+
                    "      </PolyStyle>\n"+
                    "    </Style>\n");
        }
    }
    
    
    private static void kmlNewStage(PrintWriter writer, String[] buffer1_line, int stage_number, int actual_module) {
        writer.print(
                "        </coordinates>\n"+
                "      </LineString>\n"+
                "    </Placemark>\n"+
                "    <Placemark>\n"+
                "      <name>Stage_"+String.format("%03d",stage_number)+"_"+MODULE_NAME_SQL_TABLE[actual_module]+"</name>\n"+
                "      <visibility>0</visibility>\n"+
                "      <description>Track generated with the converter.</description>\n"+
                "      <LookAt>\n"+
                "        <longitude>"+buffer1_line[1]+"</longitude>\n"+
                "        <latitude>"+buffer1_line[2]+"</latitude>\n"+
                "        <altitude>"+(buffer1_line[3]+50)+"</altitude>\n"+
                "        <heading>0</heading>\n"+
                "        <tilt>0</tilt>\n"+
                "        <range>550</range>\n"+
                "      </LookAt>\n"+
                "      <styleUrl>#"+(stage_number%KML_COLOR.length)+"</styleUrl>\n"+
                "      <LineString>\n"+
                "        <extrude>1</extrude>\n"+
                "        <tessellate>1</tessellate>\n"+
                "        <altitudeMode>relativeToGround</altitudeMode>\n"+
        "        <coordinates>\n");
    }
    
    
    private static String dropDown(String message, String[] options) {
        System.out.println("[ACQUIRING] "+message);
        String choice = (String) JOptionPane.showInputDialog(null, message, "Thesis - Mehrzweck Drohne", JOptionPane.QUESTION_MESSAGE, null, options,  options[0]);
        System.out.println("[ANSWER] "+choice);
        return choice;
    }
    
    
    private static File chooseFile(String currentDirectory, String dialogTitle, int selectionMode, FileNameExtensionFilter[] filters) {
        System.out.println("[ACQUIRING] "+dialogTitle);
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(currentDirectory));
        chooser.setDialogTitle(dialogTitle);
        chooser.setFileSelectionMode(selectionMode);
        chooser.setAcceptAllFileFilterUsed(false);
        if(filters != null && filters.length>0)
            for(int i = 0; i<filters.length; i++) {
                chooser.setFileFilter(filters[i]);
            }
        
        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("[ANSWER] "+chooser.getSelectedFile().toString()+"\n");
            return chooser.getSelectedFile();
        } else {
            System.out.println("[ANSWER] Wrong or no selection\n");
            ACTION = null;
            return null;
        }
    }
    
    private static String inputDialog(Object message, String title, String standartInput) {
        System.out.println("[ACQUIRING] "+message);
        int dialogButton = JOptionPane.DEFAULT_OPTION;
        Object tmp = JOptionPane.showInputDialog(null, message, title, dialogButton, null, null, standartInput);
        String tmp2;
        if(tmp != null) {
            tmp2 = tmp.toString();
            if(tmp2==null || tmp2.equals("")) {
                System.out.println("[ANSWER] No answer or quitting application");
                ACTION = null;
                return null;
            }
        }
        return tmp.toString();
    }
    
}
