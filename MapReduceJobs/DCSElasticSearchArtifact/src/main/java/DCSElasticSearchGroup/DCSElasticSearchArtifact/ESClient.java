package DCSElasticSearchGroup.DCSElasticSearchArtifact;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

class ESAPI {
	private RestHighLevelClient client;

	public ESAPI(String host, int port) {
		this.client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http"),new HttpHost("host", port+1, "http")));
	}

	public IndexResponse getIndexResponse(Map<String, Object> jsonMap, String index, String doc, String id) {
		IndexRequest indexRequest = new IndexRequest(index, doc, id).source(jsonMap);			 
		try {
			IndexResponse indexResponse = this.client.index(indexRequest, RequestOptions.DEFAULT);
			return indexResponse;
		} catch (IOException e) {				
			e.printStackTrace();
		}
		return null;			
	}
}

public class ESClient 
{
	public static Map<String, Object> createdummyInput() {
    	Map<String, Object> jsonMap = new HashMap<String, Object>();
    	jsonMap.put("user", "sharan");
    	jsonMap.put("postDate", new Date());
    	jsonMap.put("message", "trying out Elasticsearch");
    	return jsonMap;
	}
    public static void main( String[] args ) throws IOException
    {
    	ESAPI esclient = new ESAPI("localhost", 9200);

    	Map<String, Object> jsonMap = createdummyInput();
    	
    	String index = "apiposts";
    	String doc = "doc";
    	String id = "1";
    	// Index API
    	IndexResponse indexResponse = esclient.getIndexResponse(jsonMap, index, doc, id);
    	if(indexResponse != null) {
    		System.out.println(String.format("index: %s, type: %s, id: %s	", indexResponse.getIndex(), indexResponse.getType(), indexResponse.getId()));	
    	}
    	    	
    	System.out.println("Done");
    	

    	
    	
    }
}
