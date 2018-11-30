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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.json.JSONObject;

public class getTweetKeys {

	public static class GetTweetKeysMapper extends Mapper<Object, Text, Text, IntWritable> {
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			//System.out.println("mapper input is: "+ value.toString());			
			try {
				JSONObject tweetJson = new JSONObject(value.toString());
				for(String tweetfield : JSONObject.getNames(tweetJson)) {
					//System.out.println("field is: " + tweetfield);
					context.write(new Text(tweetfield), new IntWritable(1));
				}							
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {		
		public void reduce(Text term, Iterable<IntWritable> ones, Context context) throws IOException, InterruptedException {			
			int count = 0;
			Iterator<IntWritable> iterator = ones.iterator();			
			while(iterator.hasNext()) {
				IntWritable tweetKey = iterator.next();
//				System.out.println("tweetKey is : " + tweetKey);
				count++;						
			}							
			context.write(term, new IntWritable(count));

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
		job.setJarByClass(getTweetKeys.class);
		job.setMapperClass(GetTweetKeysMapper.class);
		job.setReducerClass(SumReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
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
