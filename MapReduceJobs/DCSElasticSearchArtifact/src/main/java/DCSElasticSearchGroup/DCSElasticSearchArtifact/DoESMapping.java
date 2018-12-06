package DCSElasticSearchGroup.DCSElasticSearchArtifact;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.json.JSONObject;

public class DoESMapping {

	public static ArrayList<String> readlines(String inputPath) {
		ArrayList<String> lines = new ArrayList<>();
		try {
			FileInputStream fstream = new FileInputStream(inputPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;						
			while ((strLine = br.readLine()) != null)   {			
				lines.add(strLine);										
			}

			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return lines;
	}
	public static void main(String args[]) {
		System.out.println("Starting .....");

		if(args.length != 1) {
			System.err.println("Not enough Args");
		}

		ArrayList<String> lines = readlines(args[0]);		


		ESAPI esclient = new ESAPI("localhost", 9200);
		String index = "testmapping";
		String type = "doc";
		Map<String, Object> message = getIndexData(lines);
		System.out.println(message);
		try {		
			CreateIndexResponse createIndexResponse = esclient.createIndex(index);
			boolean acknowledged = createIndexResponse.isAcknowledged();
			System.out.println("Index is: " + acknowledged);
		} catch(Exception e) {

		}
		AcknowledgedResponse putMappingResponse = esclient.performMapping(index, type, message);
		System.out.println("put mapping is : " + putMappingResponse.isAcknowledged());
		System.out.println("done");

	}
	private static Map<String, Object>getIndexData(ArrayList<String> lines) {
		Map<String, Object> mapping = new HashMap<String, Object>();		
		for(String line: lines) {
			Map<String, Object> internalMap = new HashMap<String, Object>();
			String[] pairs = line.split("\t");
			if(pairs.length == 2) {
				String key = pairs[0].trim();
				String indexBoolean = pairs[1].trim().toLowerCase();
				mapping.put(key,internalMap);
				internalMap.put("type", "text"); // This needs to change
				if(indexBoolean.equals("n")) {
					internalMap.put("index", false);				
				}				
			}
		}
		return mapping;
	}
}
