# Create multiple Google cloud Instance
# Get their ips
# Set up zookeeper
# Put ips in kafka

gcloud beta compute --project="datacenterscaleproject" instances create zookeeper --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name=zookeeper

for i in `seq 1 $1`;do
	gcloud beta compute --project="datacenterscaleproject" instances create "slave$i" --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name="slave$i"
done

kafka=()
for i in `seq 1 $1`;do
	kafka+=($(gcloud beta compute --project="datacenterscaleproject" instances create "slave$i" --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name="slave$i"))
done

ips="$(IFS=, ; echo "${kafka[*]}")"

# Get IP
zk=$(gcloud compute instances describe zookeeper --zone=us-east1-b |sed -n "/networkIP\:\ /s/networkIP\:\ //p")

#Setupzookeeper
gcloud compute --zone "us-east1-b" scp ~/zookeeper_setup.sh zookeeper:~/
gcloud compute --project "independentstudy-219521" ssh --zone "us-east1-b" "zookeeper" --command "chmod +x zookeeper_setup.sh && ./zookeeper_setup.sh $zk"
