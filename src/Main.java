import org.json.JSONArray;


public class Main{
	
	public static void main(String[] argv){
		
		Data data = new Data();
		data.getSpells();
		JSONArray dataArray= data.getSpellArray();
		System.out.println("\nThis is the number of all spells : "+dataArray.length());
		
		try {
			data.sendDataToDB();
			
		} catch (Exception e) {
			
			System.out.println("\nError while sending data to SQlite DB : "+e);	
		}
		
		try {
			data.getGoodSpells();
			
		} catch (Exception e) {
			
			System.out.println("\nError while getting data from SQlite DB : "+e);	
		}			
	}		
} 