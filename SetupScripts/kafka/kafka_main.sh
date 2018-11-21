# Create multiple Google cloud Instance
# Get their ips
# Set up zookeeper
# Put ips in kafka


#Create nodes
gcloud beta compute --project="datacenterscaleproject" instances create zookeeper --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name=zookeeper
for i in `seq 1 $1`;do
	gcloud beta compute --project="datacenterscaleproject" instances create "kafka$i" --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name="kafka$i"
done

#Get Ips
zk=$(gcloud compute instances describe zookeeper |sed -n "/networkIP\:\ /s/networkIP\:\ //p")
kafka=()
for i in `seq 1 $1`;do
	kafka+=($(gcloud compute instances describe "kafka$i" |sed -n "/networkIP\:\ /s/networkIP\:\ //p"))
done
ips="$(IFS=, ; echo "${kafka[*]}")"

#Setup zookeeper
gcloud compute scp --zone "us-east1-b" zookeeper_setup.sh zookeeper:~/
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "zookeeper" --command "chmod +x zookeeper_setup.sh && ./zookeeper_setup.sh $zk"

#Setup Kafka
for i in `seq 1 $1`;do
	gcloud compute scp --zone "us-east1-b" kafka_setup.sh "kafka$i":~/
	gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "kafka$i" --command "chmod +x kafka_setup.sh && ./kafka_setup.sh $i $zk"
done

#Start Zookeeper
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "zookeeper" --command "zookeeper-3.4.13/bin/zkServer.sh start"

#Start Kafka
for i in `seq 1 $1`;do
	gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "kafka$i" --command "kafka_2.12-1.1.0/bin/kafka-server-start.sh -daemon kafka_2.12-1.1.0/config/server.properties"
done
