import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

import java.util.ArrayList;
import java.util.List;

public class Data {
	private int pageNbr;
	private String url;
	private String filesRoot;
	private JSONArray DataArray;
	private List<BasicDBObject> ListDBSpell = new ArrayList<BasicDBObject>();
	
	public Data(int pageNbr, String url, String filesRoot) {
		this.pageNbr = pageNbr;
		this.url = url;
		this.filesRoot = filesRoot;
		this.DataArray = new JSONArray();
		File directory = new File(this.filesRoot+"BDDR_test");

	    if (!directory.exists()) {
	        directory.mkdir();
	    }
	    else {
	    	for (File f : directory.listFiles()) f.delete();
	    }
	}
	
	public Data() {
		this.pageNbr=1975; // 1975
		this.url="http://www.dxcontent.com/SDB_SpellBlock.asp?SDBID=";
		this.filesRoot="";
		this.DataArray = new JSONArray();
		File directory = new File(this.filesRoot+"BDDR_test");

	    if (!directory.exists()) {
	        directory.mkdir();
	    }
	    else {
	    	for (File f : directory.listFiles()) f.delete();
	    }
	}

	public void getSpells() {
		
		int idPage = 1 ;
				
		while(idPage <= this.pageNbr){  
					
			final String spellHTMlPath = this.filesRoot+"BDDR_test/SpellHTML"+idPage+".txt";
			final File spellHTML = new File(spellHTMlPath);         
			final String spellPath = this.filesRoot+"BDDR_test/Spell"+idPage+".txt"; 
			//final File spell = new File(spellPath);
			//BasicDBObject doc = new BasicDBObject();
			
			// Connect to web site to extract the spells ----------------------------------------------------
			try{
				URL url = new URL(this.url+idPage);
				URLConnection con = url.openConnection();
				InputStream input = con.getInputStream();
					
				// Create the HTML file for the current spell
				
				spellHTML.createNewFile();
				final FileWriter writer = new FileWriter(spellHTML);
				Reader reader = new InputStreamReader(input, "UTF-8");
				char[] buffer = new char[10000];
				StringBuilder builder = new StringBuilder();
				int len;
						
				try {
					// Testing whether the file is empty or not by testing the length of the data that has just been read
					while ((len = reader.read(buffer)) > 0) {
						builder.append(buffer, 0, len);
					}
					
					// Write in the file
					writer.write(builder.toString());	 
				}
				finally {
		            	
					// Close the file 
					writer.close();
					System.out.println("Page "+idPage+" downloaded with success !");
				}
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println(e);
			} // -----------------------------------------------------------------------------------------
			
			
			// Clean the data collected from the web site	----------------------------------------------
			try {
				
				spellHTMLCleaner(spellHTMlPath, spellPath);
				System.out.println("Page "+idPage+" cleaned with success !");
				
			} catch (IOException e) {
				
				System.out.println("The HTML file coudn't be cleaned : "+e);
				
			} // -----------------------------------------------------------------------------------------
			
			// Analyze the data form the cleaned file	--------------------------------------------------
			
			spellDataAnalyzer(spellPath, idPage);
			
			//	------------------------------------------------------------------------------------------
			
			// Incrementing page number
			idPage = idPage + 1;
		
		} // While loop end 
	}
	
	public void spellHTMLCleaner(String fileToRead, String fileToWrite) throws IOException {
		
		// Open the Spell HTML file to analyze the data
		FileReader fileReader = new FileReader(fileToRead);
		// Create another file for more specific information about the spell
		FileWriter fileWriter = new FileWriter(fileToWrite, true); 
		BufferedWriter output = new BufferedWriter(fileWriter); 
		
		// Read the spell HTML file
		int flot = fileReader.read();
		char readChar = (char)flot; 
		char newChar = (char)flot;
    	boolean isComment = false;
    	boolean start = false;
    	boolean end = true;
    	
		// Test if the file has come to an end (The read method returns -1 if there's nothing to read)
		while(flot != -1){ 
    		
    		// Search for the beginning of a tag
			if(readChar == '<'){ 
				
				// Test if the new char read is not an ending tag
				while(newChar != '>'){
	    			
					// If the new char is an '!' an the read char is '<' then maybe it's a beginning of a comment therefore skip it 
					if(newChar == '!' && readChar == '<' )
					{
						isComment = true;
						readChar = newChar;
						flot = fileReader.read();
						newChar = (char)flot;
					}
					
					// If it is a comment and there's an 'S' followed by a 'T' then it is the start of the spell description
					if(isComment && readChar == 'S' && newChar == 'T')
					{
						start = true;
						end = false;
						readChar = newChar;
						flot = fileReader.read(); 
						newChar = (char)flot; 
					}
					
					// If it is a comment and there's an 'E' followed by an 'N' then it is the end of the spell description
					if(isComment && start && readChar == 'E' && newChar =='N')
					{
						start = false;
						end = true;
						readChar = newChar;
						flot = fileReader.read(); 
						newChar = (char)flot;
					}
						
					flot = fileReader.read();
					readChar=newChar;
					newChar = (char)flot; 
				}
				

				// Read the next char and save it in the newChar
				flot = fileReader.read(); 
				newChar = (char)flot;
				
				// If it's a comment and the newChar is an ending tag the put isComment back to false
				if(isComment && newChar=='>'){
					isComment=false;
				}
				
				readChar = newChar;
				
			}	// If we are not in a tag (anymore)
			else{
				
				newChar=(char)flot;
				
				// We right in a buffer
				if(start && !end){
					// If it is an uppercase letter then put a new line before it
					if(newChar<=90 && newChar>=65){
						output.write('\n');
					}
					
					// If it is a ';' then change it to a new line
					if(newChar==';') newChar='\n';
					
					// Write the char
					output.write(newChar);
					
				}
				
				// Read the next char
				flot = fileReader.read();
				readChar = (char)flot;
			}
		}
		
		// Update the spell file and close the buffer and the fileToRead
		output.flush();  
		output.close();
		fileReader.close();
		File HTMLSpellFile = new File(fileToRead);
		HTMLSpellFile.delete();
	}
	
	
	public void spellDataAnalyzer(String spellPath, int idPage) {

		
		try (BufferedReader br = new BufferedReader(new FileReader(spellPath))) {

			String line;
			String out="";
			int lineNbr=1;
			JSONObject currentData = new JSONObject();
			boolean WizardSpell=false;
			boolean resistance=false;
			BasicDBObject doc = new BasicDBObject();

					
									    
			while ((line = br.readLine()) != null) {
				// process the line.
				out+=line;
				
				while(lineNbr==1 && (line = br.readLine())!= null){
					
					// If the line starts with "School" then save the precedent line as name
				    if(line.startsWith("School")){
				    	
						currentData.put("name", out);
					    doc.append("name", out);
				    	out="";
				    	lineNbr++;		
				    }
				    		
				    else {	
				    	out+=line;	
				    }

				}
				
				// If the line starts with "Level"
				if(line.startsWith("Level")){
					int i=0;
					
					// If the the line contains "sorcerer/wizard" then search for its level after the string
				    if(line.contains("sorcerer/wizard")){
				    	WizardSpell=true;
				    	i=line.indexOf("sorcerer/wizard", 0);
				    	currentData.put("level", line.substring(i+16,i+17));
				    	doc.append("level", line.substring(i+16,i+17));
				    		
				    }	// else put a random level for example 99 
				    else {
				    	WizardSpell=false;
				    	currentData.put("level", 99);
					    doc.append("level", 99);
				    }
				}
				
				// If the line starts with "Components"
				if(line.startsWith("Components")){
					
					String components="";
				    line = br.readLine();
				    
				    // While the next line is not "Effect"
				    while(!line.startsWith("Effect")){
				    	
				    	// Look for the components and save them in the components string
				    	if(line.length()<=2 || line.charAt(1)==' ' || line.charAt(1)==','){
				    		
				    		if(!components.contains(line.substring(0, 1)))
				    			components+=line.charAt(0);
				    		}
				    		line = br.readLine();
				    }
				    
				    char[] comp = new char[components.length()];
				    
				    // Add all the chars in the components string to the data
				    for(int i=0; i<components.length(); i++) {
				    	
				    	comp[i]=components.charAt(i);
				    }
				    
				    currentData.put("components", comp);		
				    doc.append("components", comp);
				}
				
				// If the line starts with "Resistance"
				if(line.startsWith("Resistance")){
					
				    int i=line.indexOf("Resistance", 0);
				    
				    // If there's a no then resistance false
				    if(line.substring(i+11).equals("no")){
				    	
				    	resistance=false;
				    }// Else resistance is true
				    else{
				    	
				    	resistance=true;
				    }
				    
				}
			}
			
			// Add the resistance to the data from here so even if it is not mentioned in the file it will be added
			currentData.put("spell_resistance", resistance);
			doc.append("spell_resistance", resistance);
				    
			
			if(WizardSpell && idPage!=1326) {
				
				String fileName= currentData.getString("name");
				if(fileName.contains("/")) 
					fileName = fileName.substring(0, fileName.indexOf('/'))+"_"+fileName.substring(fileName.indexOf('/')+1);
				File directory = new File(filesRoot+"BDDR_test");
				boolean exist = new File(directory, fileName+".json").exists();
				
				if(!exist) {
					
					// Create a JSON file of the spell
					final String spellJSONPath = filesRoot+"BDDR_test/"+fileName+".json";
					FileWriter fw = new FileWriter(spellJSONPath, false); 
					BufferedWriter output = new BufferedWriter(fw);
					output.write(""+currentData.toString()+"");
					output.flush();  
					output.close();
					
					// Save data
					currentData.put("Type", "sorcerer/wizard"); 
					DataArray.put(currentData);
					doc.append("SpellType", "Wizard");
					ListDBSpell.add(doc);
				}
				
				System.out.println("Spell "+(idPage)+" : "+currentData.toString());
			}
			else {
				//currentData.put("type", "Not a sorcerer/wizard");
				//System.out.println("Spell "+(idPage)+" : "+currentData.toString());
				//DataArray.put(currentData);
				System.out.println("Not a Sorcerer/Wizard spell");
				//doc.append("SpellType", "Not Wizard");
			}

			System.out.println("Done");
			System.out.println("----");
				    
		    // Delete the spell file
			br.close();
			File spellFile = new File(spellPath);
			spellFile.delete();
							
		}
		catch (Exception e) {
			
			System.out.println("Analyzing data problem : "+e);
		}
	}
	
	public JSONArray getSpellArray() {
		return this.DataArray;
	}
	
	public void sendDataToMongoDB() {
		
	}
	
	public void sendDataToDB() throws SQLException, JSONException {
		
		SQLiteDB sqDB = new SQLiteDB(filesRoot, "spell.db");
		sqDB.connect();
		sqDB.createTable();
		
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB( "test" );
		
		// Free the collection from any previous data 
		if(db.collectionExists("SpellCollection"))
			db.getCollection("SpellCollection").drop();
		
		DBCollection coll = db.getCollection("SpellCollection");
		mongoClient.setWriteConcern(WriteConcern.JOURNALED);
		
		for(int i = 0; i < DataArray.length(); i++) {
			System.out.println("Sending spell "+i+"/"+DataArray.length());
			JSONObject spell = DataArray.getJSONObject(i);
			char[] comp = (char[]) spell.get("components");
			sqDB.insert(spell.getString("name"), spell.getInt("level"), String.valueOf(comp), spell.getBoolean("spell_resistance"));
			coll.insert(ListDBSpell.get(i));
		}
		mongoClient.close();
		sqDB.close();	
	}
	
	
	public void getGoodSpells() {
		getGoodSpellsSQL();
		getGoodSpellsMongoDB();
	}
	
	
	public void getGoodSpellsSQL() {
		
		SQLiteDB sqDB = new SQLiteDB(filesRoot, "spell.db");
		sqDB.connect();
		sqDB.select_Good_Spells();
		sqDB.close();
	}
	
	
	public void getGoodSpellsMongoDB() {
		
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB( "test" );
		
		DBCollection coll = db.getCollection("SpellCollection");
		mongoClient.setWriteConcern(WriteConcern.JOURNALED);
		
		String map = "function() { "+ 
	             "if ( (this.level <= 4) && (this.components == \"V\" )) "+
	             "emit(this.name, this.level);}";
	   
		String reduce = "function(key, values) { " +

               "return {level : values};} ";
	
		MapReduceCommand cmd = new MapReduceCommand(coll, map, reduce, null, MapReduceCommand.OutputType.INLINE, null);

		MapReduceOutput out = coll.mapReduce(cmd);
		
		System.out.println("MongoDB - Spells that can free Pito : ");
		for (DBObject o : out.results()) {
			System.out.println(o.toString());
		}
		
		mongoClient.close();
	}
}