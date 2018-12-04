package DCSElasticSearchGroup.DCSElasticSearchArtifact;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.warmer.ShardIndexWarmerService;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

class ESAPI {
	private RestHighLevelClient client;

	public ESAPI(String host, int port) {
		this.client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http"),new HttpHost("host", port+1, "http")));
	}

	public IndexResponse getIndexResponse(Map<String, Object> jsonMap, String index, String type, String id) {
		IndexRequest indexRequest = new IndexRequest(index, type, id).source(jsonMap);			 
		try {
			IndexResponse indexResponse = this.client.index(indexRequest, RequestOptions.DEFAULT);
			return indexResponse;
		} catch (IOException e) {				
			e.printStackTrace();
		}
		return null;			
	}

	public GetResponse getResponse(String index, String type, String id ) {
		GetRequest getRequest = new GetRequest(index, type,  id);
		try {
			GetResponse getResponse = this.client.get(getRequest, RequestOptions.DEFAULT);
			return getResponse;
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean getExistsResponse(String index, String type, String id ) throws IOException {
		GetRequest getRequest = new GetRequest(index, type,  id);
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		getRequest.storedFields("_none_"); // TODO: research what this is.
		boolean exists = this.client.exists(getRequest, RequestOptions.DEFAULT);
		return exists;
	}
	
	public DeleteResponse getDeleteResponse(String index, String type, String id ) {
		DeleteRequest deleteRequest = new DeleteRequest(index, type,  id);
		try {
			DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
			return deleteResponse;
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
		String type = "doc";
		String id = "1";

		// Index API
		IndexResponse indexResponse = esclient.getIndexResponse(jsonMap, index, type, id);
		if(indexResponse != null) {
			System.out.println(String.format("index: %s, type: %s, id: %s	", indexResponse.getIndex(), indexResponse.getType(), indexResponse.getId()));	
		}
		
		// Get API
		GetResponse getResponse = esclient.getResponse(index, type, id);
		if(getResponse != null) {
			System.out.println(String.format("index: %s, type: %s, id: %s	", getResponse.getIndex(), getResponse.getType(), getResponse.getId()));
			if (getResponse.isExists()) {
			    long version = getResponse.getVersion();
			    String sourceAsString = getResponse.getSourceAsString();        
			    Map<String, Object> sourceAsMap = getResponse.getSourceAsMap(); 
			    byte[] sourceAsBytes = getResponse.getSourceAsBytes();
			    System.out.println(String.format("version is %d\nsource is %s", version, sourceAsString));
			}
		}
		
		// Exists API
		boolean exists = esclient.getExistsResponse(index, type, id);
		System.out.printf("Exists is : %b\n", exists);
		
		// Delete API
		DeleteResponse deleteResponse = esclient.getDeleteResponse(index, type, id);
		if(deleteResponse != null) {
			System.out.println(String.format("index: %s, type: %s, id: %s	", deleteResponse.getIndex(), deleteResponse.getType(), deleteResponse.getId()));
			long version = deleteResponse.getVersion();
			System.out.println(String.format("version is %d", version));
			ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
			System.out.println(String.format("Failed is %d, Success is %d, Total is %d", shardInfo.getFailed(), shardInfo.getSuccessful(), shardInfo.getTotal()));
			if (shardInfo.getFailed() > 0) {
			    for (ReplicationResponse.ShardInfo.Failure failure :
			            shardInfo.getFailures()) {
			        String reason = failure.reason();
			        System.out.println("reason for failure is: " + reason);
			    }
			}
		}
		
			
		System.out.println("Done");




	}
}
