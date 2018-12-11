

# Step 6: Set up kafka
sudo chmod 755 kafka/kafka_main.sh
./kafka/kafka_main.sh 1
for i in `seq 1 $1`;do
	kafka+=($(gcloud compute instances describe "kafka$i" |sed -n "/networkIP\:\ /s/networkIP\:\ //p"))
done
kafka_ips="$(IFS=, ; echo "${kafka[*]}")"


# Setp 7 : Set up storm
sudo chmod 755 storm/create.sh
./storm/create.sh 1

read -p "Press enter when the file downloads is complete"

# Step 8 : Submit topology

# Step 9 : Copy files
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter" --command "mkdir data && sudo cp -r /var/lib/transmission-daemon/Downloads/ data/"
gcloud compute  scp --zone "us-east1-b" $2 torrentgetter:~/data/
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter" --command "chmod +x data/$2 && ./data/$2"

# Step 10 : Send to kafka
gcloud compute  scp --zone "us-east1-b" kafkaProducer.py torrentgetter:~/
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter" --command "python kafkaProducer.py data/ $kafka_ips"
