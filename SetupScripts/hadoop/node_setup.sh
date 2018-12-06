sudo apt-get update
sudo apt-get install -y openjdk-8-jre
export JAVA_HOME='/usr/lib/jvm/java-1.8.0-openjdk-amd64'

wget http://apache.claz.org/hadoop/common/stable/hadoop-2.9.2.tar.gz
tar -xvf hadoop-2.9.2.tar.gz
mv hadoop-2.9.2 hadoop
rm hadoop-2.9.2.tar.gz

export HADOOP_HOME=$HOME/hadoop
export PATH=$PATH:$HADOOP_HOME/bin

sudo rm hadoop/etc/hadoop/core-site.xml
cat >hadoop/etc/hadoop/core-site.xml <<EOL
<configuration>
<property>
<name>fs.default.name</name>
<value>hdfs://localhost:9000</value>
</property>
</configuration>
EOL
cat hadoop/etc/hadoop/core-site.xml


sudo rm hadoop/etc/hadoop/hdfs-site.xml
cat >hadoop/etc/hadoop/hdfs-site.xml <<EOL
<configuration>
<property>
<name>dfs.replication</name>
<value>1</value>
</property>
<property>
<name>dfs.permission</name>
<value>false</value>
</property>
</configuration>
EOL
cat hadoop/etc/hadoop/hdfs-site.xml

cat >hadoop/etc/hadoop/mapred-site.xml <<EOL
<configuration>
<property>
<name>mapreduce.framework.name</name>
<value>yarn</value>
</property>
</configuration>
EOL
cat hadoop/etc/hadoop/mapred-site.xml


sudo rm hadoop/etc/hadoop/yarn-site.xml
cat >hadoop/etc/hadoop/yarn-site.xml <<EOL
<configuration>
<property>
<name>yarn.nodemanager.aux-services</name>
<value>mapreduce_shuffle</value>
</property>
<property>
<name>yarn.nodemanager.auxservices.mapreduce.shuffle.class</name>
<value>org.apache.hadoop.mapred.ShuffleHandler</value>
</property>
</configuration>
EOL
cat hadoop/etc/hadoop/yarn-site.xml

sed 's/export JAVA_HOME=.*/export JAVA_HOME=\/usr\/lib\/jvm\/java-1.8.0-openjdk-amd64/' hadoop/etc/hadoop/hadoop-env.sh

ssh-keygen -t rsa -P ""
cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys

./bin/hadoop namenode -format
./hadoop/sbin/start-all.sh

