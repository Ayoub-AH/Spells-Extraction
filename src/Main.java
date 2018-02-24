import java.net.URL;
import java.net.URLConnection;
import org.json.JSONArray;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.diagnostics.logging.Logger;

 
public class Main{
	
	public static void main(String[] argv){
		
		//Mongo mongo;
		
		
		/*MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB( "test" );
		
		DBCollection coll = db.getCollection("SpellCollection");
		mongoClient.setWriteConcern(WriteConcern.JOURNALED);*/
		
		// il faut créer le dossier C:\temp\BDDR_test pour pouvoir y écrire l'ensemble des fichiers dedans 
		// (s'assurer que le dossier est vide avant de lancer le programme)
		// Création des variables globales utilisées dans tout le programme
		// Sur le site "dxcontent.com" les sort commencent à la page d'id = 1
		
		Data data = new Data();
		data.getSpells();
		JSONArray dataArray= data.getSpellArray();
		System.out.println("\nThis is the number of all spells : "+dataArray.length());
		
		try {
			data.sendDataToSQLDB();
			
		} catch (Exception e) {
			
			System.out.println("\nError while sending data to SQlite DB : "+e);	
		}
		
		try {
			data.getGoodSpellsSQL();
			
		} catch (Exception e) {
			
			System.out.println("\nError while getting data from SQlite DB : "+e);	
		}
		
		
		
		
		/*String map = "function() { "+ 
	             "var category; " +
	             "if ( (this.SpellLevel <= 4) && (this.components == \"V\" ) ) "+
				 "category = 'Pito is free'; " +
	             "else " +
	             "category = 'Pito -> dead'; "+  
	             "emit(category, {name: this.SpellName});}";
	   
		String reduce = "function(key, values) { " +
                "var sum = 0; " +
                "values.forEach(function(doc) { " +
                "sum += 1; "+
                "}); " +
                "return {Spell: sum};} ";*
		
		
		
		
	   
	   MapReduceCommand cmd = new MapReduceCommand(coll, map, reduce,
			     null, MapReduceCommand.OutputType.INLINE, null);

			   MapReduceOutput out = coll.mapReduce(cmd);

			   for (DBObject o : out.results()) {
			    System.out.println(o.toString());
			   }
		
			
	
			   
			   ////////////////////////////////////////////////////////////////////////////////
			   // Implémentation de la base, trouver comment le mettre dans la boucle "while"//
			    Main sqliteDataBase = new Main();
		        sqliteDataBase.createTable();
		        sqliteDataBase.insert("Acid arrow", 3, "V", false);
		        sqliteDataBase.insert("Acid split", 1, "V", false);
		        sqliteDataBase.insert("Acid grow", 4, "T", true);
		        sqliteDataBase.insert("Acid fart", 2, "P", false);
		        sqliteDataBase.select_Good_Spells();
		       // Implémentation de la base, trouver comment le mettre dans la boucle "while"//
               ////////////////////////////////////////////////////////////////////////////////
		        
			   
	 */
			   
			   
				   
	 		
				
	} // fermeture de la boucle du programme principal
	
	
	
	
		
} // fermeture de la classe "Main"