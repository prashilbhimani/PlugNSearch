sudo add-apt-repository ppa:transmissionbt/ppa
sudo apt-get update
sudo apt-get install transmission-cli transmission-common transmission-daemon
sudo service transmission-daemon start
sudo transmission-remote -n 'transmission:transmission' -a data.torrent