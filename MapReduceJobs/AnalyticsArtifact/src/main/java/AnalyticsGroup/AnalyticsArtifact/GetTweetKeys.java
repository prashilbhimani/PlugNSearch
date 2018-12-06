package AnalyticsGroup.AnalyticsArtifact;


import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.jobhistory.HistoryViewer.AnalyzedJob;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.json.JSONObject;

import net.minidev.json.JSONArray;

public class GetTweetKeys {
	public static JSONObject getTypeAndValue(JSONObject tweetJson, String key) {
		Object tweetvalue = tweetJson.get(key);
		JSONObject result = new JSONObject();
		result.put("type", JSONObject.NULL.toString());
		result.put("value", JSONObject.NULL.toString());

		if(tweetvalue instanceof Long || tweetvalue instanceof Double || tweetvalue instanceof Float || tweetvalue instanceof Integer) {
			result.put("type", "Numeric");
			result.put("value", String.valueOf(tweetJson.getBigInteger(key)));
		} else if(tweetvalue instanceof Boolean) {
			result.put("type", "Boolean");
			result.put("value", String.valueOf(tweetJson.getBoolean(key)));
		} else if(tweetvalue instanceof String || tweetvalue instanceof Byte) {
			result.put("type", "String");
			result.put("value", tweetJson.getString(key));
		} else if(tweetvalue instanceof JSONArray) {
			result.put("type", "JSONArray");
			result.put("value", tweetJson.getJSONArray(key).toString());
		} else if(tweetvalue instanceof JSONObject) {
			result.put("type", "JSONObject");
			result.put("value", tweetJson.getJSONObject(key).toString());
		} 
		return result;
	}

	public static class GetTweetKeysMapper extends Mapper<Object, Text, Text, Text> {
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {					
			try {
				JSONObject tweetJson = new JSONObject(value.toString());
				for(String tweetfield : JSONObject.getNames(tweetJson)) {					
					context.write(new Text(tweetfield), value);
				}							
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static class SumReducer extends Reducer<Text, Text, Text, Text> {		
		public void reduce(Text term, Iterable<Text> ones, Context context) throws IOException, InterruptedException {			
			int appearedCount = 0;
			int nullCount = 0;			
			TreeSet<String> typeSet = new TreeSet<String>();			
			TreeSet<String> valueSet = new TreeSet<String>();
			Iterator<Text> iterator = ones.iterator();			
			
			while(iterator.hasNext()) {
				System.out.println("key is: " + term.toString());
				Text tweet = iterator.next();
				JSONObject tweetJson = new JSONObject(tweet.toString());
				JSONObject typeAndValue = GetTweetKeys.getTypeAndValue(tweetJson, term.toString());
				
				typeSet.add(typeAndValue.getString("type"));
				if(typeAndValue.getString("type").equals("null")) {
					nullCount++;
				} else{
					appearedCount++;	
				}				
				valueSet.add(typeAndValue.getString("value"));

			}
			JSONObject result = new JSONObject();
			result.put("tweetKey", term.toString());
			result.put("appearedCount", appearedCount);
			result.put("nullCount", nullCount);
			
			JSONArray types = new JSONArray();
			Object[] typedArr = typeSet.toArray();
			for(int i=0; i < typedArr.length ; i++) {
				types.add(typedArr[i]);
			}
			result.put("dataType", types);
			
			JSONArray values = new JSONArray();
			Object[] valueArr = valueSet.toArray();
			for(int i=0; i < valueArr.length ; i++) {
				values.add(valueArr[i]);
			}
			
			result.put("NumberofUniqueValues", valueArr.length);
			result.put("values", values);
			context.write(new Text(result.toString()), new Text());

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
		job.setJarByClass(GetTweetKeys.class);
		job.setMapperClass(GetTweetKeysMapper.class);
		job.setReducerClass(SumReducer.class);

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
