gcloud beta compute --project="datacenterscaleproject" instances create zookeeper --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE  --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name=zookeeper
gcloud beta compute --project="datacenterscaleproject" instances create nimbus --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name=nimbus

for i in `seq 1 $1`;do
	gcloud beta compute --project="datacenterscaleproject" instances create "slave$i" --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server --image=debian-9-stretch-v20181011 --image-project=debian-cloud --boot-disk-size=50GB --boot-disk-type=pd-standard --boot-disk-device-name="slave$i"
done


# Get IP
zk=$(gcloud compute instances describe zookeeper --zone=us-east1-b |sed -n "/networkIP\:\ /s/networkIP\:\ //p")
nimbus=$(gcloud compute instances describe nimbus --zone=us-east1-b |sed -n "/networkIP\:\ /s/networkIP\:\ //p")

#Copy to zookeeper
gcloud compute --zone "us-east1-b" scp ~/zookeeper_setup.sh zookeeper:~/
#Setup zookeeper
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "zookeeper" --command "chmod +x zookeeper_setup.sh && ./zookeeper_setup.sh $zk"


#Copy to Nimbus
gcloud compute --zone "us-east1-b" scp ~/storm_setup.sh nimbus:~/
#Nimbus setup
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "nimbus" --command "chmod +x storm_setup.sh && ./storm_setup.sh $zk $nimbus $1"

for i in `seq 1 $1`; do
	#Slave copy
	gcloud compute --zone "us-east1-b" scp ~/storm_setup.sh "slave$i":~/
	#Slave setup
	gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "slave$i" --command "chmod +x storm_setup.sh && ./storm_setup.sh $zk $nimbus $1"
done