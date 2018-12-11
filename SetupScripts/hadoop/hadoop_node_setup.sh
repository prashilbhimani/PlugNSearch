sudo apt-get update
sudo apt-get install -y openjdk-8-jre
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64

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
<name>fs.defaultFS</name>
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


sed -i '/export JAVA_HOME=/c\export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64' hadoop/etc/hadoop/hadoop-env.sh


ssh-keygen -t rsa -P ""
cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys

./hadoop/bin/hadoop namenode -format
./hadoop/sbin/start-dfs.sh

