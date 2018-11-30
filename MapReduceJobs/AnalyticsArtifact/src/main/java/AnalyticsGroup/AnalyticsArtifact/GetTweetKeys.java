package AnalyticsGroup.AnalyticsArtifact;


import java.io.IOException;
import java.util.Iterator;
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
	public static String getType(Object tweetvalue) {
		String type = "Is Nullable";
		if(tweetvalue instanceof Long) {
			type = "Long";
		} else if(tweetvalue instanceof Double) {
			type = "Double";
		} else if(tweetvalue instanceof Float) {
			type = "Float";
		} else if(tweetvalue instanceof Integer) {
			type = "Integer";
		} else if(tweetvalue instanceof Boolean) {
			type = "Boolean";
		} else if(tweetvalue instanceof String) {
			type = "String";
		} else if(tweetvalue instanceof Byte) {
			type = "Byte";
		} else if(tweetvalue instanceof JSONArray) {
			type = "JSONArray";
		} else if(tweetvalue instanceof JSONObject) {
			type = "JSONObject";
		} 
		return type;
	}
	
	public static class GetTweetKeysMapper extends Mapper<Object, Text, Text, Text> {
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			//System.out.println("mapper input is: "+ value.toString());			
			try {
				JSONObject tweetJson = new JSONObject(value.toString());
				for(String tweetfield : JSONObject.getNames(tweetJson)) {
					//System.out.println("field is: " + tweetfield);
					context.write(new Text(tweetfield), value);
				}							
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static class SumReducer extends Reducer<Text, Text, Text, Text> {		
		public void reduce(Text term, Iterable<Text> ones, Context context) throws IOException, InterruptedException {			
			int count = 0;
			String type = "unknown";
			
			Iterator<Text> iterator = ones.iterator();			
			while(iterator.hasNext()) {
				Text tweet = iterator.next();
				JSONObject tweetJson = new JSONObject(tweet.toString());
				Object value = tweetJson.get(term.toString());
				type = GetTweetKeys.getType(value);
				count++;						
			}
			JSONObject result = new JSONObject();
			result.put("tweetKey", term.toString());
			result.put("keyCount", count);
			result.put("dataType", type);
			context.write(new Text(result.toString()), new Text(""));

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
