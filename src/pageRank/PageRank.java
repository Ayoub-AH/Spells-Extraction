package pageRank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class PageRank {

	public static void main(String[] args) throws JSONException, IOException {
		
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB( "PageRank" );
		BasicDBObject doc = new BasicDBObject();
		List<BasicDBObject> graph = new ArrayList<BasicDBObject>();
		JSONObject currentData = new JSONObject();
		
		// Free the collection from any previous data 
		if(db.collectionExists("PageRank"))
			db.getCollection("PageRank").drop();

		DBCollection coll = db.getCollection("PageRank");
		mongoClient.setWriteConcern(WriteConcern.JOURNALED);
		
		doc.append("pagerank", 1);
		doc.append("adjlist", new char[] {'B', 'C'});
		doc.append("_id", "A");
		
		coll.insert(doc);
		
		doc = new BasicDBObject();
		doc.append("pagerank", 1);
		doc.append("adjlist", new char[] {'C'});
		doc.append("_id", "B");
		
		coll.insert(doc);
		
		doc = new BasicDBObject();
		doc.append("pagerank", 1);
		doc.append("adjlist", new char[] {'A'});
		doc.append("_id", "C");
		
		coll.insert(doc);

		doc = new BasicDBObject();
		doc.append("pagerank", 1);
		doc.append("adjlist", new char[] {'C'});
		doc.append("_id", "D");
		
		coll.insert(doc);
		
		
		doc = new BasicDBObject();
		
		
		String map = "function() { "+
				 "var vertex = this._id;" + 
				 "var adjlist = this.adjlist;" + 
				 "var pagerank = this.pagerank;"+
				 "for (var i = 0; i < adjlist.length; i++) {" + 
				 "var adj = adjlist[i];" + 
				 "emit(adj, pagerank/adjlist.length);}" +
				 "emit(vertex, 0);" + 
				 "emit(vertex, adjlist);"+
				 "};";
	   
		String reduce = "function (key, adjlist) {" + 
				"var damping = 0.85;"+ 
				"var adj_list = [];" + 
				"var pagerank = 0.0;"+
				"for (var i = 0; i < adjlist.length; i++){" +
				"var adj = adjlist[i];" + 
				"if (adj instanceof Array) {" + 
				"adj_list = adj;" +
				"}else{" + 
				"pagerank += adj;}}" + 
				"pagerank = 1 - damping + ( damping * pagerank );" +
				"return { pagerank: pagerank, adjlist: adj_list }" + 
				" };";
		
	
		MapReduceCommand cmd = new MapReduceCommand(coll, map, reduce, null, MapReduceCommand.OutputType.INLINE, null);
		
		FileWriter fw = new FileWriter("PageRank.txt", false); 
		BufferedWriter output = new BufferedWriter(fw);

		
		for(int i=0; i<20;i++) {
			
			output.write("\n************************************************************************\n");
			output.write("\nInteration : "+(i+1)+"\n");
			
			MapReduceOutput out = coll.mapReduce(cmd);
			
			coll.drop();
			coll = db.getCollection("PageRank");
			mongoClient.setWriteConcern(WriteConcern.JOURNALED);
			
			for (DBObject o : out.results()) {
				
				DBObject value = (DBObject) o.get("value");
				
				doc.append("_id",o.get("_id"));
				doc.append("pagerank", value.get("pagerank"));
				doc.append("adjlist", value.get("adjlist"));
				
				coll.insert(doc);
				
				output.write(o.toString()+"\n\n");
			}
		}
		
		output.write("End of pagerank");
		output.flush();  
		output.close();
		
		mongoClient.close();
	}

}
