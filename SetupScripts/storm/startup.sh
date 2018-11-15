##Do not run this. The commands are blocking - Check how to set them to background

gcloud compute --project "independentstudy-219521" ssh --zone "us-east1-b" "zookeeper" --command "./zookeeper-3.4.13/bin/zkStart.sh start"
gcloud compute --project "independentstudy-219521" ssh --zone "us-east1-b" "nimbus" --command "./apache-storm-1.2.1/bin/storm nimbus"
for i in `seq 1 $1`; do
	gcloud compute --project "independentstudy-219521" ssh --zone "us-east1-b" "slave$i" --command "./apache-storm-1.2.1/bin/storm supervisor"
done
gcloud compute --project "independentstudy-219521" ssh --zone "us-east1-b" "nimbus" --command "./apache-storm-1.2.1/bin/storm ui"
