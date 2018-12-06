## Will write a script that will do all that we discussed
## $1 = The data
## $2 = Custom unzipper


## Initial Setup of variables
download_dir=$1 

## Step 1 : Set up google cloud for the user:

# Part A - Download and install Google cloud sdk
#to:do - For mac
# export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
# echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
# curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
# sudo apt-get update && sudo apt-get install google-cloud-sdk

# # Part B - Initialize it
# gcloud init

# # Step 2: Set up transmission on local computer:
# sudo add-apt-repository ppa:transmissionbt/ppa
# sudo apt-get update
# sudo apt-get install transmission-cli transmission-common transmission-daemon
# sudo service transmission-daemon start

# Step 3: Create .torrent for the given file

# Part A - Create torrent
sudo cp -r $download_dir /var/lib/transmission-daemon/Downloads
transmission-create -o data.torrent -t udp://open.stealth.si:80/announce -t http://torrent.nwps.ws:80/announce -t udp://denis.stalker.upeer.me:6969/announce -t https://opentracker.xyz:443/announce -t http://open.trackerlist.xyz:80/announce -t https://4.track.ga:443/announce -t udp://retracker.hotplug.ru:2710/announce -t https://tracker.fastdownload.xyz:443/announce -t udp://tracker.birkenwald.de:6969/announce -t https://seeders-paradise.org:443/announce -t udp://chihaya.toss.li:9696/announce -t udp://tracker.torrent.eu.org:451/announce -t http://t.nyaatracker.com:80/announce -t udp://retracker.lanta-net.ru:2710/announce -t udp://tracker.port443.xyz:6969/announce -t udp://tracker.vanitycore.co:6969/announce -t https://t.quic.ws:443/announce -t udp://bt.xxx-tracker.com:2710/announce -t udp://open.demonii.si:1337/announce -t udp://ipv4.tracker.harry.lu:80/announce -t udp://tracker.opentrackr.org:1337/announce -t udp://exodus.desync.com:6969/announce -t http://tyu.ddns.net:36006/announce -t udp://hk1.opentracker.ga:6969/announce -t udp://tracker.coppersurfer.tk:6969/announce -t udp://tw.opentracker.ga:36920/announce -t http://tracker.corpscorp.online:80/announce -t udp://9.rarbg.to:2710/announce -t udp://tracker.internetwarriors.net:1337/announce -t udp://public.popcorn-tracker.org:6969/announce -t udp://explodie.org:6969/announce -t udp://tracker1.itzmx.com:8080/announce -t udp://tracker.tiny-vps.com:6969/announce -t udp://zephir.monocul.us:6969/announce -t udp://tracker.iamhansen.xyz:2000/announce -t udp://tracker.cypherpunks.ru:6969/announce $download_dir 

# Part B - Add torrent
transmission-remote -n 'transmission:transmission' -a data.torrent # error


# Step 4: Create gcloud machine and setting it up to take the input
gcloud beta compute --project="datacenterscaleproject" instances create torrentgetter --zone=us-east1-b --machine-type=n1-standard-4 --subnet=default --network-tier=PREMIUM --maintenance-policy=MIGRATE --scopes=https://www.googleapis.com/auth/cloud-platform --tags=http-server,https-server,torrentmachine --image=ubuntu-minimal-1804-bionic-v20181114 --image-project=ubuntu-os-cloud --boot-disk-size=100GB --boot-disk-type=pd-standard --boot-disk-device-name=torrentgetter
gcloud compute firewall-rules create torrentfirewall --direction=INGRESS --priority=1000 --network=default --action=ALLOW --rules=tcp:51413,udp:51413 --source-ranges=0.0.0.0/0 --target-tags=torrentmachine
gcloud compute firewall-rules create scpfirewall --direction=INGRESS --priority=1000 --network=default --action=ALLOW --rules=tcp:22 --source-ranges=0.0.0.0/0 --target-tags=torrentmachine
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter"
gcloud compute  scp --zone "us-east1-b" torrent.sh torrentgetter:~/
gcloud compute  scp --zone "us-east1-b" data.torrent torrentgetter:~/
gcloud compute --project "datacenterscaleproject" ssh --zone "us-east1-b" "torrentgetter" --command "chmod +x torrent.sh && ./torrent.sh"

# Step 5: Custom unzip


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
