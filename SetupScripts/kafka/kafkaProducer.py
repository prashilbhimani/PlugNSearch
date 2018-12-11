import sys

def readlines(path):
    data=[]
    f=open(path)
    for line in f:
        data.append(line)
    return data


def push_to_kafka(input_dir,kafka_servers ):
    topdir = './'+input_dir
    exten = '.json'
    producer = KafkaProducer(bootstrap_servers=[kafka_servers],value_serializer=lambda m: m.encode('ascii'))
    for dirpath, dirnames, files in os.walk(topdir):
        for name in files:
            if name.lower().endswith(exten):
                lines=readlines(os.path.join(dirpath, name))
                for l in lines:
                    producer.send('data',l)

push_to_kafka(sys.argv[1],sys.argv[2])