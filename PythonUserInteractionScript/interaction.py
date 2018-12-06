import re
import os
import sys
import json

class CombineOutputs:
    def __init__(self, base_dir, outputpath):
        self.base_dir = base_dir
        self.mr_output_regex = re.compile(r'^(part)')
        self.outputpath = outputpath

    def combineFiles(self):
        with open(self.outputpath, "w") as outfile:
            for dirpath, dirnames, filenames in os.walk(self.base_dir):
                for filename in filenames:
                    match = self.mr_output_regex.match(filename)
                    if (match):
                        path = dirpath + "/" + filename
                        with open(path, "r") as infile:
                            for line in infile:
                                outfile.write(line)
                        outfile.write("\n")


class UserPrompt:
    def __init__(self, inputpath):
        self.inputpath = inputpath
        self.results = {}

    def prompt(self):
        results = {}
        with open(self.inputpath, "r") as infile:
            for line in infile:
                line = line.strip()
                print()
                if(len(line) >0):
                    analyticsjson = json.loads(line)
                    tweetKey  = analyticsjson['tweetKey']
                    NumberofUniqueValues = analyticsjson['NumberofUniqueValues']
                    appearedCount = analyticsjson['appearedCount']
                    dataType = analyticsjson['dataType']
                    nullCount = analyticsjson['nullCount']
                    index = input("The field: " + tweetKey +" appeared " + str(appearedCount) +" times in your dataset\nIt was null " + str(nullCount) +" times\nIt has  " +str(NumberofUniqueValues)+ " unique values\nIt has the following data values: " + str(dataType) +" \nDo you want to index it? Y/N\n")
                    results[tweetKey] = index
                    print('*' * 50)
        return results


inputdir = sys.argv[1]
combinedpath = sys.argv[2]
outputpath = sys.argv[3]

c = CombineOutputs(inputdir, combinedpath)
c.combineFiles()

u = UserPrompt(combinedpath)
results = u.prompt()
print(results)
for key, value in results.items():
    with open(outputpath, "a") as outfile:
        outfile.write(key + "\t" + value + "\n")



