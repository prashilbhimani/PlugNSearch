package DCSElasticSearchGroup.DCSElasticSearchArtifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.rest.RestStatus;
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

	public UpdateResponse getUpdateResponse(Map<String, Object> jsonMap, String index, String type, String id) {		
		UpdateRequest request = new UpdateRequest(index, type,  id).doc(jsonMap);
		try {
			UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
			return updateResponse;
		} catch (ElasticsearchException e) {
			if (e.status() == RestStatus.NOT_FOUND) {
				// This is when the index or the type does not exist. Its a little misleading 		    	
				System.out.println("Handle the exception thrown because the document not exist .. Index or Type might not exist");
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return null;

	}

	public BulkResponse doBulkInsert(ArrayList<Map<String, Object>> jsonMaps, String index, String type, String idKey) {
		BulkRequest request = new BulkRequest();
		if(jsonMaps != null) {
			for(int i=0; i< jsonMaps.size(); i++) {
				String id = (idKey == null || idKey.length() == 0) ? Integer.toString((i+1)) : jsonMaps.get(i).get(idKey).toString();
				request.add(new IndexRequest(index, type, id)
						.source(jsonMaps.get(i)));
			}
			try {
				BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
				return bulkResponse;
			} catch (IOException e) {			
				e.printStackTrace();
			}
		}
		return null;		
	}

	public void processIndexResponse(IndexResponse indexResponse) {
		if(indexResponse != null) {
			System.out.println(String.format("index: %s, type: %s, id: %s	", indexResponse.getIndex(), indexResponse.getType(), indexResponse.getId()));	
		}		
	}

	public void processGetResponse(GetResponse getResponse) {
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
	}

	public void processDeleteResponse(DeleteResponse deleteResponse) {
		if(deleteResponse != null) {
			System.out.println(String.format("index: %s, type: %s, id: %s	", deleteResponse.getIndex(), deleteResponse.getType(), deleteResponse.getId()));
			long version = deleteResponse.getVersion();
			System.out.println(String.format("version is %d", version));
			ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
			System.out.println(String.format("Failed is %d, Success is %d, Total is %d", shardInfo.getFailed(), shardInfo.getSuccessful(), shardInfo.getTotal()));
			if (shardInfo.getFailed() > 0) {
				for (ReplicationResponse.ShardInfo.Failure failure :shardInfo.getFailures()) {
					String reason = failure.reason();
					System.out.println("reason for failure is: " + reason);
				}
			}
		}
		
	}

	public void processUpdateResponse(UpdateResponse updateResponse) {
		long version = updateResponse.getVersion();
		System.out.println(String.format("version is %d", version));
		// TODO: Readup on the 4 things below
		if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
			System.out.println("We created it!"); 
		} else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
			System.out.println("We updated it!");
		} else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
			System.out.println("We deleted it!");
		} else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
			System.out.println("We Noop it!");
		}
		GetResult result = updateResponse.getGetResult();		
		if (result != null && result.isExists()) {
			String sourceAsString = result.sourceAsString(); 
			Map<String, Object> sourceAsMap = result.sourceAsMap(); 
			byte[] sourceAsBytes = result.source();
			System.out.println(String.format("version is %d\nsource is %s", version, sourceAsString));		    
		} 
		ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
		if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

		}
		if (shardInfo.getFailed() > 0) {
			for (ReplicationResponse.ShardInfo.Failure failure :shardInfo.getFailures()) {
				String reason = failure.reason();
				System.out.println("reason for failure is: " + reason);
			}
		}
		
	}

	public void handleBulkResponse(BulkResponse bulkResponse) {
		for (BulkItemResponse bulkItemResponse : bulkResponse) { 
			DocWriteResponse itemResponse = bulkItemResponse.getResponse(); 
			if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
					|| bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) { 
				IndexResponse indexResponse = (IndexResponse) itemResponse;
				this.processIndexResponse(indexResponse);

			} else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) { 
				UpdateResponse updateResponse = (UpdateResponse) itemResponse;
				this.processUpdateResponse(updateResponse);

			} else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) { 
				DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
				this.processDeleteResponse(deleteResponse);				
			}
		}
		
		if (bulkResponse.hasFailures()) { 
			for (BulkItemResponse bulkItemResponse : bulkResponse) {
				if (bulkItemResponse.isFailed()) { 
					BulkItemResponse.Failure failure = bulkItemResponse.getFailure(); 

				}
			}
		}
		
	}

	public AcknowledgedResponse performMapping(String index, String type, Map<String, Object> mapping, String key) {
		PutMappingRequest request = new PutMappingRequest(index); 
		request.type(type);
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(key, mapping);// this needs to change
		jsonMap.put("properties", properties);
		request.source(jsonMap);
		try {
			AcknowledgedResponse putMappingResponse = client.indices().putMapping(request, RequestOptions.DEFAULT);
			return putMappingResponse;
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
	public static Map<String, Object> createdummyInput(String username) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("user", username);
		jsonMap.put("postDate", new Date());
		jsonMap.put("message", "trying out Elasticsearch");
		return jsonMap;
	}
	public static Map<String, Object> createdummyInput(String username, Date postDate, String message) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("user", username);
		jsonMap.put("postDate", postDate);
		jsonMap.put("message", message);
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
		esclient.processIndexResponse(indexResponse);


		// Get API
		GetResponse getResponse = esclient.getResponse(index, type, id);
		esclient.processGetResponse(getResponse);

		// Exists API
		boolean exists = esclient.getExistsResponse(index, type, id);
		System.out.printf("Exists is : %b\n", exists);

		// Delete API
		DeleteResponse deleteResponse = esclient.getDeleteResponse(index, type, id);
		esclient.processDeleteResponse(deleteResponse);

		// Update API
		esclient.getIndexResponse(jsonMap, index, type, id); // Deleted above, so doing an insert here
		Map<String, Object> jsonMap2 = new HashMap<String, Object>();
		jsonMap2.put("updated", new Date());
		jsonMap2.put("reason", "daily update");
		UpdateResponse updateResponse = esclient.getUpdateResponse(jsonMap2, index, type, id);
		esclient.processUpdateResponse(updateResponse);
		// Test Update Failure
		esclient.getUpdateResponse(createdummyInput("prashil"), index, "faill", id); // to check how we handle errors				
		System.out.println("Done");

		// Bulk insert API
		ArrayList<Map<String, Object>> jsonMaps = new ArrayList<Map<String,Object>>();
		for(int i=0; i< 5; i ++) {
			jsonMaps.add(createdummyInput("User" + Integer.toString(i), new Date(), "This is User Number " + i));
		}
		BulkResponse bulkResponse = esclient.doBulkInsert(jsonMaps, index, type, "user");
		esclient.handleBulkResponse(bulkResponse);
		
		// Mapping API
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("type", "text"); 
		AcknowledgedResponse putMappingResponse = esclient.performMapping(index, type, message, "message");
		System.out.println("put mapping is : " + putMappingResponse.isAcknowledged());
		

	}
}
