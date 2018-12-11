gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter" --command "mkdir data && sudo cp -r /var/lib/transmission-daemon/downloads/ data/ && sudo chmod 777 -R data"

if [ ! -z "$1" ]
  then
    gcloud compute  scp --zone "us-east1-b" $1 torrentgetter:~/data/downloads
	gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter" --command "chmod +x data/downloads/$1 && cd data/downloads && ./$1"
fi

gcloud compute scp --zone "us-east1-b" hadoop/hadoop_node_setup.sh torrentgetter:~/
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter" --command "chmod +x hadoop_node_setup.sh && ./hadoop_node_setup.sh"

gcloud compute firewall-rules create elasticsearchrule --direction=INGRESS --priority=1000 --network=default --action=ALLOW --rules=tcp:9200,tcp:9300-9400,tcp:22 --source-ranges=0.0.0.0/0 --target-tags=http-server

gcloud compute  scp ElasticSearch/SingleNode/es_single_setup.sh torrentgetter:~/ --zone "us-east1-b"
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" torrentgetter  --command "chmod +x es_single_setup.sh && ./es_single_setup.sh" 

gcloud compute scp --zone "us-east1-b" jars/GetTweetKeys.jar torrentgetter:~/
gcloud compute scp --zone "us-east1-b" jars/Mapper.jar torrentgetter:~/
gcloud compute scp --zone "us-east1-b" jars/DataPush.jar torrentgetter:~/

gcloud compute scp --zone "us-east1-b" pythonscript/interaction.py torrentgetter:~/

gcloud compute scp --zone "us-east1-b" analytics_helper.sh torrentgetter:~/

gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" torrentgetter  --command "chmod +x analytics_helper.sh && ./analytics_helper.sh" 

#Run map reduce for get tweet keys
#run python script
#run jar for index creation and mapping
#run jar to push data

