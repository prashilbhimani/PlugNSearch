package DCSElasticSearchGroup.DCSElasticSearchArtifact;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.elasticsearch.action.index.IndexResponse;
import org.json.JSONObject;
import org.json.simple.JSONValue;


public class ESDataPush {
	public static ESAPI esclient = new ESAPI("localhost", 9200);
	
	public static class PushTweetToESMapper extends Mapper<Object, Text, Text, Text> {
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {					
			try {
				Map<String, Object> jsonMap = new HashMap<String, Object>();				
				JSONObject tweetJson = new JSONObject(value.toString());
				
				
				/*for(String tweetfield : JSONObject.getNames(tweetJson)) {					
					jsonMap.put(tweetfield, tweetJson.get(tweetfield));
				}*/
				/*JSONObject dummy = new JSONObject();
				dummy.put("title", "this is title");
				dummy.put("name", "this is name");
				dummy.put("age", 20);*/
				IndexResponse indexResponse = esclient.getIndexResponseForStr( tweetJson.toString(), "testmapping", "_doc", null);
				esclient.processIndexResponse(indexResponse);
				
				
			} catch(Exception e) {
				e.printStackTrace();
			}
						
		}

	}

	public static class PushTweetToESReducer extends Reducer<Text, Text, Text, Text> {		
		public void reduce(Text term, Iterable<Text> ones, Context context) throws IOException, InterruptedException {			
				}
	}


	public static void main(String []args) throws IOException, ClassNotFoundException, InterruptedException {
		System.out.println("Starting ...");
		Configuration conf = new Configuration();

		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		if(otherArgs.length != 2) {
			System.err.println("Usage: Wordcount <if> <dir>");
		}
		JobConf f=new JobConf();
		f.setNumReduceTasks(1);
		System.out.println("Setting Job");		
		Job job = Job.getInstance(conf, "Job name: WordCount");
		job.setJarByClass(ESDataPush.class);
		job.setMapperClass(PushTweetToESMapper.class);
		job.setReducerClass(PushTweetToESReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setNumReduceTasks(1);

		System.out.println("Setting files");
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.out.println("Finished Setting files.. Starting to wait for Status");

		boolean status = job.waitForCompletion(true);
		System.out.println("Staus is: " + status);

		if(status) {
			System.exit(0);
		} else {
			System.exit(1);
		}
		System.out.println("Done... with job 1");
	}


}
