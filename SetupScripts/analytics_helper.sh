./hadoop/bin/hdfs dfs -mkdir /user
./hadoop/bin/hdfs dfs -mkdir /user/prashil

rm data/downloads/*.zip
rm data/downloads/*.sh

./hadoop/bin/hdfs dfs -put data/downloads/data input-all-data

./hadoop/bin/hadoop jar GetTweetKeys.jar input-all-data/ output-data-keys
./hadoop/bin/hdfs dfs -get output-data-keys output-data-keys

python3 interaction.py output-data-keys analytics.txt index.txt

java -jar Mapper.jar index.txt localhost 9200 data data

./hadoop/bin/hadoop jar DataPush.jar input-all-data output-empty
